package com.example.net.base;

import com.example.base.entities.transaction.Transaction;
import com.example.base.utils.BlockUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

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

    /**
     * 按交易费率（sat/vByte）降序排序
     * @return
     */
    public Map<Transaction, Double> txOrderByFees() {
        List<Transaction> transactionList = new ArrayList<>(transactions.values());
        HashMap<Transaction, Double> map = new HashMap<>();
        for (Transaction tx : transactionList) {
            // 计算交易费率
            // 获取交易大小
            int size = BlockUtils.getTransactionSize(tx);
            // 获取交易费
            Double feeRate = (double) tx.getFee() / size;
            map.put(tx, feeRate);
        }
        LinkedHashMap<Transaction, Double> sortedMap = map.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())) // 倒序排序
                .collect(Collectors.toMap(
                        Map.Entry::getKey,    // 获取键
                        Map.Entry::getValue,  // 获取值
                        (e1, e2) -> e1,       // 合并函数
                        LinkedHashMap::new    // 保持插入顺序
                ));
        return sortedMap;
    }

    /**
     * 计算大小
     * @param obj
     * @return
     */
    public int getObjectSize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray().length;
        } catch (IOException e) {
            throw new RuntimeException("计算对象大小失败", e);
        }
    }

}
