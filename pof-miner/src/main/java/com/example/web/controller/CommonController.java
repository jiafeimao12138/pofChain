package com.example.web.controller;

import com.example.base.entities.NewPathManager;
import com.example.base.entities.Program;
import com.example.base.entities.block.Block;
import com.example.base.utils.BlockUtils;
import com.example.base.utils.LoggingMonitor;
import com.example.base.vo.JsonVo;
import com.example.fuzzed.ProgramService;
import com.example.net.base.Mempool;
import com.example.web.service.ChainService;
import com.example.web.service.impl.FakeTXGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

@RestController
@RequestMapping("common")
@RequiredArgsConstructor
public class CommonController {

    private final ChainService chainService;
    private final FakeTXGenerator generator;
    private final ProgramService programService;
    private final Mempool mempool;
    private final NewPathManager newPathManager;

    @RequestMapping("generateFakeTXs")
    public void generateFakeTXs() throws Exception {
        generator.generateInitialUTXO(50);
        while (true) {
            try {
                generator.generateTransactions();
                System.out.println("本次交易发送完毕");
                Thread.sleep(30000); // 睡眠 30000 毫秒，即 30 秒
            } catch (InterruptedException e) {
                e.printStackTrace(); // 如果线程在睡眠期间被中断，捕获异常
            }
        }
    }

    @RequestMapping("getMempoolSize")
    public int getMempoolSize() {
        return mempool.size();
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

    // 请求区块平均生成时间，并写入文件
    @RequestMapping("getAvgBlockTime")
    public String getAvgBlockTime() {
        long prevTime = chainService.getBlockByHeight(1).getBlockHeader().getNTime();
        long chainHeight = chainService.getChainHeight();
        ArrayList<Double> diffList = new ArrayList<>();
        for (long i = 2; i <= chainHeight; i++) {
            Block block = chainService.getBlockByHeight(i);
            long nTime = block.getBlockHeader().getNTime();
            double diff = (nTime - prevTime) / 1000.0;
            diffList.add(diff);
            prevTime = nTime;
        }
        double avg = diffList.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        // 写入文件
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("block_time_diffs.txt"))) {
            IntStream.range(0, diffList.size())
                    .forEach(i -> {
                        try {
                            writer.write(i + ": " + diffList.get(i));
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.format("%.2f", avg);
    }

    // 请求crash数量
    @RequestMapping("getCrashNum")
    public int getCrashNum() {
        return programService.getCrashNum();
    }

    // 请求最新50个区块的生成时间
    @RequestMapping("getBlockTimeList")
    public List<Double> getBlockTimeList() {
        long chainHeight = chainService.getChainHeight();
        long l = (chainHeight - 50) >= 0 ? (chainHeight - 50) : 0;

        long prevTime = chainService.getBlockByHeight(1).getBlockHeader().getNTime();
        ArrayList<Double> diffList = new ArrayList<>();
        for (long i = l + 2; i <= chainHeight; i++) {
            Block block = chainService.getBlockByHeight(i);
            long nTime = block.getBlockHeader().getNTime();
            double diff = (nTime - prevTime)/1000;
            diffList.add(diff);
            prevTime = nTime;
        }
        return diffList;
    }

    @RequestMapping("getBlockAvgSize")
    public String getBlockAvgSize() {
        long chainHeight = chainService.getChainHeight();
        ArrayList<Integer> sizeList = new ArrayList<>();
        for (int i = 1; i <= chainHeight; i++) {
            Block block = chainService.getBlockByHeight(i);
            int blockSize = BlockUtils.getBlockSize(block);
            sizeList.add(blockSize);
        }
        System.out.println(sizeList);
        double avgSize = sizeList.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        double size = avgSize / 1024;
        return String.format("%.2f", size);
    }

    @RequestMapping("getTotalNewPath")
    public long getTotalNewPath() {
        HashMap<String, MutablePair<Long, Long>> programPathInfo =
                newPathManager.getProgramPathInfo();
        long newPath = 0;
        for (Map.Entry<String, MutablePair<Long, Long>> entry : programPathInfo.entrySet()) {
            MutablePair<Long, Long> value = entry.getValue();
            newPath += value.getLeft();
        }
        return newPath;
    }

    // 获取随时间的新路径变化
    @RequestMapping("getNewPathList")
    public TreeMap<Long, Long> getNewPathList() {
        HashMap<Long, Long> map = LoggingMonitor.readLogFile();
        TreeMap<Long, Long> sortedMap = new TreeMap<>(map);
        return sortedMap;
    }

    @RequestMapping("getConfirmedTXs")
    public int getConfirmedTXs() {
        int totalSize = 0;
        long chainHeight = chainService.getChainHeight();
        for (int i = 1; i <= chainHeight; i++) {
            int size = chainService.getBlockByHeight(i).getTransactions().size();
            totalSize += size;
        }
        return totalSize;
    }

}
