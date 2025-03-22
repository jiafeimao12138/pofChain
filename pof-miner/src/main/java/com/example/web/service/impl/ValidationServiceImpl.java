package com.example.web.service.impl;

import com.example.base.entities.block.Block;
import com.example.base.entities.transaction.Transaction;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.exception.TransactionNotExistException;
import com.example.net.base.Mempool;
import com.example.web.service.ChainService;
import com.example.web.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();
    private final ChainService chainService;
    private final DBStore dbStore;
    private final Mempool mempool;

    private final int MIN_BLOCKS_TO_KEEP = 10;

    @Override
    public boolean processNewBlock(Block block) {
        return false;
    }

    // 处理接收到的新区块
    @Override
    public boolean processNewMinedBlock(Block block) {
        boolean ret = checkBlock(block);
        // 经过校验后进行后续步骤
        if (ret) {
            storeBlock(block);
            return true;
        } else {
            return false;
        }
    }

    // fuzzer和observer校验新区块合法性
    @Override
    public boolean checkBlock(Block block) {
        long height = block.getBlockHeader().getHeight();
        // 如果这个高度的区块已经存在了, 忽略
        if (chainService.getBlockByHeight(height) != null){
            return true;
        } else {
            // 校验这个新区块
            // 如果该区块高度过高，舍弃
            if (height > chainService.getLocalLatestBlock().getBlockHeader().getHeight() + MIN_BLOCKS_TO_KEEP) {
                return false;
            }
            String preHash = block.getBlockHeader().getHashPreBlock();
            Block preBlock = chainService.getBlockByHeight(block.getBlockHeader().getHeight() - 1);
            // 前一个区块不存在，说明接收到的这个新区块要么不合法，要么超前
            if (preBlock == null) {
                return false;
            }
            // 前一个区块存在，校验preHash是否等于新hash
            if (!StringUtils.equals(preHash, preBlock.getBlockHash())) {
                return false;
            }
        }
        // 交易校验失败
        if (!checkTransactions(block)) {
            return false;
        }
        return true;
    }

    // 校验交易合法性
    @Override
    public boolean checkTransactions(Block block) {
        return true;
    }

    @Override
    public boolean storeBlock(Block block) {
        writeLock.lock();
        try {
            if(chainService.getBlockByHash(block.getHash()) != null) {
                // 已有该块，忽略
                return true;
            }
            if (!dbStore.put(BlockPrefix.BLOCK_HEIGHT_PREFIX.getPrefix() + block.getBlockHeader().getHeight(), block)){
                return false;
            }
            if (!dbStore.put(BlockPrefix.BLOCK_HASH_PREFIX.getPrefix() + block.getBlockHash(), block)){
                return false;
            }
            if (!dbStore.put(BlockPrefix.HEIGHT.getPrefix(), block.getBlockHeader().getHeight())) {
                return false;
            }
            return true;
        }finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean storeChainHeight(long height) {
        writeLock.lock();
        try {
            if (!dbStore.put(BlockPrefix.CHAIN_HEIGHT.getPrefix(), height)) {
                return false;
            }
            return true;
        }finally {
            writeLock.unlock();
        }
    }

    // supplier校验fuzzer提交的payloads中的newblock是否是正确的，不正确则拒绝接收payloads
    // 判断提交的block是否和supplier本地的最新区块相同，因为提交payload有时间限制，过了时限supplier收到会丢弃，这里先设置时限为挖到一个区块的时间
    @Override
    public boolean supplierCheckNewBlock(Block block) {
        Block latestBlock = chainService.getLocalLatestBlock();
        if (latestBlock.getHash().equals(block.getHash())){
            return true;
        }
        //如果该区块和本地区块链的不一样，进一步校验
        if (latestBlock.getBlockHeader().getHeight() == block.getBlockHeader().getHeight() - 1) {
            // @TODO 申请同步区块

        }
        return false;
    }

    /**
     * 移除已经打包好的交易，去掉coinbase交易
     * @param block
     * @return
     */
    @Override
    public boolean removeTransactions(Block block) throws TransactionNotExistException {
        List<Transaction> transactionList = block.getTransactions();
        for (int i = 1; i < transactionList.size(); i++) {
            String txIdStr = transactionList.get(i).getTxIdStr();
            mempool.removeTransaction(txIdStr);
            if (!dbStore.delete(WalletPrefix.TX_PREFIX.getPrefix() + txIdStr) ||
            !dbStore.delete(WalletPrefix.UTXO_PREFIX.getPrefix() + txIdStr)) {
                throw new TransactionNotExistException(txIdStr);
            }
        }
        return true;
    }
}
