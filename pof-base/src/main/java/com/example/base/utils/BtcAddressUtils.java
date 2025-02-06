package com.example.base.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;

public class BtcAddressUtils {
    /**
     * 双重Hash
     *
     * @param data
     * @return
     */
    public static byte[] doubleHash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }

    /**
     * 计算公钥的 RIPEMD160值
     *
     * @return ipeMD160Hash(input)
     */
    public static byte[] ripeMD160Hash(byte[] input) {
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(input, 0, input.length);
        byte[] output = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(output, 0);
        return output;
    }

    /**
     * 生成公钥的校验码
     *
     * @param payload
     * @return
     */
    public static byte[] checksum(byte[] payload) {
        return Arrays.copyOfRange(payload, 0, 4);
    }

}
