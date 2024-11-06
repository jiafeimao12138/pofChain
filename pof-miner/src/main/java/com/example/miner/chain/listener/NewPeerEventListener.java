package com.example.miner.chain.listener;

import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.NewPeerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NewPeerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(NewPeerEventListener.class);

    private final P2pClient p2pClient;

    public NewPeerEventListener(P2pClient p2pClient) {
        this.p2pClient = p2pClient;
    }

    @EventListener(NewPeerEvent.class)
    public void onNewPeer(NewPeerEvent event) {
        Peer peer = (Peer) event.getSource();
        MessagePacket packet = new MessagePacket();
        packet.setType(MessagePacketType.REQ_NEW_PEER);
        packet.setBody(SerializeUtils.serialize(peer));
        p2pClient.sendToGroup(packet);
        logger.info("已广播新peer:{}", peer);
    }
}
