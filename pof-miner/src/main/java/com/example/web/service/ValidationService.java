package com.example.web.service;

import com.example.base.entities.Block;

public interface ValidationService {
    boolean processNewBlock(Block block);
    boolean checkBlock(Block block);
    boolean checkTransactions(Block block);
    boolean storeBlock(Block block);
    boolean storeChainHeight(long height);
}
