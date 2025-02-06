package com.example.base.entities.transaction;

import com.example.base.entities.block.Block;
import com.example.base.store.DBStore;
import com.example.base.store.RocksDBStore;
import com.example.base.store.WalletPrefix;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Maps;

import com.example.base.utils.SerializeUtils;
import org.rocksdb.RocksDB;
import org.springframework.util.CollectionUtils;


public class UTXOSet {

    private final DBStore dbStore;
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static Lock readLock = rwl.readLock();

    public UTXOSet(DBStore dbStore) {
        this.dbStore = dbStore;
    }
    /**
     * 寻找能够花费的交易
     *
     * @param pubKeyHash 钱包公钥Hash
     * @param amount     花费金额
     */
    public SpendableOutputResult findSpendableOutputs(byte[] pubKeyHash, int amount) {
        readLock.lock();
        Map<String, List<Integer>> thisUtxoMap = new HashMap<>();
        int accumulated = 0;
        Map<String, Object> utxoMap = dbStore.searchforWallet(WalletPrefix.UTXO_PREFIX.getPrefix());
        for (Map.Entry<String, Object> entry : utxoMap.entrySet()) {
            String TxId = entry.getKey();
            List<TXOutput> txOutputList = (List<TXOutput>)entry.getValue();
            for (int i = 0; i < txOutputList.size(); i++) {
                if (txOutputList.get(i).isLockedWithKey(pubKeyHash) && accumulated < amount) {
                    accumulated += txOutputList.get(i).getValue();
//                    把该UTXO加入到本次需要花费的列表中
                    List<Integer> indexList = thisUtxoMap.get(TxId);
                    if (CollectionUtils.isEmpty(indexList)) {
                        indexList = new ArrayList<>();
                    }
                    indexList.add(i);
                    thisUtxoMap.put(TxId, indexList);
                } else if (accumulated >= amount) {
                    break;
                }
            }
        }
        // 如果所有UTXO都不够
        if (amount > accumulated) {
            return new SpendableOutputResult(accumulated, null);
        }
        dbStore.close();
        readLock.unlock();
        return new SpendableOutputResult(accumulated, thisUtxoMap);
    }

    /**
     * 查找钱包地址对应的所有UTXO
     *
     * @param pubKeyHash 钱包公钥Hash
     * @return
     */
    public static List<TXOutput> findUTXOs(byte[] pubKeyHash) {
        DBStore dbStore = new RocksDBStore("/home/wj/datastore/wallet");
        List<TXOutput> utxos = new ArrayList<>();
        List<List<TXOutput>> utxoList = dbStore.search(WalletPrefix.UTXO_PREFIX.getPrefix());
        System.out.println("utxoList size: " + utxoList.size());
        for (List<TXOutput> txOutputs : utxoList) {
            for (TXOutput txOutput : txOutputs) {
                System.out.println(txOutput.toString());
                if (txOutput.isLockedWithKey(pubKeyHash)) {
                    utxos.add(txOutput);
                }
            }
        }
        dbStore.close();
        return utxos;
    }

    /**
     * 通过TXId索引该Transaction的TxOutputs
     * @param TXId
     * @return
     */
    public static List<TXOutput> getPreUTXO(String TXId) {
        readLock.lock();
        List<TXOutput> txOutputs = new ArrayList<>();
        DBStore dbStore = new RocksDBStore("/home/wj/datastore/wallet");
        Optional<Object> o = dbStore.get(WalletPrefix.UTXO_PREFIX.getPrefix() + TXId);
        if (o.isPresent()) {
            txOutputs = (List<TXOutput>)o.get();
        }
        dbStore.close();
        readLock.unlock();
        return txOutputs;
    }
//
//    /**
//     * 重建 UTXO 池索引
//     */
//    @Synchronized
//    public void reIndex() {
//        log.info("Start to reIndex UTXO set !");
//        RocksDBUtils.getInstance().cleanChainStateBucket();
//        Map<String, TXOutput[]> allUTXOs = blockchain.findAllUTXOs();
//        Map<String, byte[]> allUTXOBytes = Maps.newHashMap();
//        for (Map.Entry<String, TXOutput[]> entry : allUTXOs.entrySet()) {
//            allUTXOBytes.put(entry.getKey(), SerializeUtils.serialize(entry.getValue()));
//        }
//        RocksDBUtils.getInstance().initAllUTXOs(allUTXOBytes);
//        log.info("ReIndex UTXO set finished ! ");
//    }
//
//    /**
//     * 更新UTXO池
//     * <p>
//     * 当一个新的区块产生时，需要去做两件事情：
//     * 1）从UTXO池中移除花费掉了的交易输出；
//     * 2）保存新的未花费交易输出；
//     *
//     * @param tipBlock 最新的区块
//     */
//    @Synchronized
//    public void update(Block tipBlock) {
//        if (tipBlock == null) {
//            log.error("Fail to update UTXO set ! tipBlock is null !");
//            throw new RuntimeException("Fail to update UTXO set ! ");
//        }
//        for (Transaction transaction : tipBlock.getTransactions()) {
//
//            // 根据交易输入排查出剩余未被使用的交易输出
//            if (!transaction.isCoinbase()) {
//                for (TXInput txInput : transaction.getInputs()) {
//                    // 余下未被使用的交易输出
//                    TXOutput[] remainderUTXOs = {};
//                    String txId = Hex.encodeHexString(txInput.getTxId());
//                    TXOutput[] txOutputs = RocksDBUtils.getInstance().getUTXOs(txId);
//
//                    if (txOutputs == null) {
//                        continue;
//                    }
//
//                    for (int outIndex = 0; outIndex < txOutputs.length; outIndex++) {
//                        if (outIndex != txInput.getTxOutputIndex()) {
//                            remainderUTXOs = ArrayUtils.add(remainderUTXOs, txOutputs[outIndex]);
//                        }
//                    }
//
//                    // 没有剩余则删除，否则更新
//                    if (remainderUTXOs.length == 0) {
//                        RocksDBUtils.getInstance().deleteUTXOs(txId);
//                    } else {
//                        RocksDBUtils.getInstance().putUTXOs(txId, remainderUTXOs);
//                    }
//                }
//            }
//
//            // 新的交易输出保存到DB中
//            TXOutput[] txOutputs = transaction.getOutputs();
//            String txId = Hex.encodeHexString(transaction.getTxId());
//            RocksDBUtils.getInstance().putUTXOs(txId, txOutputs);
//        }
//
//    }

}
