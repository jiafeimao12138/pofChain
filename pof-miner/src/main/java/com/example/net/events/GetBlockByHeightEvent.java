package com.example.net.events;

import org.springframework.context.ApplicationEvent;

public class GetBlockByHeightEvent extends ApplicationEvent {
    public GetBlockByHeightEvent(long height) {
        super(height);
    }
}
