package com.example.net.server;

import com.example.base.entities.Block;
import com.example.base.entities.Message;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewPeerEvent;
import com.example.web.service.BlockService;
import com.example.web.service.BlockServiceImpl;
import com.example.web.service.MessageService;
import com.example.web.service.PeerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.core.Node;

// 处理其他node发送的message request
@Component
public class MessageServerHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);

    private final BlockService blockService;
    private final PeerService peerService;
    private final P2pClient p2pClient;

    public MessageServerHandler(BlockService blockService, PeerService peerService,
                                P2pClient p2pClient) {
        this.blockService = blockService;
        this.peerService = peerService;
        this.p2pClient = p2pClient;
    }

    public synchronized MessagePacket helloMessage(byte[] msgBody) {
        Message message = (Message) SerializeUtils.unSerialize(msgBody);
        logger.info("receive a new message， {}", message);
        PacketBody packetBody = new PacketBody(message, true);
        return buildPacket(MessagePacketType.HELLO_MESSAGE, packetBody, null);
    }

    public synchronized MessagePacket newPeerConnect(byte[] msgBody) throws Exception {
        Peer peer = (Peer) SerializeUtils.unSerialize(msgBody);
        // 如果该节点之前没有连过，将它添加到数据库中
        if (!peerService.hasPeer(peer)) {
            peerService.addPeer(peer);
        }
        if (p2pClient.connect(new Node(peer.getIp(),peer.getPort()))) {
            logger.info("已连接新节点：{}", peer);
            ApplicationContextProvider.publishEvent(new NewPeerEvent(peer));
        }
        return null;
    }

    //处理接收到的新区块
    public synchronized MessagePacket receiveNewBlock(byte[] msgBody) {
        Block newBlock = (Block) SerializeUtils.unSerialize(msgBody);
        if (!blockService.validateNewBlock(newBlock)) {
            logger.info("校验新区块失败");
            return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, false), "校验新区块失败");
        }
        logger.info("校验新区块成功");
        return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, true), "成功");

    }

    //处理接收到的新消息
    public synchronized MessagePacket receiveNewMsg(byte[] msgBody) {
        Message message = (Message) SerializeUtils.unSerialize(msgBody);
        logger.info("接收到一个新消息，{}", message.getText());
        return buildPacket(MessagePacketType.RES_NEW_MESSAGE, new PacketBody(message, true), "成功");
    }

    private MessagePacket buildPacket(byte type, PacketBody packetBody, String message)
    {
        MessagePacket resPacket = new MessagePacket();
        packetBody.setMessage(message);
        resPacket.setType(type);
        resPacket.setBody(SerializeUtils.serialize(packetBody));
        return resPacket;
    }


}
