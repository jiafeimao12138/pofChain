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
}
