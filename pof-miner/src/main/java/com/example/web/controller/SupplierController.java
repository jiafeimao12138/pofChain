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
        programService.prepareTargetProgram("/home/wj/pofChain/AFL/afl_testfiles/test_afl_files/string_length.c",
                "/home/wj/pofChain/AFL/afl_testfiles/objfiles/string_length_publish");
    }
}
