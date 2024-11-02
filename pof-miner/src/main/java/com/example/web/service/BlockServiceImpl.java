package com.example.web.service;


import com.example.base.entities.Block;
import com.example.base.store.RocksDBStore;
import com.example.miner.chain.Chain;
import com.example.miner.pof.PoFMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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

    public Block generateGenesisBlock() {

        Block genesisBlock = new Block();
        genesisBlock.setHashPreBlock("00000000000000");
        genesisBlock.setNBits(0x1d00ffff);
        genesisBlock.setNNonce(2083236893);
        genesisBlock.setTransactions(new ArrayList<>());
        genesisBlock.setNVersion(1);
        genesisBlock.setHashMerkleRoot("");
        genesisBlock.setNTime(System.currentTimeMillis());

        rocksDBStore.put(BLOCK_PREFIX + genesisBlock.GetHash(), genesisBlock);
        logger.info("Successfully create genesis block and store in database. Hash is {}.", genesisBlock.GetHash());
        return genesisBlock;
    }

    public Block addBlock() {
//        Chain chain = new Chain(new PoFMiner());
//        chain.run();
        return new Block();
    }

    public boolean storeBlock(Block block) {
        rocksDBStore.put(BLOCK_PREFIX + block.GetHash(), block);
        return true;
    }

    public Block getPreBlock() {
        Block b = new Block();
        return b;
    }
}
