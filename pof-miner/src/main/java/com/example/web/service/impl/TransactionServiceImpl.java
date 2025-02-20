package com.example.web.service.impl;

import com.example.base.entities.transaction.*;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.base.utils.ByteUtils;
import com.example.net.base.Mempool;
import com.example.web.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final DBStore dbStore;
    private final Mempool mempool;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();
    private final Lock readLock = rwl.readLock();
    /**
     * 创建新coinbase交易
     * @param address 接收方地址，也就是成功挖矿的矿工地址
     * @return
     */
    @Override
    public Transaction createCoinbaseTransaction(String address, long height, int blockReward, int fee) {
        return Transaction.newCoinbaseTX(address, height, blockReward, fee);
    }

    /**
     * 创建新普通交易
     * @param fromAddress
     * @param toAddress
     * @param amount
     * @return
     * @throws Exception
     */
    @Override
    public Transaction createCommonTransaction(String fromAddress, String toAddress, int amount, int fee) throws Exception {
        // 获取钱包
        Wallet senderWallet = WalletUtils.getInstance().getWallet(fromAddress);
        byte[] pubKeyHash = senderWallet.getPubKeyHash();
        byte[] publicKey = senderWallet.getPublicKey();

        SpendableOutputResult result = findSpendableOutputs(pubKeyHash, amount + fee);
        int accumulated = result.getAccumulated();
        // 找零
        int change = accumulated - amount - fee;
        Map<String, List<Integer>> unspentOuts = result.getUnspentOuts();

        if (result.getUnspentOuts() == null) {
            logger.error("ERROR: Not enough funds ! accumulated=" + accumulated + ", amount=" + amount);
            throw new RuntimeException("ERROR: Not enough funds ! ");
        }
        Set<Map.Entry<String, List<Integer>>> entrySet = unspentOuts.entrySet();
        Transaction newTx = new Transaction();
        // 生成交易输入
        for (Map.Entry<String, List<Integer>> entry : entrySet) {
            String txIdStr = entry.getKey();
            List<Integer> outIds = entry.getValue();
            byte[] txId = ByteUtils.hexToBytes(txIdStr);
            for (Integer outIndex : outIds) {
                // 添加交易输入和签名
                newTx.addInput(new TXInput(txId, outIndex));
            }
        }
        // 生成交易输出，包括付款方和找零
        List<TXOutput> txOutputs = new ArrayList<>();
        txOutputs.add(TXOutput.newTXOutput(amount, toAddress));
        // 找零
        if (change > 0) {
            txOutputs.add(TXOutput.newTXOutput((accumulated - amount), fromAddress));
        }
        newTx.setFee(fee);
        newTx.addOutputs(txOutputs);
        newTx.setCreateTime(System.currentTimeMillis());
        newTx.setTxId(newTx.getTxId());
        // 签名
        newTx.sign(senderWallet.getPrivateKey(), publicKey);
        return newTx;
    }

    /**
     * 校验交易合法性
     * @param transaction
     * @return
     * @throws Exception
     */
    @Override
    public boolean verify(Transaction transaction) throws Exception {
        return transaction.verifyTransaction();
    }

    /**
     * 将交易以及utxo存储到本地数据库中
     * @param transaction
     */
    @Override
    public void txStore(Transaction transaction){
//        // 校验交易是否合法
//        if (!verify(transaction)) {
//            logger.info("交易不合法！");
//            return false;
//        }
        writeLock.lock();
        String txId = ByteUtils.bytesToHex(transaction.getTxId());
//        dbStore.put(WalletPrefix.TX_PREFIX.getPrefix() + txId, transaction);
        dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, transaction.getOutputs());
//        dbStore.close();
        writeLock.unlock();
        logger.info("已将交易{}存入数据库", txId);
    }

    /**
     * 存储到mempool中
     * @param transaction
     * @return
     */
    @Override
    public void storeMempool(Transaction transaction) {
        mempool.addTransaction(transaction);
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
        // TODO：后面看要不要手动close
//        dbStore.close();
        readLock.unlock();
        return new SpendableOutputResult(accumulated, thisUtxoMap);
    }

}
