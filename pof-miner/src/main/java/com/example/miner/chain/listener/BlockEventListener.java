package com.example.miner.chain.listener;

import com.example.base.entities.Block;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.NewBlockEvent;
import com.example.web.service.BlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.invoke.SerializedLambda;

@Component
public class BlockEventListener {
    private static final Logger logger = LoggerFactory.getLogger(BlockEventListener.class);
    private final P2pClient client;
    private final BlockService blockService;

    public BlockEventListener(P2pClient client, BlockService blockService) {
        this.client = client;
        this.blockService = blockService;
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


}
