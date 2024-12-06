package com.example.net.events;

import com.example.base.entities.Message;
import org.springframework.context.ApplicationEvent;

public class NewMsgEvent extends ApplicationEvent {

    public NewMsgEvent(Message message) {
        super(message);
    }
}
