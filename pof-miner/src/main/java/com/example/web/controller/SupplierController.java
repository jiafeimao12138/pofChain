package com.example.web.controller;

import com.example.base.entities.Message;
import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.fuzzed.ProgramService;
import com.example.net.server.P2pServer;
import com.example.web.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SupplierController {
    private static final Logger logger = LoggerFactory.getLogger(SupplierController.class);

    private final MessageService messageService;
    private final ProgramService programService;
    private final Node node;

    @RequestMapping("/publishMsg")
    public void publishMsg(){
        messageService.publishMsg(new Message("from","to","hello"));
    }

    @RequestMapping("/publishFile")
    public void publishFile() {
        node.setType(NodeType.SUPPLIER);
        logger.info("node:{}", node);
        // @TODO：supplier上传源代码
        programService.prepareTargetProgram("supplierfiles/string_length.c",
                "supplierfiles/string_length_publish");
    }
}
