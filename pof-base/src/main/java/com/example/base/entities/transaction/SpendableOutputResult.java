package com.example.base.entities.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendableOutputResult {

    /**
     * 交易时的支付金额
     */
    private int accumulated;
    /**
     * 未花费的交易, key为TxId，value为utxo在该交易的TXoutputs里的index
     */
    private Map<String, List<Integer>> unspentOuts;

}
