package com.example.miner.chain;

import com.example.base.entities.Block;
import com.example.base.entities.Message;
import com.example.miner.Miner;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.conf.MinerConfig;
import com.example.net.events.NewMsgEvent;
import com.example.web.service.BlockService;
import com.example.web.service.BlockServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

/**
 * @author jiafeimao
 * @date 2024年09月15日 14:51
 */

@Component
public class Chain {

    private static final Logger logger = LoggerFactory.getLogger(Chain.class);
    @Qualifier(value = "pofMiner")
    private final Miner miner;
    private final BlockService blockService;
    private final MinerConfig minerConfig;

    public Chain(Miner miner, BlockService blockService, MinerConfig minerConfig) {
        this.miner = miner;
        this.blockService = blockService;
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
