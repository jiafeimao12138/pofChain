package com.example.web.controller;

import com.example.base.entities.Block;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:32
 */
@RestController
@RequiredArgsConstructor
public class BlockController {

    private final MiningService miningService;
    private final ChainService chainService;

    @RequestMapping("startmining")
    public void mine() {
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
