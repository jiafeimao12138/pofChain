package com.example.base.entities;

import com.example.base.utils.CryptoUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiafeimao
 * @date 2024年09月14日 21:34
 */

@Data
public class Block implements Serializable {
//    header
    public int nVersion;
    public String hashPreBlock;
    public String hashMerkleRoot;
    public long height = 1;
    public long nTime;
    public long nNonce;
    public List<Triple<String,List<Integer>,Boolean>> triples = new ArrayList<>();


    //    当前的hash目标
    public int nBits;

    public List<Transaction> transactions;

    public Block() {
        this.nVersion = 1;
        this.hashPreBlock = "";
        this.hashMerkleRoot = "";
        this.nTime = 0;
        this.nNonce = 0;
        this.nBits = 0;
        this.transactions = new ArrayList<>();
    }

    public Block(int nVersion,
                 String hashPreBlock,
                 String hashMerkleRoot,
                 long height,
                 long nTime,
                 long nNonce,
                 int nBits,
                 List<Transaction> transactions,
                 List<Triple<String,List<Integer>,Boolean>> triples) {
        this.nVersion = nVersion;
        this.hashPreBlock = hashPreBlock;
        this.hashMerkleRoot = hashMerkleRoot;
        this.height = height;
        this.nTime = nTime;
        this.nNonce = nNonce;
        this.nBits = nBits;
        this.transactions = transactions;
        this.triples = triples;
    }

    public String GetHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.hashPreBlock);
        sb.append(this.height);
        sb.append(this.nNonce);
        sb.append(this.nTime);
        sb.append(this.transactions);
        sb.append(this.triples);
        sb.append(this.hashMerkleRoot);
        sb.append(this.nVersion);
        String sha256 = CryptoUtils.SHA256(sb.toString());
        return sha256;
    }
}
