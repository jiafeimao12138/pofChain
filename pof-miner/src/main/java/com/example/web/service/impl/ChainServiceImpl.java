package com.example.web.service.impl;

import com.example.base.entities.block.Block;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlockByHeightEvent;
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

    // 同步区块链
    @Override
    public void syncBlockChain(long height) {
        // TODO：只向8-10个peer发送这个请求
        ApplicationContextProvider.publishEvent(new GetBlockByHeightEvent(height));
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
            long height = (long)o.get();
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
            long height = (long) o.get();
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
        long height = 0;
        if (o.isPresent()) {
            height = (long) o.get();
        }
        for (long i = height; i >= 0 ; i--) {
            heights.add(i);
        }
        readLock.unlock();
        return heights;
    }
}
