package com.example.web.service;

import com.example.base.entities.transaction.Transaction;

public interface TransactionService {
    // 创建新coinbase交易
    Transaction createCoinbaseTransaction(String address, long height, int blockReward, int fee);
    // 创建新普通交易
    Transaction createCommonTransaction(String fromAddress, String toAddress, int amount, int fee) throws Exception;
    // 创建supplier奖励交易
    Transaction createFuzzingRewardTransaction(String fromAddress, String toAddress, int amount);
    // 校验交易
    boolean verify(Transaction transaction) throws Exception;
    // 存入本地数据库
    void txStore(Transaction transaction) throws Exception;
    // 存入mempool
    void storeMempool(Transaction transaction);
}
