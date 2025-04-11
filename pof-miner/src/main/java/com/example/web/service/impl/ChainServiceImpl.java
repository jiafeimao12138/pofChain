package com.example.web.service.impl;

import com.example.base.entities.block.Block;
import com.example.base.entities.block.BlockHeader;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlockByHeightEvent;
import com.example.net.events.GetBlockHeaderByHeightEvent;
import com.example.net.events.GetHeightEvent;
import com.example.web.service.ChainService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ChainServiceImpl implements ChainService {

    private final DBStore rocksDBStore;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();

    public ChainServiceImpl(DBStore dbStore) {
        this.rocksDBStore = dbStore;
    }

    /**
     * 同步区块链
     * @param isFullNode 是否全节点
     * @param height 开始同步的高度
     */
    @Override
    public void syncBlockChain(boolean isFullNode, long height) {
        if (isFullNode) {
            // 同步整个区块
            ApplicationContextProvider.publishEvent(new GetBlockByHeightEvent(height));
        } else {
            // 只同步区块头
            ApplicationContextProvider.publishEvent(new GetBlockHeaderByHeightEvent(height));
        }
    }

    @Override
    public Block getGenesisBlock() {
        return getBlockByHeight(0);
    }


    @Override
    public Block getBlockByHeight(long height) {
        readLock.lock();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.BLOCK_HEIGHT_PREFIX.getPrefix() + height);
        if (o.isPresent()) {
            Block block = (Block) o.get();
            readLock.unlock();
            return block;
        }
        readLock.unlock();
        return null;
    }

    @Override
    public BlockHeader getBlockHeaderByHeight(long height) {
        readLock.lock();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.BLOCK_HEIGHT_PREFIX.getPrefix() + height);
        if (o.isPresent()) {
            Block block = (Block) o.get();
            readLock.unlock();
            return block.getBlockHeader();
        }
        return null;
    }

    @Override
    public Block getBlockByHash(String hash) {
        readLock.lock();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.BLOCK_HASH_PREFIX.getPrefix() + hash);
        if (o.isPresent()) {
            Block block = (Block) o.get();
            readLock.unlock();
            return block;
        }
        return null;
    }

    @Override
    public Block getLocalLatestBlock() {
        readLock.lock();
        Block block = new Block();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.HEIGHT.getPrefix());
        if (o.isPresent()) {
            long height;
            Object value = o.get();
            if (value instanceof Integer) {
                height = ((Integer) value).longValue();  // Integer 转 Long
            } else if (value instanceof Long) {
                height = (Long) value;
            } else {
                throw new IllegalStateException("Unexpected type: " + value.getClass());
            }
            block = getBlockByHeight(height);
            readLock.unlock();
            return block;
        }
        readLock.unlock();
        return block;
    }

    @Override
    public long getMainChainHeight() {
        ApplicationContextProvider.publishEvent(new GetHeightEvent(0));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        readLock.lock();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.CHAIN_HEIGHT.getPrefix());
        if (o.isPresent()) {
            long height;
            Object value = o.get();
            if (value instanceof Integer) {
                height = ((Integer) value).longValue();  // Integer 转 Long
            } else if (value instanceof Long) {
                height = (Long) value;
            } else {
                throw new IllegalStateException("Unexpected type: " + value.getClass());
            }
            readLock.unlock();
            return height;
        }
        readLock.unlock();
        return -1;
    }

    /**
     * 获取本地区块链中的所有区块的高度索引
     * @return
     */
    @Override
    public List<Long> getLocalBlocksHeight() {
        readLock.lock();
        ArrayList<Long> heights = new ArrayList<>();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.HEIGHT.getPrefix());
        long height = -1;
        if (o.isPresent()) {
            Object value = o.get();
            if (value instanceof Integer) {
                height = ((Integer) value).longValue();  // Integer 转 Long
            } else if (value instanceof Long) {
                height = (Long) value;
            } else {
                throw new IllegalStateException("Unexpected type: " + value.getClass());
            }
        }
        for (long i = height; i >= 0 ; i--) {
            heights.add(i);
        }
        readLock.unlock();
        return heights;
    }

    @Override
    public long getChainHeight() {
        readLock.lock();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.HEIGHT.getPrefix());
        long height = -1;
        if (o.isPresent()) {
            Object value = o.get();
            if (value instanceof Integer) {
                height = ((Integer) value).longValue();  // Integer 转 Long
            } else if (value instanceof Long) {
                height = (Long) value;
            } else {
                throw new IllegalStateException("Unexpected type: " + value.getClass());
            }
        }
        readLock.unlock();
        return height;
    }



}
