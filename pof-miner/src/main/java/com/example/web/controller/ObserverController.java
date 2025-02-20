package com.example.web.controller;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.web.service.ChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("common")
@RequiredArgsConstructor
public class ObserverController {

    private final ChainService chainService;

    @RequestMapping("getLocalBlocks")
    public List<Long> getLocalBlocks() {
        return chainService.getLocalBlocksHeight();
    }

    @RequestMapping("getLongestChainHeight")
    public JsonVo<Long> getChainHeight() {
        long mainChainHeight = chainService.getMainChainHeight();
        JsonVo jsonVo = new JsonVo<>();
        jsonVo.setData(mainChainHeight);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("getLocalChainLatestBlock")
    public JsonVo<Block> getLocalChainLatestBlock() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
        JsonVo jsonVo = new JsonVo<>();
        jsonVo.setData(localLatestBlock);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("syncBlockChain")
    public void syncBlockChain() {
        chainService.syncBlockChain(1);
    }
}
