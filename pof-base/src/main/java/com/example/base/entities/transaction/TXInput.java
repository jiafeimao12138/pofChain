package com.example.base.entities.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 交易输入
 *
 * @author chenhx
 * @version TXInput.java, v 0.1 2018-10-15 下午 6:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXInput {

    /**
     * 交易Id的hash值
     * 包含了它所指向的UTXO的交易的Hash值。
     */
    private byte[] previousTXId;
    /**
     * 交易输出索引
     * 定义了它所指向的UTXO在上一笔交易中交易输出数组的位置。
     */
    private int txOutputIndex;
    /**
     * 私钥签名+公钥组成ScriptSig，用于解锁该UTXO使得可以消费这笔钱
     */

    private byte[] scriptSig = null;

    public TXInput(byte[] previousTXId) {
        this.previousTXId = previousTXId;
    }

    /**
     * common input
     * @param previousTXId
     * @param txOutputIndex
     */
    public TXInput(byte[] previousTXId, int txOutputIndex) {
        this.previousTXId = previousTXId;
        this.txOutputIndex = txOutputIndex;
    }

    /**
     * Creates an input that connects to nothing - used only in creation of coinbase transactions.
     * @return
     */
    public static TXInput coinbaseInput(byte[] coinBaseData) {
        return new TXInput(coinBaseData);
    }


    /**
     * 只存储publicKey，用于签名
     * @param publicKey
     */
    public void setScriptSig(byte[] publicKey) {
        this.scriptSig = publicKey;
    }

    /**
     * real ScriptSig
     * @param sig
     * @param pubKey
     */
    public void setScriptSig(byte[] sig, byte[] pubKey) {
        int totalLength = 4 + sig.length + 4 + pubKey.length;
        this.scriptSig = new byte[totalLength];
        ByteBuffer buffer = ByteBuffer.wrap(scriptSig);
        buffer.putInt(sig.length);
        buffer.put(sig);
        buffer.putInt(pubKey.length);
        buffer.put(pubKey);
    }

    /**
     * 提取签名
     * @return
     */
    public byte[] getSig() {
        ByteBuffer buffer = ByteBuffer.wrap(this.scriptSig);
        // 读取第一组数据
        int length1 = buffer.getInt(); // 读取长度
        byte[] retrievedData1 = new byte[length1];
        buffer.get(retrievedData1); // 读取数据

        return retrievedData1;
    }

    /**
     * 提取公钥
     * @return
     */
    public byte[] getPubKey() {
        ByteBuffer buffer = ByteBuffer.wrap(this.scriptSig);
        int length1 = buffer.getInt(); // 读取长度
        byte[] retrievedData1 = new byte[length1];
        buffer.get(retrievedData1); // 读取数据

        // 读取第二组数据
        int length2 = buffer.getInt(); // 读取长度
        byte[] retrievedData2 = new byte[length2];
        buffer.get(retrievedData2); // 读取数据
        return retrievedData2;
    }

    /**
     *
     * @return
     */
    public String getPreTXIdStr() {
        String preTXId = Hex.encodeHexString(this.getPreviousTXId());
        return preTXId;
    }

    @Override
    public String toString() {
        return "TXInput{" +
                ", previousTXId=" + Arrays.toString(previousTXId) +
                ", txOutputIndex=" + txOutputIndex +
                ", scriptSig=" + Arrays.toString(scriptSig) +
                '}';
    }

    public static void main(String[] args) {
        byte[] pubKey = {84, 104, 101, 32, 84, 105, 109, 101, 115, 32, 50, 48, 50, 50, 45, 48, 54, 45, 48, 49, 32, 49, 55, 58, 49, 48, 58, 48, 55, 32, 51, 54, 52, 32, 67, 104, 97, 110, 99, 101, 108, 108, 111, 114, 32, 111, 110, 32, 98, 114, 105, 110, 107, 32, 111, 102, 32, 115, 101, 99, 111, 110, 100, 32, 98, 97, 105, 108, 111, 117, 116, 32, 102, 111, 114, 32, 98, 97, 110, 107, 115};
        System.out.println(new String(pubKey));
        // 要存储的两组数据
        byte[] data1 = "Hello".getBytes();
        byte[] data2 = "World".getBytes();
        TXInput txInput = new TXInput(null);
        txInput.setScriptSig(data1, data2);
        System.out.println(new String(txInput.getSig()));
        System.out.println(new String(txInput.getPubKey()));

    }
}
