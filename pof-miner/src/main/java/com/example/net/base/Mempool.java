package com.example.net.base;

import com.example.base.entities.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交易池
 */
public class Mempool {

    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>(); // 线程安全存储
    /** 添加交易到 Mempool */
    public void addTransaction(Transaction tx) {
        transactions.put(tx.getTxIdStr(), tx);
    }

    /** 获取 Mempool 中所有交易 */
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions.values());
    }

    /** 从 Mempool 删除交易（当交易被打包进区块后调用） */
    public void removeTransaction(String txIdStr) {
        transactions.remove(txIdStr);
    }

    /** Mempool 交易数量 */
    public int size() {
        return transactions.size();
    }

}
