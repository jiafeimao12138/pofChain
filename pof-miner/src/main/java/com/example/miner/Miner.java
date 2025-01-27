package com.example.miner;


import com.example.base.entities.block.Block;

/**
 * @author jiafeimao
 * @date 2024年09月14日 22:20
 */
public interface Miner {
    void mineAndFuzzing(Block preBlock) throws Exception;
}
