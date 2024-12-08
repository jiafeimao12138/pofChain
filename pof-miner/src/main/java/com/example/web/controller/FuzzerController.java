package com.example.web.controller;

import com.example.base.entities.Block;
import com.example.base.entities.NodeType;
import com.example.net.server.P2pServer;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("fuzzer")
public class FuzzerController {

    private static final Logger logger = LoggerFactory.getLogger(FuzzerController.class);

    private final MiningService miningService;
    private final ChainService chainService;
    private final P2pServer p2pServer;

    @RequestMapping("startmining")
    public void mine() {
        p2pServer.setMeType(NodeType.FUZZER);
        logger.info("node: {}", p2pServer.getMe());
        if (miningService.AFLswitchRoot()){
            miningService.startMining();
        }
    }

    @RequestMapping("getChainHeight")
    public long getChainHeight() {
        long mainChainHeight = chainService.getMainChainHeight();
        return mainChainHeight;
    }

    @RequestMapping("getLocalChainLatestBlock")
    public String getLocalChainLatestBlock() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
        return "localLatestBlock: hash=" + localLatestBlock.getHash() + ",height=" + localLatestBlock.getBlockHeader().getHeight();
    }

    @RequestMapping("syncBlockChain")
    public void syncBlockChain() {
        chainService.syncBlockChain(1);
    }

    @RequestMapping("test")
    public void test() throws InterruptedException {
        int i = 0;
        while (i++ < 10) {
            Thread.sleep(1000);
            System.out.println("=================");
        }

    }
}
