package com.example.net.client;

import com.example.web.service.BlockServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 处理其他 node 发送的response
@Component
public class MessageClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageClientHandler.class);
    private final BlockServiceImpl blockService;

    public MessageClientHandler(BlockServiceImpl blockService) {
        this.blockService = blockService;
    }
}
