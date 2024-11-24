package com.example.net.events;

import org.springframework.context.ApplicationEvent;

public class GetHeightEvent extends ApplicationEvent {
    public GetHeightEvent(int source) {
        super(source);
    }
}
