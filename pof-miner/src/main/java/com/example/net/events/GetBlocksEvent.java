package com.example.net.events;

import com.example.base.entities.Block;
import org.springframework.context.ApplicationEvent;

public class GetBlocksEvent extends ApplicationEvent {
    // 获取该Block之后的所有区块
    // 可用于新节点加入时同步区块链
    // 或者对某区块之后的区块有疑问，向peer节点请求
    public GetBlocksEvent(Block block) {
        super(block);
    }
}
