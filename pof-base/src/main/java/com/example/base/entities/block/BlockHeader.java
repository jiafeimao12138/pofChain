package com.example.base.entities.block;

import com.example.base.entities.Payload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class BlockHeader {
    private String hashPreBlock = "";
    private String hashMerkleRoot = "";
    private long height = 0l;
    private long nTime = System.currentTimeMillis();
    private long nNonce = 0l;
    //    当前的hash目标
    private int nBits = 0;


    public BlockHeader() {

    }

    public BlockHeader(
                       String hashPreBlock,
                       String hashMerkleRoot,
                       long height,
                       long nTime,
                       long nNonce,
                       int nBits,
                       List<Payload> triples) {
        this.hashPreBlock = hashPreBlock;
        this.hashMerkleRoot = hashMerkleRoot;
        this.height = height;
        this.nTime = nTime;
        this.nNonce = nNonce;
        this.nBits = nBits;
    }

    @Override
    public String toString() {
        return "BlockHeader{" +
                "hashPreBlock='" + hashPreBlock + '\'' +
                ", hashMerkleRoot='" + hashMerkleRoot + '\'' +
                ", height=" + height +
                ", nTime=" + nTime +
                ", nNonce=" + nNonce +
                ", nBits=" + nBits +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockHeader)) return false;
        BlockHeader that = (BlockHeader) o;
        return
                height == that.height &&
                nTime == that.nTime &&
                nNonce == that.nNonce &&
                nBits == that.nBits &&
                Objects.equals(hashPreBlock, that.hashPreBlock) &&
                Objects.equals(hashMerkleRoot, that.hashMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashPreBlock, hashMerkleRoot, height, nTime, nNonce, nBits);
    }
}
