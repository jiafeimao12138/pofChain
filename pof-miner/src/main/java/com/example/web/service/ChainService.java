package com.example.web.service;

import com.example.base.entities.block.Block;

import java.util.List;

public interface ChainService {
    void syncBlockChain(long height);
    Block getGenesisBlock();
    Block getBlockByHeight(long height);
    Block getBlockByHash(String hash);
    Block getLocalLatestBlock();
    long getMainChainHeight();
    List<Long> getLocalBlocksHeight();
}
