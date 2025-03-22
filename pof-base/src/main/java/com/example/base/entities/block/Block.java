package com.example.base.entities.block;

import com.example.base.entities.Payload;
import com.example.base.entities.transaction.Transaction;
import com.example.base.crypto.CryptoUtils;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.MerkleTree;
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
    private String blockHash;
    private BlockHeader blockHeader;
    private List<Transaction> transactions = new ArrayList<>();
    private List<Payload> payloads = new ArrayList<>();

    public Block() {
    }

    public Block(BlockHeader blockHeader,
                 List<Transaction> transactions,
                 List<Payload> payloads
                 ) {

        this.blockHeader = blockHeader;
        this.blockHeader.setHashMerkleRoot(ByteUtils.bytesToHex(calculateMerkleRoot(transactions)));
        this.transactions = transactions;
        this.payloads = payloads;
    }

    public byte[] calculateMerkleRoot(List<Transaction> transactions) {
        byte[][] txIdArrays = new byte[transactions.size()][];
        for (int i = 0; i < this.getTransactions().size(); i++) {
            txIdArrays[i] = Sha256Hash.hash(SerializeUtils.serialize(this.getTransactions().get(i)));
        }
        return new MerkleTree(txIdArrays).getRoot().getHash();
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
        return Objects.equals(blockHash, block.blockHash) && Objects.equals(blockHeader, block.blockHeader) && Objects.equals(transactions, block.transactions) && Objects.equals(payloads, block.payloads);
    }


    @Override
    public String toString() {
        return "Block{" +
                "blockHash=" + blockHash.toString() +
                "blockHeader=" + blockHeader.toString() +
                ", transactions=" + transactions.toString() +
                ", payloads=" + payloads +
                '}';
    }
}
