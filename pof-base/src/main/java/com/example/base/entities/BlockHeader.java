package com.example.base.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BlockHeader {
    private int nVersion = 0;
    private String hashPreBlock = "";
    private String hashMerkleRoot = "";
    private long height = 0;
    private long nTime = System.currentTimeMillis();
    private long nNonce = 0;
    //    当前的hash目标
    private int nBits = 0;
    private List<Payload> triples = new ArrayList<>();

    public BlockHeader() {

    }


    public BlockHeader(int nVersion, String hashPreBlock, String hashMerkleRoot, long height, long nTime, long nNonce, int nBits, List<Payload> triples) {
        this.nVersion = nVersion;
        this.hashPreBlock = hashPreBlock;
        this.hashMerkleRoot = hashMerkleRoot;
        this.height = height;
        this.nTime = nTime;
        this.nNonce = nNonce;
        this.nBits = nBits;
        this.triples = triples;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "nVersion=" + nVersion +
                ", hashPreBlock='" + hashPreBlock + '\'' +
                ", hashMerkleRoot='" + hashMerkleRoot + '\'' +
                ", height=" + height +
                ", nTime=" + nTime +
                ", nNonce=" + nNonce +
                ", nBits=" + nBits +
                ", triples=" + triples +
                '}';
    }
}
