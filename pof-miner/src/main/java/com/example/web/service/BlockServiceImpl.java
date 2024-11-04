package com.example.web.service;


import com.example.base.entities.Block;
import com.example.base.store.RocksDBStore;
import com.example.miner.Miner;
import com.example.miner.chain.Chain;
import com.example.miner.pof.PoFMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:28
 */

@Service
public class BlockServiceImpl implements BlockService{

    private static final Logger logger = LoggerFactory.getLogger(BlockServiceImpl.class);

    static String BLOCK_PREFIX = "/blocks/";

    String dir_path = System.getProperty("user.dir");
    RocksDBStore rocksDBStore = new RocksDBStore(dir_path);

    @Autowired
    private Miner miner;

    @Override
    public void startMining() {
        new Thread(() -> {
            logger.info("开始进行Fuzzing挖矿");
            try {
                Block preBlock = getLatestBlock();
                miner.mineAndFuzzing(preBlock);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //存储新区块到本地区块链中
    public boolean storeBlock(Block newBlock) {
        if (!rocksDBStore.put(BLOCK_PREFIX + newBlock.height, newBlock)) {
            logger.info("存入新区块失败");
            return false;
        }
        if (!rocksDBStore.put(HEIGHT_PREFIX, newBlock.height)){
            logger.info("存入最新高度失败");
            return false;
        }
        rocksDBStore.close();
        logger.info("存入新区块，高度为{}，hash={}", newBlock.height, newBlock.GetHash());
        return true;
    }

    // 获取本地区块链中最新的链
    public Block getLatestBlock() {
        Block block = new Block();
        Optional<Object> o = rocksDBStore.get(HEIGHT_PREFIX);
        rocksDBStore.close();
        if (o.isPresent()) {
            long height = (long)o.get();
            block = getBlockByHeight(height);
        }
        return block;
    }

    @Override
    public boolean validateNewBlock(Block newBlock) {
        return true;
    }

    @Override
    public void testBlocks() {

    }

    @Override
    public Block getBlockByHeight(long height) {
        rocksDBStore.get(HEIGHT_PREFIX + height);
        return null;
    }

    @Override
    public Block getBlockByHash(String hash) {
        return null;
    }
}
