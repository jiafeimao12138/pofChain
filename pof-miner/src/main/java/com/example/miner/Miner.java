package com.example.miner;


import com.example.base.entities.Block;
import com.example.web.service.BlockService;

/**
 * @author jiafeimao
 * @date 2024年09月14日 22:20
 */
public interface Miner {
    void mineAndFuzzing(Block preBlock) throws Exception;
}
