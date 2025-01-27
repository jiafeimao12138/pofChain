package com.example.net.listener;

import com.example.base.entities.block.Block;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.GetHeightEvent;
import com.example.net.events.NewBlockEvent;
import com.example.web.service.MiningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BlockEventListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockEventListener.class);
    private final P2pClient client;
    private final MiningService miningService;

    public BlockEventListener(P2pClient client, MiningService miningService) {
        this.client = client;
        this.miningService = miningService;
    }

    // 广播新挖出的block
    @EventListener(NewBlockEvent.class)
    public void onNewBlock(NewBlockEvent event) {
        Block newBlock = (Block) event.getSource();
        logger.info("广播new Block");
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.REQ_NEW_BLOCK);
        messagePacket.setBody(SerializeUtils.serialize(newBlock));
        client.sendToGroup(messagePacket);
    }

    // 请求主链目前高度
    @EventListener(GetHeightEvent.class)
    public void onGetHeight(GetHeightEvent event) {
        int source = (int)event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.REQ_HEIGHT);
        messagePacket.setBody(SerializeUtils.serialize(source));
        client.sendToGroup(messagePacket);
    }


}
