package com.example.net.events;

import org.springframework.context.ApplicationEvent;

public class GetProgramQueue extends ApplicationEvent {
    public GetProgramQueue(int source) {
        super(source);
    }
}
