package com.example.web.controller;

import com.example.base.entities.Message;
import com.example.web.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    @Autowired
    MessageService messageService;

    @RequestMapping("/sendMsg")
    public void sendMessage(){
        messageService.sendMessage(new Message("from","to","hello"));
    }

}
