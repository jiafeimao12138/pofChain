package com.example.web.service;

import com.example.base.entities.Block;
import org.springframework.stereotype.Service;


public interface BlockService {
    String BLOCK_PREFIX = "/blocks/";

    Block generateGenesisBlock();
    Block addBlock();
    Block getPreBlock();
}
