package com.example.net.server;

import com.example.base.entities.Block;
import com.example.base.entities.Message;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import com.example.net.base.PacketMsgType;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewPeerEvent;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import com.example.web.service.PeerService;
import com.example.web.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.core.Node;

// 处理其他node发送的message request
@Component
@RequiredArgsConstructor
public class MessageServerHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);

    private final PeerService peerService;
    private final ValidationService validationService;
    private final ChainService chainService;
    private final P2pClient p2pClient;


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
        if (!validationService.checkBlock(newBlock)) {
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

    public synchronized MessagePacket receiveGetBlocksReq(byte[] msgBody) {
        Block block = (Block) SerializeUtils.unSerialize(msgBody);
        if( chainService.getBlockByHash(block.getHash()) != null ){

        }
        return buildPacket(MessagePacketType.RES_BLOCKS, new PacketBody(), "不存在该区块");
    }

    public synchronized MessagePacket receiveGetBlockByHeight(byte[] msgBody) {
        long height = (long) SerializeUtils.unSerialize(msgBody);
        Block block = chainService.getBlockByHeight(height);
        if (block == null) {
            // 请求的高度没有区块
            return buildPacket(MessagePacketType.RES_BLOCK_BY_HEIGHT, new PacketBody(block, PacketMsgType.FAIL_NO_HEIGHT_BLOCK), "不存在该区块");
        }
        return buildPacket(MessagePacketType.RES_BLOCK_BY_HEIGHT, new PacketBody(block, PacketMsgType.SUCEESS), "成功");
    }

    public synchronized MessagePacket receiveHeightReq(byte[] msgBody) {
        long height = chainService.getLocalLatestBlock().getBlockHeader().getHeight();
        return buildPacket(MessagePacketType.RES_HEIGHT, new PacketBody(height, PacketMsgType.SUCEESS), "成功");
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
