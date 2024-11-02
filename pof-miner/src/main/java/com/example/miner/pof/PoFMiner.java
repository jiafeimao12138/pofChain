package com.example.miner.pof;

import com.example.base.entities.Block;
import com.example.miner.Miner;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author jiafeimao
 * @date 2024年09月15日 15:00
 */
@Component("pofMiner")
@NoArgsConstructor
public class PoFMiner implements Miner {

    @Override
    public void mineOne(Block preBlock) throws Exception {

        ProofOfFuzzing proofOfFuzzing = ProofOfFuzzing.newProofOfFuzzing(preBlock.GetHash(), preBlock.getHeight() + 1);
        proofOfFuzzing.run();
    }
}
