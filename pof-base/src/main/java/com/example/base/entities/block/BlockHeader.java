package com.example.base.entities.block;

import com.example.base.entities.Payload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class BlockHeader {
    private int nVersion = 0;
    private String hashPreBlock = "";
    private String hashMerkleRoot = "";
    private int height = 0;
    private long nTime = System.currentTimeMillis();
    private long nNonce = 0;
    //    当前的hash目标
    private int nBits = 0;
    private List<Payload> triples = new ArrayList<>();

    public BlockHeader() {

    }


    public BlockHeader(int nVersion,
                       String hashPreBlock,
                       String hashMerkleRoot,
                       int height,
                       long nTime,
                       long nNonce,
                       int nBits,
                       List<Payload> triples) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockHeader)) return false;
        BlockHeader that = (BlockHeader) o;
        return nVersion == that.nVersion &&
                height == that.height &&
                nTime == that.nTime &&
                nNonce == that.nNonce &&
                nBits == that.nBits &&
                Objects.equals(hashPreBlock, that.hashPreBlock) &&
                Objects.equals(hashMerkleRoot, that.hashMerkleRoot) &&
                Objects.equals(triples, that.triples);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nVersion, hashPreBlock, hashMerkleRoot, height, nTime, nNonce, nBits, triples);
    }
}
