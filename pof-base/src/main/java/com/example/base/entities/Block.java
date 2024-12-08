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
import java.util.Objects;

/**
 * @author jiafeimao
 * @date 2024年09月14日 21:34
 */

@Data
public class Block implements Serializable {
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

    public String getHash() {
        return GetHash();
    }

    private String GetHash() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.blockHeader.toString());
        sb.append(this.transactions.toString());
        String sha256 = CryptoUtils.SHA256(sb.toString());
        return sha256;
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
}
