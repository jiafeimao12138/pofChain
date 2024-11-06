package com.example.net.events;

import com.example.base.entities.Peer;
import org.springframework.context.ApplicationEvent;

// 新节点加入事件
public class NewPeerEvent extends ApplicationEvent {
    public NewPeerEvent(Peer peer) {
        super(peer);
    }
}
