package com.example.web.service;

import com.example.base.entities.Block;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService{

    private static final Logger logger = LoggerFactory.getLogger(ValidationServiceImpl.class);
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();
    private final ChainService chainService;
    private final DBStore dbStore;
    private final int MIN_BLOCKS_TO_KEEP = 10;

    // 处理接收到的新区块
    @Override
    public boolean processNewBlock(Block block) {
        boolean ret = checkBlock(block);
        if (ret) {
            storeBlock(block);
            return true;
        } else {
            return false;
        }

    }

    // 校验新区块合法性
    @Override
    public boolean checkBlock(Block block) {
        long height = block.getBlockHeader().getHeight();
        // 如果这个高度的区块已经存在了
        if (chainService.getBlockByHeight(height) != null){
            return false;
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
            if (!StringUtils.equals(preHash, preBlock.getHash())) {
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
            if (!dbStore.put(BlockPrefix.BLOCK_HASH_PREFIX.getPrefix() + block.getHash(), block)){
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
}
