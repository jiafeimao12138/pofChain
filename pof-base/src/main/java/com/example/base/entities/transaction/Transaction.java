package com.example.base.entities.transaction;

import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.BtcAddressUtils;
import com.example.base.utils.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.base.Sha256Hash;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Transaction {
    private static final Integer OUT_PUT_VALUE = 210000;
    /**
     * 100 UB 创世奖励再加100UB
     */
    private static final Integer CREATION_VALUE = 1000000 * 100;
    /**
     * 交易的Hash
     */
    private byte[] txId;
    /**
     * 交易输入
     */
    private List<TXInput> inputs = new ArrayList<>();
    /**
     * 交易输出
     */
    private List<TXOutput> outputs = new ArrayList<>();
    /**
     * 创建日期
     */
    private long createTime = System.currentTimeMillis();

    public TXInput addInput(TXInput input) {
        input.setParent(this);
        inputs.add(input);
        return input;
    }

    public TXOutput addOutput(TXOutput output) {
        output.setParentTransaction(this);
        outputs.add(output);
        return output;
    }

    public byte[] getTxId() {
        return Sha256Hash.hashTwice(SerializeUtils.serialize(this));
    }

    /**
     * 创建CoinBase交易
     *
     * @param toAddress   收账的钱包地址
     * @param blockHeight
     * @return
     */
    public static Transaction newCoinbaseTX(String toAddress, int blockHeight) {
        Transaction coinBaseTX = new Transaction();
        // 创建交易输入
        byte[] coinBaseData = ByteBuffer.allocate(4).putInt(blockHeight).array();
        TXInput txInput = TXInput.coinbaseInput(coinBaseData);
        coinBaseTX.addInput(txInput);

        // 创建交易输出
        TXOutput txOutput = TXOutput.newTXOutput(coinBaseTX, 10 , toAddress);
        coinBaseTX.addOutput(txOutput);
        return coinBaseTX;
    }

    /**
     * 创建 交易  挖矿奖励
     *
     * @param to 收账的钱包地址
     * @return
     */
//    public static Transaction newRewardTX(String to, Blockchain blockchain) throws DecoderException {
//        //获取当前区块大小
//        Integer size = RocksDBUtils.getInstance().getChainstateBucket().size();
//        Integer multiple = size / OUT_PUT_VALUE + 1;
//        Integer value = CREATION_VALUE / multiple;
//        if (size == 0) {
//            //创世区块
//            value = value * 2;
//        }
//
//        String data = String.format("Reward to '%s'", to);
//        // 创建交易输入
//        TXInput txInput = new TXInput(new byte[]{}, -1, null, data.getBytes(StandardCharsets.UTF_8));
//
//        // 创建交易输出
//        TXOutput txOutput = TXOutput.newTXOutput(value, to);
//        // 创建交易
//        Transaction tx = new Transaction(null, new TXInput[]{txInput},
//                new TXOutput[]{txOutput}, System.currentTimeMillis());
//        // 设置交易ID
//        tx.setTxId(tx.hash());
//        return tx;
//    }

    /**
     * 从 from 向  to 支付一定的 amount 的金额
     *
     * @param fromAddress       支付钱包地址
     * @param toAddress    收款方地址
     * @param amount     交易金额
     * @return
     */
    public static Transaction newUTXOTransaction(String fromAddress, String toAddress, int amount) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(fromAddress);
        byte[] pubKey = senderWallet.getPublicKey();
        byte[] pubKeyHash = BtcAddressUtils.ripeMD160Hash(pubKey);

        SpendableOutputResult result = new UTXOSet(new RocksDBStore("/home/wj/wallet")).findSpendableOutputs(pubKeyHash, amount);
        int accumulated = result.getAccumulated();
        // 找零
        int change = accumulated - amount;
        Map<String, List<Integer>> unspentOuts = result.getUnspentOuts();

        if (result.getUnspentOuts() == null) {
            log.error("ERROR: Not enough funds ! accumulated=" + accumulated + ", amount=" + amount);
            throw new RuntimeException("ERROR: Not enough funds ! ");
        }
        Set<Map.Entry<String, List<Integer>>> entrySet = unspentOuts.entrySet();
        Transaction newTx = new Transaction();
        // 生成交易输入
        for (Map.Entry<String, List<Integer>> entry : entrySet) {
            String txIdStr = entry.getKey();
            List<Integer> outIds = entry.getValue();
            byte[] txId = Hex.decodeHex(txIdStr);
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
        newTx.setTxId(newTx.getTxId());
        // 签名
        newTx.sign(senderWallet.getPrivateKey());
        return newTx;
    }

    /**
     * 计算交易信息的Hash值
     *
     * @return
     */
//    public byte[] hash() {
//        // 使用序列化的方式对Transaction对象进行深度复制
//        byte[] serializeBytes = SerializeUtils.serialize(this);
//        Transaction copyTx = (Transaction) SerializeUtils.deserialize(serializeBytes);
//        copyTx.setTxId(new byte[]{});
//        return DigestUtils.sha256(SerializeUtils.serialize(copyTx));
//    }

    /**
     * 是否为 Coinbase 交易
     *
     * @return
     */
    public boolean isCoinbase() {
        return this.getInputs().size() == 0;
    }

    /**
     * 创建用于签名的交易数据副本，交易输入的 signature 和 pubKey 需要设置为null
     *
     * @return
     */
    public Transaction trimmedCopy() {
        TXInput[] tmpTXInputs = new TXInput[this.getInputs().size()];
        for (int i = 0; i < this.getInputs().size(); i++) {
            TXInput txInput = this.getInputs().get(i);
            tmpTXInputs[i] = new TXInput(txInput., txInput.getTxOutputIndex(), null, null);
        }

        TXOutput[] tmpTXOutputs = new TXOutput[this.getOutputs().size()];
        for (int i = 0; i < this.getOutputs().size(); i++) {
            TXOutput txOutput = this.getOutputs().get(i);
            tmpTXOutputs[i] = new TXOutput(txOutput.getValue(), txOutput.getPubKeyHash());
        }

        return new Transaction(this.getTxId(), tmpTXInputs, tmpTXOutputs, this.getCreateTime());
    }


    /**
     * 签名
     *
     * @param privateKey 私钥
     */
    public void sign(BCECPrivateKey privateKey) throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (this.isCoinbase()) {
            return;
        }
        // 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
        for (TXInput txInput : this.getInputs()) {
            if (prevTxMap.get(Hex.encodeHexString(txInput.getParent().getTxId())) == null) {
                throw new RuntimeException("ERROR: Previous transaction is not correct");
            }
        }

        // 创建用于签名的交易信息的副本
        Transaction txCopy = this.trimmedCopy();

        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);

        for (int i = 0; i < txCopy.getInputs().size(); i++) {
            TXInput txInputCopy = txCopy.getInputs().get(i);
            // 获取交易输入TxID对应的交易数据
            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInputCopy.getTxId()));
            // 获取交易输入所对应的上一笔交易中的交易输出
            TXOutput prevTxOutput = prevTx.getOutputs()[txInputCopy.getTxOutputIndex()];
            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
            txInputCopy.setSignature(null);
            // 得到要签名的数据，即交易ID
