package com.example.net.events;

import org.springframework.context.ApplicationEvent;

public class TerminateAFLEvent extends ApplicationEvent {
    public TerminateAFLEvent(Object source) {
        super(source);
    }
}
