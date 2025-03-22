package com.example.base.entities.transaction;

import com.example.base.utils.Base58Check;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@NoArgsConstructor
public class TXOutput {

    /**
     * 数值
     */
    private int value;
    /**
     * 在P2PKH中，是目标地址的公钥Hash,scriptPubKey，20字节
     */
    private byte[] pubKeyHash;

    public TXOutput(int value, byte[] pubKeyHash) {
        this.value = value;
        this.pubKeyHash = pubKeyHash;
    }


    /**
     * 创建交易输出
     * 把address还原为pubKeyHash
     * @param value
     * @param address
     * @return
     */
    public static TXOutput newTXOutput(int value, String address) {
        if (address == "000000")
            return new TXOutput(value, new byte[0]);
        // 反向转化为 byte 数组
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        return new TXOutput(value, pubKeyHash);
    }

    /**
     * 检查交易输出是否能够使用指定的公钥
     *
     * @param pubKeyHash
     * @return
     */
    public boolean isLockedWithKey(byte[] pubKeyHash) {
        return Arrays.equals(this.getPubKeyHash(), pubKeyHash);
    }

    @Override
    public String toString() {
        return "TXOutput{" +
                "value=" + value +
                ", pubKeyHash=" + Arrays.toString(pubKeyHash) +
                '}';
    }
}