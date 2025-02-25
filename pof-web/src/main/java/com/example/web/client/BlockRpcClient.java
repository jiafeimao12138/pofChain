package com.example.web.client;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.web.rpc.BlockService;
import com.example.web.rpc.Impl.BlockServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class BlockRpcClient {
    public static void main(String[] args) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        if (args.length == 0) {
            // 如果没有命令，显示帮助信息
            printHelp();
            return;
        }

        int argLength = args.length;
        String command = args[0];
        BlockService blockService = new BlockServiceImpl("http://192.168.110.134:8001", true);
        switch (command) {
            case "getChainHeight" :
                JsonVo<Long> longestChainHeight = blockService.getLongestChainHeight();
                Long height = longestChainHeight.getData();
                String resJson = objectMapper.writeValueAsString(height);
                System.out.println(resJson);
                break;
            case "getBlockByHash" :
                if (argLength < 2)
                    printError();
                String hash = args[1];
                JsonVo<Block> res = blockService.getBlockByHash(hash);
                Block block = res.getData();
                System.out.println(objectMapper.writeValueAsString(block));
                break;
            case "getBlockByHeight" :
                if (argLength < 2)
                    printError();
                String arg = args[1];
                long blockHeight = Long.parseLong(arg);
                JsonVo<Block> res1 = blockService.getBlockByHeight(blockHeight);
                Block block1 = res1.getData();
                System.out.println(objectMapper.writeValueAsString(block1));
                break;
            case "getBlocks" :
                if (argLength < 3)
                    printError();
                long start = Long.parseLong(args[1]);
                long end = Long.parseLong(args[2]);
                JsonVo<List<Block>> res2 = blockService.getBlocks(start, end);
                List<Block> blocks = res2.getData();
                System.out.println(objectMapper.writeValueAsString(blocks));
                break;
            case "help" :
                printHelp();
                break;
            default:
                // help
                printHelp();
        }

    }

    // 帮助信息
    private static void printHelp() {
        System.out.println("使用说明:");
        System.out.println("  java YourClassName getLongestChainHeight");
        System.out.println("  java YourClassName getBlock <blockHash>");
        System.out.println("  java YourClassName <command> - Show this help message.");
    }

    private static void printError() {
        System.out.println("缺少参数");
        printHelp();
    }
}
