package com.example.web.rpc;

import com.example.base.entities.Peer;
import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;

import java.util.List;

public interface BlockService {
    JsonVo<Block> getChainLatestBlock();
    JsonVo<Long> getLongestChainHeight();
    JsonVo<List<Block>> getBlocks(long start, long end);
    JsonVo<Block> getBlockByHash(String hash);
    JsonVo<Block> getBlockByHeight(long height);
}
