package com.example.web.controller;

import com.example.miner.Miner;
import com.example.miner.chain.Chain;
import com.example.net.conf.MinerConfig;
import com.example.web.service.BlockService;
import com.example.web.service.BlockServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:32
 */
@RestController
public class BlockController {
    @Autowired
    BlockService blockService;

    @RequestMapping("startmining")
    public void mine() {
        blockService.startMining();
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
