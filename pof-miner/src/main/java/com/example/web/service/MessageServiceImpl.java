package com.example.web.service;

import com.example.base.entities.Message;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewMsgEvent;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService{
    @Override
    public boolean sendMessage(Message message) {
        ApplicationContextProvider.publishEvent(new NewMsgEvent(new Message("","","hello")));

        return false;
    }
}
