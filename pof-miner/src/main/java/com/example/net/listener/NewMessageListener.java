package com.example.net.listener;

import com.example.base.entities.Message;
import com.example.base.entities.NewPath;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.events.NewMsgEvent;
import com.example.net.events.NewPathRank;
import com.example.net.events.NewTargetProgramEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

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

    @EventListener(NewTargetProgramEvent.class)
    public void onNewTargetProgram(NewTargetProgramEvent event) {
        byte[] fileBytes = (byte[])event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.PUBLISH_FILE);
        messagePacket.setBody(fileBytes);
        p2pClient.sendToGroup(messagePacket);
    }

    // supplier广播新消息排名
    @EventListener(NewPathRank.class)
    public void onNewPathRank(NewPathRank event) {
        LinkedHashMap<String, List<NewPath>> pathRankMap = (LinkedHashMap<String, List<NewPath>>) event.getSource();
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.NEW_PATH_RANK);
        messagePacket.setBody(SerializeUtils.serialize(pathRankMap));
        p2pClient.sendToGroup(messagePacket);
    }
}
