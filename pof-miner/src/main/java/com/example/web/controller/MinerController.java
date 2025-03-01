package com.example.web.controller;

import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import com.example.web.service.ProcessService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:32
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("fuzzer")
public class MinerController {

    private static final Logger logger = LoggerFactory.getLogger(MinerController.class);

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
            if (CollectionUtils.isEmpty(processIds))
                return;
            Collections.sort(processIds, (s1, s2) -> Integer.compare(Integer.parseInt(s2), Integer.parseInt(s1)));
            fuzzingprocessId = processIds.get(0);
            processService.suspendProcess(fuzzingprocessId);
            logger.info("已挂起，进程号{}", fuzzingprocessId);

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
            logger.info("stopFuzzing processIds: {}", processIds);
            if (CollectionUtils.isEmpty(processIds))
                return;
            Collections.sort(processIds, (s1, s2) -> Integer.compare(Integer.parseInt(s2), Integer.parseInt(s1)));
            fuzzingprocessId = processIds.get(0);
            processService.killProcess(fuzzingprocessId);
            logger.info("已杀死Fuzzing进程，进程号{}", fuzzingprocessId);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 恢复Fuzzing进程
    @RequestMapping("resumeFuzzing")
    public void resumeFuzzing() {
        try {
            List<String> processIds = processService.findProcessIds("afl-fuzz");
            logger.info("resumeFuzzing processIds: {}", processIds);
            if (CollectionUtils.isEmpty(processIds))
                return;
            Collections.sort(processIds, (s1, s2) -> Integer.compare(Integer.parseInt(s2), Integer.parseInt(s1)));
            processService.resumeProcess(fuzzingprocessId);
            logger.info("已恢复Fuzzing，进程号{}",fuzzingprocessId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
