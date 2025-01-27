package com.example.miner.pof;

import com.example.base.entities.block.Block;
import com.example.miner.Miner;
import org.springframework.stereotype.Component;

/**
 * @author jiafeimao
 * @date 2024年09月15日 15:00
 */
@Component("pofMiner")
public class PoFMiner implements Miner {

    @Override
    public void mineAndFuzzing(Block preBlock) {

        ProofOfFuzzing proofOfFuzzing = ProofOfFuzzing.newProofOfFuzzing(preBlock);
        proofOfFuzzing.run();
    }
}
