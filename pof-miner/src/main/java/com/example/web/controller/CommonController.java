package com.example.web.controller;

import com.example.base.entities.Program;
import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.fuzzed.ProgramService;
import com.example.web.service.ChainService;
import com.example.web.service.impl.FakeTXGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("common")
@RequiredArgsConstructor
public class CommonController {

    private final ChainService chainService;
    private final FakeTXGenerator generator;
    private final ProgramService programService;

    @RequestMapping("generateFakeTXs")
    public void generateFakeTXs() throws Exception {
        generator.generateTransactions();
    }

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

    @RequestMapping("getLatestBlocks")
    public List<Block> getLatestBlocks() {
        ArrayList<Block> blocks = new ArrayList<>();
        List<Long> heights = chainService.getLocalBlocksHeight();
        for (Long height : heights) {
            Block block = chainService.getBlockByHeight(height);
            blocks.add(block);
        }
        return blocks;
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
    public Block getBlockByHeight(@RequestParam long height) {
        Block block = chainService.getBlockByHeight(height);
//        JsonVo<Block> jsonVo = new JsonVo<>();
//        jsonVo.setData(block);
//        jsonVo.setCode(JsonVo.SUCCESS);
        return block;
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
    public Block getLocalChainLatestBlock() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
//        JsonVo jsonVo = new JsonVo<>();
//        jsonVo.setData(localLatestBlock);
//        jsonVo.setCode(JsonVo.SUCCESS);
        return localLatestBlock;
    }

    @RequestMapping("getLocalChainHeight")
    public JsonVo<Long> getLocalChainHeight() {
        Block localLatestBlock = chainService.getLocalLatestBlock();
        long height = localLatestBlock.getBlockHeader().getHeight();
        return new JsonVo<>(JsonVo.SUCCESS, height);
    }

    @RequestMapping("getTasks")
    public JsonVo<Program> getTasks() {
        ConcurrentHashMap<String, Program> tasks = programService.getTasks();
        ArrayList<Program> taskList = new ArrayList<>();
        for (Map.Entry<String, Program> entry : tasks.entrySet()) {
            taskList.add(entry.getValue());
        }

        JsonVo jsonVo = new JsonVo<>();
        jsonVo.setCode(JsonVo.SUCCESS);
        jsonVo.setData(taskList);
        return jsonVo;
    }

}
