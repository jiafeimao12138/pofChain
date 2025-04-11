package com.example.web.service;

import com.example.base.entities.block.Block;
import com.example.base.entities.block.BlockHeader;

import java.util.List;

public interface ChainService {
    void syncBlockChain(boolean isFullNode, long height);
    Block getGenesisBlock();
    Block getBlockByHeight(long height);
    BlockHeader getBlockHeaderByHeight(long height);
    Block getBlockByHash(String hash);
    Block getLocalLatestBlock();
    long getMainChainHeight();
    List<Long> getLocalBlocksHeight();
    long getChainHeight();

}
