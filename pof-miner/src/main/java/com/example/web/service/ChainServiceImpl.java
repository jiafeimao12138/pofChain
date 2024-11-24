package com.example.web.service;

import com.example.base.entities.Block;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlockByHeightEvent;
import com.example.net.events.GetBlocksEvent;
import com.example.net.events.GetHeightEvent;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ChainServiceImpl implements ChainService{

    private final DBStore rocksDBStore;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

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
        Block block = new Block();
        Optional<Object> o = rocksDBStore.get(BlockPrefix.HEIGHT.getPrefix());
        if (o.isPresent()) {
            long height = (long)o.get();
            block = getBlockByHeight(height);
            return block;
        }
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
        Optional<Object> o = rocksDBStore.get(BlockPrefix.CHAIN_HEIGHT.getPrefix());
        if (o.isPresent()) {
            long height = (long) o.get();
            return height;
        }
        return -1;
    }
}
