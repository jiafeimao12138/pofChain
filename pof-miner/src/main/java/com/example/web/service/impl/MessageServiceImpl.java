package com.example.web.service.impl;

import com.example.base.entities.Message;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewMsgEvent;
import com.example.web.service.MessageService;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Override
    public boolean publishMsg(Message message) {
        ApplicationContextProvider.publishEvent(new NewMsgEvent(new Message("","","hello")));
        return false;
    }
}
