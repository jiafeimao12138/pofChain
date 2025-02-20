package com.example.web;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;
import com.example.web.rpc.BlockService;
import com.example.web.rpc.Impl.BlockServiceImpl;

public class ClientOperation {
    public static void main(String[] args) {
        String api = "http://192.168.110.134:8001";
        BlockService blockService = new BlockServiceImpl(api, false);
        JsonVo<Block> latestBlock = blockService.getLocalChainLatestBlock();
        System.out.println(latestBlock.getData());
        JsonVo<Long> longestChainHeight = blockService.getLongestChainHeight();
        System.out.println(longestChainHeight);
    }
}
