package com.example.base.entities.transaction;

public class TxIn {
//    前一个交易的TXID
    String prevoutHash;
//    引用的交易中的具体输出的索引值
    int index;
//    当一个UTXO作为一个交易的输入被花费时，需要提供script的前一半
    String scriptSig;
//    用于在支付通道中迭代输入。当 `nSequence` 等于 `0xFFFFFFFF` 时，输入被视为最终状态
    int nSequence;
}
