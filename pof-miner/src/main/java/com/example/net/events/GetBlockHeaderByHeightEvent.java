package com.example.net.events;

import org.springframework.context.ApplicationEvent;

public class GetBlockHeaderByHeightEvent extends ApplicationEvent {
    public GetBlockHeaderByHeightEvent(long height) {
        super(height);
    }
}
