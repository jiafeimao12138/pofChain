package com.example.base.entities.transaction;

import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.BtcAddressUtils;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
@NoArgsConstructor
@Slf4j
public class Transaction {
    private static final Integer OUT_PUT_VALUE = 210000;
    /**
     * 挖矿奖励
     */
    public final static int BLOCK_REWARD = 100;
    /**
     * 新路径奖励
     */
    public final static int NEW_PATH_REWARD = 5;
    /**
     * 交易的Hash
     */
    private transient byte[] txId;
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
    @NonNull private long createTime;
    /**
     * 交易费
     */
    private int fee = 0;

    public Transaction(byte[] txId, List<TXInput> inputs, List<TXOutput> outputs, long createTime) {
        this.txId = txId;
        this.inputs = inputs;
        this.outputs = outputs;
        this.createTime = createTime;
    }

    public TXInput addInput(TXInput input) {
        inputs.add(input);
        return input;
    }

    public void addInputs(List<TXInput> inputs) {
        this.inputs.addAll(inputs);
    }

    public TXOutput addOutput(TXOutput output) {
        outputs.add(output);
        return output;
    }

    public void addOutputs(List<TXOutput> outputs) {
        this.outputs.addAll(outputs);
    }

    public byte[] getTxId() {
        return Sha256Hash.hashTwice(SerializeUtils.serialize(this));
    }

    /**
     * 获取string类型的txId
     * @return
     */
    public String getTxIdStr() {
        return ByteUtils.bytesToHex(this.getTxId());
    }


    /**
     * 创建CoinBase交易
     *
     * @param toAddress   收账的钱包地址
     * @param blockHeight
     * @return
     */
    public static Transaction newCoinbaseTX(String toAddress, int blockHeight, int blockReward, int fee) {
        Transaction coinBaseTX = new Transaction();
        // 创建交易输入
        byte[] coinBaseData = ByteBuffer.allocate(4).putInt(blockHeight).array();
        TXInput txInput = TXInput.coinbaseInput(coinBaseData);
        coinBaseTX.addInput(txInput);

        // 创建交易输出
        TXOutput txOutput = TXOutput.newTXOutput(blockReward + fee, toAddress);
        coinBaseTX.addOutput(txOutput);
        coinBaseTX.setCreateTime(System.currentTimeMillis());
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
     * 计算交易信息的Hash值
     *
     * @return
     */
//    public byte[] hash() {
//        // 使用序列化的方式对Transaction对象进行深度复制
//        byte[] serializeBytes = SerializeUtils.serialize(this);
//        Transaction copyTx = (Transaction) SerializeUtils.unSerialize(serializeBytes);
//        copyTx.setTxId(new byte[]{});
//        return DigestUtils.sha256(SerializeUtils.serialize(copyTx));
//    }

    /**
     * @TODO 是否为 Coinbase 交易
     * @return
     */
    public boolean isCoinbase() {
        return false;
    }

    /**
     * 创建用于签名的交易数据副本，交易输入的 signature 和 pubKey 需要设置为null
     *
     * @return
     */
    @Deprecated
    public Transaction trimmedCopy() {
        List<TXInput> TXInputsCopyList = new ArrayList<>();
//        获取交易输入
        for (int i = 0; i < this.getInputs().size(); i++) {
            TXInput txInput = this.getInputs().get(i);
            TXInputsCopyList.add(new TXInput(txInput.getPreviousTXId(), txInput.getTxOutputIndex()));
        }

//       获取交易输出
        List<TXOutput> TXOutputCopyList = new ArrayList<>();
        for (int i = 0; i < this.getOutputs().size(); i++) {
            TXOutput txOutput = this.getOutputs().get(i);
            TXOutputCopyList.add(new TXOutput(txOutput.getValue(), txOutput.getPubKeyHash()));
        }

        return new Transaction(this.getTxId(), TXInputsCopyList, TXOutputCopyList, this.getCreateTime());
    }


    /**
     * 签名
     *
     * @param privateKey 私钥
     * @param pubKey 明文公钥
     */
    public void sign(BCECPrivateKey privateKey, byte[] pubKey) throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (this.isCoinbase()) {
            return;
        }

        Security.addProvider(new BouncyCastleProvider());
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);
        ecdsaSign.initSign(privateKey);

        for (int i = 0; i < this.getInputs().size(); i++) {
//            获取第i个交易输入
            TXInput txInputCopy = this.getInputs().get(i);
//            将ScriptSig用占位符替代
            txInputCopy.setScriptSig("OP_0".getBytes());
            this.setTxId(this.getTxId());

            // 得到要签名的数据(不包括TXId)
            byte[] tobeSigned = Sha256Hash.hashTwice(SerializeUtils.serialize(this));

            // 对整个交易信息进行签名
            ecdsaSign.update(tobeSigned);
            byte[] signature = ecdsaSign.sign();

            // 将整个交易数据的签名以及自己的公钥赋值给交易输入，因为交易输入需要包含整个交易信息的签名
            // 注意是将得到的签名赋值给原交易信息中的交易输入
            this.getInputs().get(i).setScriptSig(signature, pubKey);
        }
    }

    public boolean verifyTransaction() throws Exception {
        // coinbase 交易信息不需要签名，因为它不存在交易输入信息
        if (this.isCoinbase()) {
            return true;
        }
        for (int i = 0; i < this.getInputs().size(); i++) {
//            获取第i个交易输入
            TXInput txInputCopy = this.getInputs().get(i);
//            将ScriptSig用占位符替代
            txInputCopy.setScriptSig("OP_0".getBytes());
            this.setTxId(this.getTxId());

            // 得到要签名的数据(不包括TXId)
            byte[] tobeSigned = Sha256Hash.hashTwice(SerializeUtils.serialize(this));
            if (!doverify(tobeSigned))
                return false;
        }
        return true;
    }


    /**
     * 验证交易信息
//     * @param prevTxMap 前面多笔交易集合
     * @return
     */
    private boolean doverify(byte[] tobeVerifiedHash) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        ECParameterSpec ecParameters = ECNamedCurveTable.getParameterSpec("secp256k1");
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", BouncyCastleProvider.PROVIDER_NAME);
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", BouncyCastleProvider.PROVIDER_NAME);

        // 逐条校验TXInput
        for (int i = 0; i < this.getInputs().size(); i++) {
            TXInput txInput = this.getInputs().get(i);
            // 获取交易输入的PreTxID对应的utxo
            List<TXOutput> preUTXO = UTXOSet.getPreUTXO(txInput.getPreTXIdStr());
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