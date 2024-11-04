package com.example.web.service;

import com.example.base.entities.Block;
import org.springframework.stereotype.Service;


public interface BlockService {
    String BLOCK_PREFIX = "/blocks/";
    //用于维护本地区块最新高度
    String HEIGHT_PREFIX = "/heights/";

    void startMining();
    boolean storeBlock(Block newBlock);
    Block getLatestBlock();
    boolean validateNewBlock(Block newBlock);
    void testBlocks();
    Block getBlockByHeight(long height);
    Block getBlockByHash(String hash);
}
