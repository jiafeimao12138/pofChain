package com.example.base.entities.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiafeimao
 * @date 2024年09月14日 22:00
 */
public class Transaction {
    // 版本号
    int versionNo = 1;
    // 输入的数量
    int inCount = 1;
    // 输入列表
    List<TxIn> txInList = new ArrayList<>();
    // 输出的数量
    int outCount = 0;
    // 输出列表
    List<TxOut> txOutList = new ArrayList<>();
    // 如果序列号非0，且小于 0xFFFFFFFF，指区块高度或时间戳，它表示这笔交易在何时可以被确认
    int nLockTime = 0;

    /** How many bytes a transaction can be before it won't be relayed anymore. Currently 100kb. */
    public static final int MAX_STANDARD_TX_SIZE = 100_000;


}
