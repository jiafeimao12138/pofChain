package com.example.net.listener;

import com.example.base.entities.Message;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.NewMsgEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NewMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(NewMessageListener.class);

    private final P2pClient p2pClient;
    public NewMessageListener(P2pClient p2pClient) {
        this.p2pClient = p2pClient;
    }

    //新消息事件触发
    @EventListener(NewMsgEvent.class)
    public void onNewMsg(NewMsgEvent event) {
        Message message = (Message) event.getSource();
        logger.info("开始广播新消息: " + message);
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.REQ_NEW_MESSAGE);
        messagePacket.setBody(SerializeUtils.serialize(message));
        p2pClient.sendToGroup(messagePacket);
    }
}
