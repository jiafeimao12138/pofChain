package com.example.base.entities.transaction;

import com.example.base.utils.BtcAddressUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

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
    private String txIdStr;
    /**
     * 交易输出索引
     * 定义了它所指向的UTXO在上一笔交易中交易输出数组的位置。
     */
    private int txOutputIndex;
    /**
     * 私钥签名+公钥组成ScriptSig，用于解锁该UTXO使得可以消费这笔钱
     */

    private byte[] scriptBytes;


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
     * coinBase input, no UTXO and scriptSig
     */
    public TXInput() {
    }

    /**
     * Creates an input that connects to nothing - used only in creation of coinbase transactions.
     * @return
     */
    public static TXInput coinbaseInput(byte[] scriptBytes) {
        return new TXInput(scriptBytes);
    }

    public void setScriptBytes(byte[] scriptBytes) {
        this.scriptBytes = scriptBytes;
    }


    public String getTxIdStr() {
        if(previousTXId!=null){
            txIdStr = Arrays.toString(previousTXId);
        }
        return txIdStr;
    }



    @Override
    public String toString() {
        return "TXInput{" +
                ", previousTXId=" + Arrays.toString(previousTXId) +
                ", txIdStr='" + txIdStr + '\'' +
                ", txOutputIndex=" + txOutputIndex +
                ", scriptBytes=" + Arrays.toString(scriptBytes) +
                '}';
    }

    public static void main(String[] args) {
        byte[] pubKey = {84, 104, 101, 32, 84, 105, 109, 101, 115, 32, 50, 48, 50, 50, 45, 48, 54, 45, 48, 49, 32, 49, 55, 58, 49, 48, 58, 48, 55, 32, 51, 54, 52, 32, 67, 104, 97, 110, 99, 101, 108, 108, 111, 114, 32, 111, 110, 32, 98, 114, 105, 110, 107, 32, 111, 102, 32, 115, 101, 99, 111, 110, 100, 32, 98, 97, 105, 108, 111, 117, 116, 32, 102, 111, 114, 32, 98, 97, 110, 107, 115};
        System.out.println(new String(pubKey));
    }
}
