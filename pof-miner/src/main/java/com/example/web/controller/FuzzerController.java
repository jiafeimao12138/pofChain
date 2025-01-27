package com.example.web.controller;

import com.example.base.entities.block.Block;
import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import com.example.web.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    private final Node node;
    private final ProcessService processService;
    private String fuzzingprocessId = "";

    @RequestMapping("startFuzzing")
    public void mine() {
        if (node.getType() == NodeType.SUPPLIER) {
            logger.info("supplier不允许fuzzing");
        } else {
            node.setType(NodeType.FUZZER);
//            if (miningService.AFLswitchRoot()){
//                miningService.startMining();
//            }
            miningService.startMining();
        }
    }

    // 挂起Fuzzing进程
    @RequestMapping("suspendFuzzing")
    public void suspendFuzzing() {
        try {
            List<String> processIds = processService.findProcessIds("afl-fuzz");
            logger.info("suspendFuzzing processIds: {}", processIds);
            if (processIds.size() > 1) {
                logger.error("fuzzing进程数大于1");
            }else {
                fuzzingprocessId = processIds.get(0);
                processService.suspendProcess(fuzzingprocessId);
                logger.info("已挂起，进程号{}", fuzzingprocessId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //停止Fuzzing进程
    @RequestMapping("stopFuzzing")
    public void stopFuzzing() {
        node.setType(NodeType.OBSERVER);
        try {
            List<String> processIds = processService.findProcessIds("afl-fuzz");
            logger.info("suspendFuzzing processIds: {}", processIds);
            if (processIds.size() > 1) {
                logger.error("fuzzing进程数大于1");
            }else {
                fuzzingprocessId = processIds.get(0);
                processService.killProcess(fuzzingprocessId);
                logger.info("已杀死Fuzzing进程，进程号{}", fuzzingprocessId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 恢复Fuzzing进程
    @RequestMapping("resumeFuzzing")
    public void resumeFuzzing() {
        try {
            processService.resumeProcess(fuzzingprocessId);
            logger.info("已恢复Fuzzing，进程号{}",fuzzingprocessId);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
