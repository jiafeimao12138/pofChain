package com.example.web.service;

import com.example.base.entities.block.Block;
import com.example.exception.TransactionNotExistException;

public interface ValidationService {
    boolean processNewBlock(Block block);
    boolean processNewMinedBlock(Block block);
    boolean checkBlock(Block block);
    boolean checkTransactions(Block block);
    boolean storeBlock(Block block);
    boolean storeChainHeight(long height);
    boolean supplierCheckNewBlock(Block block);
    boolean removeTransactions(Block block) throws TransactionNotExistException;
}
