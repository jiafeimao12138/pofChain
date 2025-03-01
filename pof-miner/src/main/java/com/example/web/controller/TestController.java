package com.example.web.controller;

import com.example.base.entities.*;
import com.example.base.crypto.CryptoUtils;
import com.example.base.utils.SerializeUtils;
import com.example.fuzzed.ProgramService;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.web.service.testservice1;
import com.example.web.service.testservice2;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tio.client.ClientChannelContext;
import org.tio.core.Tio;

import java.util.*;

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
    private final NewPathManager newPathManager;
    private final ProgramQueue programQueue;

    @RequestMapping("/setPayloads")
    public void getPayloads() {
        System.out.println(service1.setPayload().getPayloads());
    }

    @RequestMapping("/getPayloads")
    public void getPayloads2() {
        System.out.println(service2.getPayloadManager());
    }

    @RequestMapping("/getProgramQueue")
    public void getProgramQueue() {
        ArrayDeque<MutablePair<byte[], Peer>> queue = programQueue.getProgramQueue();
        if (queue.isEmpty())
            System.out.println("[]");
        for (MutablePair<byte[], Peer> pair : queue) {
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

    @RequestMapping("setNewPathMap")
    public void addNewPathMap() {
        List<NewPath> newPaths1 = new ArrayList<>();
        List<NewPath> newPaths2 = new ArrayList<>();
        List<NewPath> newPaths3 = new ArrayList<>();
        Integer[] arr = {1,2,4,2};
        newPaths1.add(new NewPath(Arrays.asList(arr), "12312", 123214));
        newPaths2.add(new NewPath(Arrays.asList(arr), "54674", 345354));
        newPaths3.add(new NewPath(Arrays.asList(arr), "1354", 3525425));
        newPathManager.addPathHashMap("1233", newPaths1);
        newPathManager.addPathHashMap("1233", newPaths2);
        newPathManager.addPathHashMap("1233", newPaths3);
    }

    @RequestMapping("/getNodeType")
    public void getNodeType() {
        System.out.println(node);
    }

    @RequestMapping("getNewPathMap")
    public HashMap<String, List<NewPath>> getNewPathMap() {
        return newPathManager.getPaths();
    }

    public static void main(String[] args) {
        List<Integer> list1 = new ArrayList<>();
        list1.add(1);
        list1.add(2);
        list1.add(3);

        List<Integer> list2 = new ArrayList<>();
        list2.add(1);
        list2.add(2);
        list2.add(3);

        String str1 = StringUtils.join(list1, ",");
        String str2 = StringUtils.join(list2, ",");

        // 计算 hash 值
        System.out.println(CryptoUtils.SHA256(str1));
        System.out.println(CryptoUtils.SHA256(str2));

        System.out.println("Hashes are equal: " + (CryptoUtils.SHA256(str1).equals(CryptoUtils.SHA256(str2))));
    }



}
