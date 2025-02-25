package com.example.web.controller;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.web.service.ChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("common")
@RequiredArgsConstructor
public class ObserverController {

    private final ChainService chainService;

    /**
     * 同步区块
     * @param isFullNode 是否是全节点
     */
    @RequestMapping("syncBlocks")
    public void syncBlocks(@RequestParam boolean isFullNode) {
        chainService.syncBlockChain(isFullNode, 0);
    }

    @RequestMapping("getLocalBlocks")
    public List<Long> getLocalBlocks() {
        return chainService.getLocalBlocksHeight();
    }

    @RequestMapping("getBlocks")
    public JsonVo<List<Block>> getBlocks(@RequestParam long start, @RequestParam long end) {
        ArrayList<Block> blocks = new ArrayList<>();
        for (long i = start; i <= end; i++) {
            Block blockByHeight = chainService.getBlockByHeight(i);
            if (blockByHeight != null)
                blocks.add(blockByHeight);
        }
        JsonVo<List<Block>> jsonVo = new JsonVo<>();
        jsonVo.setData(blocks);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("getBlockByHash")
    public JsonVo<Block> getBlockByHash(@RequestParam String hash) {
        Block block = chainService.getBlockByHash(hash);
        JsonVo<Block> jsonVo = new JsonVo<>();
        jsonVo.setData(block);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("getBlockByHeight")
    public JsonVo<Block> getBlockByHeight(@RequestParam long height) {
        Block block = chainService.getBlockByHeight(height);
        JsonVo<Block> jsonVo = new JsonVo<>();
        jsonVo.setData(block);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @Deprecated
    @RequestMapping("getChainHeight")
    public JsonVo<Long> getChainHeight() {
        long mainChainHeight = chainService.getMainChainHeight();
        JsonVo jsonVo = new JsonVo<>();
        jsonVo.setData(mainChainHeight);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("getChainLatestBlock")
    public JsonVo<Block> getLocalChainLatestBlock() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
        JsonVo jsonVo = new JsonVo<>();
        jsonVo.setData(localLatestBlock);
        jsonVo.setCode(JsonVo.SUCCESS);
        return jsonVo;
    }

    @RequestMapping("getLocalChainHeight")
    public JsonVo<Long> getLocalChainHeight() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
        long height = localLatestBlock.getBlockHeader().getHeight();
        return new JsonVo<>(JsonVo.SUCCESS, height);
    }

}
