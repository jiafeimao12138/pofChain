package com.example.web.controller;

import com.example.base.entities.Message;
import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.fuzzed.ProgramService;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.server.P2pServer;
import com.example.web.service.testservice1;
import com.example.web.service.testservice2;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final testservice1 service1;
    private final testservice2 service2;
    private final P2pClient client;
    private final ProgramService programService;
    private List<ClientChannelContext> channelContextList;
    private final Node node;

    @RequestMapping("/setPayloads")
    public void getPayloads() {
        System.out.println(service1.setPayload().getPayloads());
    }

    @RequestMapping("/getPayloads")
    public void getPayloads2() {
        System.out.println(service2.getPayloads());
    }

    @RequestMapping("/getProgramQueue")
    public void getProgramQueue() {
        ArrayDeque<MutablePair<byte[], Peer>> programQueue =
                programService.getProgramQueue();
        if (programQueue.isEmpty())
            System.out.println("[]");
        for (MutablePair<byte[], Peer> pair : programQueue) {
            System.out.println(pair.getLeft().length + ";" + pair.getRight());
        }
    }


    @RequestMapping("/hello")
    public void hello() {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.setType(MessagePacketType.HELLO_MESSAGE);
        messagePacket.setBody(SerializeUtils.serialize("hello"));
//        ChannelContext channelContext = p2pClient.getChannelContext();

    }

    @RequestMapping("/getChannelContext")
    public void getChannelContext() {
        channelContextList = client.getChannelContextList();
    }
    @RequestMapping("/testIsConnect")
    public void testIsConnect() {
        for (int i = 0; i < channelContextList.size(); i++) {
            Message message = new Message("me", channelContextList.get(i).toString(), "hello");
            MessagePacket packet = new MessagePacket();
            packet.setType(MessagePacketType.REQ_NEW_MESSAGE);
            packet.setBody(SerializeUtils.serialize(message));
            System.out.println("向" + channelContextList.get(i) + "发送hello消息");
            Tio.send(channelContextList.get(i), packet);
        }
    }

    @RequestMapping("/getNodeType")
    public void getNodeType() {
        System.out.println(node);
    }


}
