package com.example.base.entities.block;

import com.example.base.entities.transaction.Transaction;
import com.example.base.crypto.CryptoUtils;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bitcoinj.base.Sha256Hash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jiafeimao
 * @date 2024年09月14日 21:34
 */

@Data
public class Block implements Serializable {
    public final static int BLOCK_MAX_SIZE = 1 * 1024 * 1024;

//    header
    private BlockHeader blockHeader;
    private List<Transaction> transactions = new ArrayList<>();

    public Block() {
    }

    public Block(BlockHeader blockHeader,
                 List<Transaction> transactions
                 ) {
        this.blockHeader = blockHeader;
        this.transactions = transactions;
    }

    @JsonIgnore
    public String getHash() {
        return ByteUtils.bytesToHex(GetHash());
    }

    private byte[] GetHash() {
        return Sha256Hash.hashTwice(SerializeUtils.serialize(this));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return blockHeader.equals(block.getBlockHeader()) && Objects.equals(transactions, block.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockHeader, transactions);
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockHeader=" + blockHeader.toString() +
                ", transactions=" + transactions.toString() +
                '}';
    }
}