//            txCopy.setTxId(txCopy.hash());
            txInputCopy.setPubKey(null);

            // 对整个交易信息仅进行签名，即对交易ID进行签名
            ecdsaSign.update(txCopy.getTxId());
            byte[] signature = ecdsaSign.sign();

            // 将整个交易数据的签名赋值给交易输入，因为交易输入需要包含整个交易信息的签名
            // 注意是将得到的签名赋值给原交易信息中的交易输入
            this.getInputs()[i].setSignature(signature);
        }

    }

    public String calculateSignature(String sigKey, String pubKey, TXInput txInput) {

    }


    /**
     * 验证交易信息
//     * @param prevTxMap 前面多笔交易集合
     * @return
     */
//    public boolean verify(Map<String, Transaction> prevTxMap) throws Exception {
//        // coinbase 交易信息不需要签名，也就无需验证
//        if (this.isCoinbase()) {
//            return true;
//        }
//
//        // 再次验证一下交易信息中的交易输入是否正确，也就是能否查找对应的交易数据
//        for (TXInput txInput : this.getInputs()) {
//            if (prevTxMap.get(Hex.encodeHexString(txInput.getTxId())) == null) {
//                throw new RuntimeException("ERROR: Previous transaction is not correct");
//            }
//        }
//
//        // 创建用于签名验证的交易信息的副本
//        Transaction txCopy = this.trimmedCopy();
//
//        Security.addProvider(new BouncyCastleProvider());
//        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
//        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
//
//        for (int i = 0; i < this.getInputs().length; i++) {
//            TXInput txInput = this.getInputs()[i];
//            // 获取交易输入TxID对应的交易数据
//            Transaction prevTx = prevTxMap.get(Hex.encodeHexString(txInput.getTxId()));
//            // 获取交易输入所对应的上一笔交易中的交易输出
//            TXOutput prevTxOutput = prevTx.getOutputs()[txInput.getTxOutputIndex()];
//
//            TXInput txInputCopy = txCopy.getInputs()[i];
//            txInputCopy.setSignature(null);
//            txInputCopy.setPubKey(prevTxOutput.getPubKeyHash());
//            // 得到要签名的数据，即交易ID
////            txCopy.setTxId(txCopy.hash());
//            txInputCopy.setPubKey(null);
//
//            // 使用椭圆曲线 x,y 点去生成公钥Key
//            BigInteger x = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 1, 33));
//            BigInteger y = new BigInteger(1, Arrays.copyOfRange(txInput.getPubKey(), 33, 65));
//            ECPoint ecPoint = ecParameters.getCurve().createPoint(x, y);
//
//            ECPublicKeySpec keySpec = new ECPublicKeySpec(ecPoint, ecParameters);
//            PublicKey publicKey = keyFactory.generatePublic(keySpec);
//            ecdsaVerify.initVerify(publicKey);
//            ecdsaVerify.update(txCopy.getTxId());
//            if (!ecdsaVerify.verify(txInput.getSignature())) {
//                return false;
//            }
//        }
//        return true;
//    }


    @Override
    public String toString() {
        return "Transaction{\n" +
                "txId=" + getTxId() +
                "\n, inputs=" + inputs +
                "\n, outputs=" + outputs +
                "\n, createTime=" + createTime +
                '}';
    }
}