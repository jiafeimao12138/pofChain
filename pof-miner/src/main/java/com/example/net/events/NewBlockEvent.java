package com.example.net.events;

import com.example.base.entities.Block;
import org.springframework.context.ApplicationEvent;

// 新区块事件
public class NewBlockEvent extends ApplicationEvent {
    public NewBlockEvent(Block newBlock) {
        super(newBlock);
    }
}
