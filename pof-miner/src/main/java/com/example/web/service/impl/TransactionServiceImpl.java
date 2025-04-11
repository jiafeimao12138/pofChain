package com.example.web.service.impl;

import com.example.base.entities.transaction.*;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.base.utils.BtcAddressUtils;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.Mempool;
import com.example.web.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.base.Sha256Hash;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final DBStore dbStore;
    private final Mempool mempool;
    private final UTXOSet utxoSet;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();
    private final Lock readLock = rwl.readLock();
    /**
     * 创建新coinbase交易
     * @param address 接收方地址，也就是成功挖矿的矿工地址
     * @return
     */
    @Override
    public Transaction createCoinbaseTransaction(String address, long height, int blockReward, int fee) {
        return newCoinbaseTX(address, height, blockReward, fee);
    }

    /**
     * 创建CoinBase交易
     *
     * @param toAddress   收账的钱包地址
     * @param blockHeight
     * @return
     */
    public static Transaction newCoinbaseTX(String toAddress, long blockHeight, int blockReward, int fee) {
        Transaction coinBaseTX = new Transaction();
        // 创建交易输入
        byte[] coinBaseData = ByteBuffer.allocate(8).putLong(blockHeight).array();
        TXInput txInput = TXInput.coinbaseInput(coinBaseData);
        coinBaseTX.addInput(txInput);

        // 创建交易输出
        TXOutput txOutput = TXOutput.newTXOutput(blockReward + fee, toAddress);
        coinBaseTX.addOutput(txOutput);
        coinBaseTX.setCreateTime(System.currentTimeMillis());
//        logger.info("coinBaseTX长度: {}", coinBaseTX.getInputs().get(0).getPreviousTXId().length);
        return coinBaseTX;
    }

    /**
     * 创建新普通交易
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     * @throws Exception
     */
    @Override
    public Transaction createCommonTransaction(String fromAddress, String toAddress, int amount, int fee) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(fromAddress);
        byte[] pubKeyHash = senderWallet.getPubKeyHash();
        byte[] publicKey = senderWallet.getPublicKey();

        SpendableOutputResult result = findSpendableOutputs(pubKeyHash, amount + fee);
        int accumulated = result.getAccumulated();
        // 找零
        int change = accumulated - amount - fee;
        Map<String, List<Integer>> unspentOuts = result.getUnspentOuts();

        if (result.getUnspentOuts() == null) {
            return null;
        }
        Set<Map.Entry<String, List<Integer>>> entrySet = unspentOuts.entrySet();
        Transaction newTx = new Transaction();
        // 生成交易输入
        for (Map.Entry<String, List<Integer>> entry : entrySet) {
            String txIdStr = entry.getKey();
            List<Integer> outIds = entry.getValue();
            byte[] txId = ByteUtils.hexToBytes(txIdStr);
            for (Integer outIndex : outIds) {
                // 添加交易输入和签名
                newTx.addInput(new TXInput(txId, outIndex));
            }
        }
        // 生成交易输出，包括付款方和找零
        List<TXOutput> txOutputs = new ArrayList<>();
        txOutputs.add(TXOutput.newTXOutput(amount, toAddress));
        // 找零
        if (change > 0) {
            txOutputs.add(TXOutput.newTXOutput((accumulated - amount), fromAddress));
        }
        newTx.setFee(fee);
        newTx.addOutputs(txOutputs);
        newTx.setCreateTime(System.currentTimeMillis());
        newTx.setTxId(newTx.getTxId());
        // 签名
        sign(newTx, senderWallet.getPrivateKey(), publicKey);
        return newTx;
    }

    /**
     * 创建supplier奖励交易，无手续费，不需要打包
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     */
    @Override
    public Transaction createFuzzingRewardTransaction(String fromAddress, String toAddress, int amount) {
        try {
            Transaction transaction = createCommonTransaction(fromAddress, toAddress, amount, 0);
            return transaction;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验交易合法性
     * @param transaction
     * @return
     * @throws Exception
     */
    @Override
    public boolean verify(Transaction transaction) throws Exception {
        return verifyTransaction(transaction);
    }

    /**
     * 将交易以及utxo存储到本地数据库中
     * @param transaction
     */
    @Override
    public void txStore(Transaction transaction){
//        // 校验交易是否合法
//        if (!verify(transaction)) {
//            logger.info("交易不合法！");
//            return false;
//        }
        writeLock.lock();
        String txId = transaction.getTxIdStr();
        dbStore.put(WalletPrefix.TX_PREFIX.getPrefix() + txId, transaction);
        dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, transaction.getOutputs());
        writeLock.unlock();
//        logger.info("已将交易{}存入数据库", txId);
    }

    /**
     * 存储到mempool中
     * @param transaction
     * @return
     */
    @Override
    public void storeMempool(Transaction transaction) {
        mempool.addTransaction(transaction);
    }

    /**
     * 寻找能够花费的交易
     *
     * @param pubKeyHash 钱包公钥Hash
     * @param amount     花费金额
     */
    public SpendableOutputResult findSpendableOutputs(byte[] pubKeyHash, int amount) {
        readLock.lock();
        Map<String, List<Integer>> thisUtxoMap = new HashMap<>();
        int accumulated = 0;
        Map<String, Object> utxoMap = dbStore.searchforWallet(WalletPrefix.UTXO_PREFIX.getPrefix());
        for (Map.Entry<String, Object> entry : utxoMap.entrySet()) {
            String TxId = entry.getKey();
            List<TXOutput> txOutputList = (List<TXOutput>)entry.getValue();
            for (int i = 0; i < txOutputList.size(); i++) {
                if (txOutputList.get(i).isLockedWithKey(pubKeyHash) && accumulated < amount) {
                    accumulated += txOutputList.get(i).getValue();
//                    把该UTXO加入到本次需要花费的列表中
                    List<Integer> indexList = thisUtxoMap.get(TxId);
                    if (CollectionUtils.isEmpty(indexList)) {
                        indexList = new ArrayList<>();
                    }
                    indexList.add(i);
                    thisUtxoMap.put(TxId, indexList);
                } else if (accumulated >= amount) {
                    break;
                }
            }
        }
        // 如果所有UTXO都不够
        if (amount > accumulated) {
            return new SpendableOutputResult(accumulated, null);
        }
        // TODO：后面看要不要手动close
//        dbStore.close();
        readLock.unlock();
        return new SpendableOutputResult(accumulated, thisUtxoMap);
    }

    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param pubKey 明文公钥
     */
    public void sign(Transaction transaction, BCECPrivateKey privateKey, byte[] pubKey) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);

        for (int i = 0; i < transaction.getInputs().size(); i++) {
//            获取第i个交易输入
            TXInput txInputCopy = transaction.getInputs().get(i);
//            将ScriptSig用占位符替代
            txInputCopy.setScriptSig("OP_0".getBytes());
            transaction.setTxId(transaction.getTxId());

            // 得到要签名的数据(不包括TXId)
            byte[] tobeSigned = Sha256Hash.hashTwice(SerializeUtils.serialize(this));

            // 对整个交易信息进行签名
            ecdsaSign.update(tobeSigned);
            byte[] signature = ecdsaSign.sign();

            // 将整个交易数据的签名以及自己的公钥赋值给交易输入，因为交易输入需要包含整个交易信息的签名
            // 注意是将得到的签名赋值给原交易信息中的交易输入
            transaction.getInputs().get(i).setScriptSig(signature, pubKey);
        }
    }

    public boolean verifyTransaction(Transaction transaction) throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (transaction.isCoinbase()) {
            return true;
        }
        for (int i = 0; i < transaction.getInputs().size(); i++) {
//            获取第i个交易输入
            TXInput txInputCopy = transaction.getInputs().get(i);
//            将ScriptSig用占位符替代
            txInputCopy.setScriptSig("OP_0".getBytes());
            transaction.setTxId(transaction.getTxId());

            // 得到要签名的数据(不包括TXId)
            byte[] tobeSigned = Sha256Hash.hashTwice(SerializeUtils.serialize(this));
            if (!doverify(transaction, tobeSigned))
                return false;
        }
        return true;
    }


    /**
     * 验证交易信息
     //     * @param prevTxMap 前面多笔交易集合
     * @return
     */
    private boolean doverify(Transaction transaction, byte[] tobeVerifiedHash) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);

        // 逐条校验TXInput
        for (int i = 0; i < transaction.getInputs().size(); i++) {
            TXInput txInput = transaction.getInputs().get(i);
            // 获取交易输入的PreTxID对应的utxo
            List<TXOutput> preUTXO = utxoSet.getPreUTXO(txInput.getPreTXIdStr());
            TXOutput prevTxOutput = preUTXO.get(txInput.getTxOutputIndex());
            // 获取utxo中的pubkeyHash
            byte[] pubKeyHash = prevTxOutput.getPubKeyHash();
            // 获取当前txinput的ScriptSig(即<signature, pubKey>)
            byte[] signature = txInput.getSig();
            byte[] pubKey = txInput.getPubKey();
            // 1. 计算Hash160(Hash256(pubKey))
            byte[] hash = Sha256Hash.hash(pubKey);
            // 2. 做RIPEMD-160计算
            byte[] ripemdHashedKey = BtcAddressUtils.ripeMD160Hash(hash);
            // 3. 将ripemdHashedKey和pubKeyHash比较
            if (!Arrays.equals(ripemdHashedKey, pubKeyHash)) {
                return false;
            }
            // 4. 验证签名是否有效,使用ScriptKey中提供的公钥验证
            // 使用椭圆曲线 x,y 点去生成公钥Key
            BigInteger x = new BigInteger(1, Arrays.copyOfRange(pubKey, 1, 33));
            BigInteger y = new BigInteger(1, Arrays.copyOfRange(pubKey, 33, 65));
            ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);

            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(tobeVerifiedHash);
            if (!ecdsaVerify.verify(signature)) {
                return false;
            }
        }
        return true;
    }

}
