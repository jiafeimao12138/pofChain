package com.example.base.crypto;

/**
 * @author jiafeimao
 * @date 2024年09月14日 22:10
 */

import java.security.MessageDigest;
import com.example.base.utils.Sha256Hash;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;


/**
 * 密码学工具类
 */
public class CryptoUtils {

    /**
     * SHA256散列函数
     * @param str
     * @return
     */
    public static String SHA256(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            System.out.println("getSHA256 is error" + e.getMessage());
        }
        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        String temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                builder.append("0");
            }
            builder.append(temp);
        }
        return builder.toString();
    }

    /**
     * Calculate RIPEMD160(SHA256(input)). This is used in Address calculations.
     * @param input bytes to hash
     * @return RIPEMD160(SHA256(input))
     */
    public static byte[] sha256hash160(byte[] input) {
        byte[] sha256 = Sha256Hash.hash(input);
        return digestRipeMd160(sha256);
    }

    /**
     * Calculate RIPEMD160(input).
     * @param input bytes to hash
     * @return RIPEMD160(input)
     */
    public static byte[] digestRipeMd160(byte[] input) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(input, 0, input.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return ripmemdHash;
    }
}

