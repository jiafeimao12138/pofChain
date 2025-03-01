package com.example.base.entities.transaction;

import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.base.Sha256Hash;
import org.springframework.util.CollectionUtils;
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
     * 判断是否为coinbase交易
     * @return
     */
    public boolean isCoinbase() {
        List<TXInput> inputsList = this.getInputs();
        if (inputsList.size() > 1 || CollectionUtils.isEmpty(inputsList))
            return false;
        TXInput txInput = inputsList.get(0);
        return txInput.getPreviousTXId().length == 8;
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