package com.example.web.rpc;

import com.example.base.entities.block.Block;
import com.example.base.vo.JsonVo;

public interface BlockService {
    JsonVo<Block> getLocalChainLatestBlock();
    JsonVo<Long> getLongestChainHeight();
    void syncBlockChain();
}
