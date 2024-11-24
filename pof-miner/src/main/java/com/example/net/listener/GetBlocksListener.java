package com.example.net.listener;

import com.example.base.entities.Block;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.GetBlockByHeightEvent;
import com.example.net.events.GetBlocksEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBlocksListener {
    private final P2pClient p2pClient;

    @EventListener(GetBlocksEvent.class)
    public void onGetBlocks(GetBlocksEvent event) {
        Block block = (Block) event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.REQ_BLOCKS);
        messagePacket.setBody(SerializeUtils.serialize(block));
        p2pClient.sendToGroup(messagePacket);
    }

    @EventListener(GetBlockByHeightEvent.class)
    public void onGetBlockByHeight(GetBlockByHeightEvent event) {
        long height = (long) event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.REQ_BLOCK_BY_HEIGHT);
        messagePacket.setBody(SerializeUtils.serialize(height));
        p2pClient.sendToGroup(messagePacket);
    }
}
