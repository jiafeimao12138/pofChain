package com.example.web.controller;

import com.example.base.entities.Message;
import com.example.fuzzed.ProgramService;
import com.example.web.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ProgramService programService;

    @RequestMapping("/publishMsg")
    public void publishMsg(){
        messageService.publishMsg(new Message("from","to","hello"));
    }

    @RequestMapping("/publishFile")
    public void publishFile() {
        programService.prepareTargetProgram("/home/wj/pofChain/AFL/afl_testfiles/test_afl_files/string_length.c",
                "/home/wj/pofChain/AFL/afl_testfiles/objfiles/string_length_publish");
    }

}
