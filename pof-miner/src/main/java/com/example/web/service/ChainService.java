package com.example.web.service;

import com.example.base.Exception.IllegalBlockException;
import com.example.base.entities.Block;

public interface ChainService {
    void syncBlockChain(long height);
    Block getGenesisBlock();
    Block getBlockByHeight(long height);
    Block getBlockByHash(String hash);
    Block getLocalLatestBlock();
    long getMainChainHeight();
}
