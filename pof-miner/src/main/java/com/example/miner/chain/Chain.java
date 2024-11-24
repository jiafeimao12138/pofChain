package com.example.miner.chain;

import com.example.miner.Miner;
import com.example.net.conf.MinerConfig;
import com.example.web.service.MiningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author jiafeimao
 * @date 2024年09月15日 14:51
 */

@Component
public class Chain {

    private static final Logger logger = LoggerFactory.getLogger(Chain.class);
    @Qualifier(value = "pofMiner")
    private final Miner miner;
    private final MiningService miningService;
    private final MinerConfig minerConfig;

    public Chain(Miner miner, MiningService miningService, MinerConfig minerConfig) {
        this.miner = miner;
        this.miningService = miningService;
        this.minerConfig = minerConfig;
    }


//    @PostConstruct
//    public void run() {
//        new Thread(() -> {
////            if (!minerConfig.isEnableMining()){
////                ApplicationContextProvider.publishEvent(new NewMsgEvent(new Message("","","hello")));
////                logger.info("广播消息111111");
////                return;
////            }
//            logger.info("blockj miner start");
//            try {
//                Block preBlock = blockService.getPreBlock();
//                miner.mineOne(preBlock);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }

}
