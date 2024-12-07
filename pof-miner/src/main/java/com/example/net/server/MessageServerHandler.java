package com.example.net.server;

import com.example.base.entities.Block;
import com.example.base.entities.Message;
import com.example.base.entities.NewPath;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.fuzzed.ProgramService;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import com.example.net.base.PacketMsgType;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.net.events.NewPeerEvent;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import com.example.web.service.PeerService;
import com.example.web.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.core.Node;

import java.io.File;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;

// 处理其他node发送的message request
@Component
@RequiredArgsConstructor
public class MessageServerHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageServerHandler.class);

    private final PeerService peerService;
    private final ValidationService validationService;
    private final ChainService chainService;
    private final P2pClient p2pClient;
    private final ProgramService programService;


    public synchronized MessagePacket helloMessage(byte[] msgBody) {
        Message message = (Message) SerializeUtils.unSerialize(msgBody);
        PacketBody packetBody = new PacketBody(message, true);
        return null;
    }

    public synchronized void newPeerConnect(byte[] msgBody) throws Exception {
        Peer peer = (Peer) SerializeUtils.unSerialize(msgBody);
        // 如果该节点之前没有连过，将它添加到数据库中
        if (!peerService.hasPeer(peer)) {
            peerService.addPeer(peer);
        }
        // TODO：接收到hello消息的回复才认为连接成功
        if (p2pClient.connect(new Node(peer.getIp(),peer.getPort()))) {
            logger.info("已连接新节点：{}", peer);
            ApplicationContextProvider.publishEvent(new NewPeerEvent(peer));
        }
    }

    //处理接收到的新区块
    public synchronized MessagePacket receiveNewBlock(byte[] msgBody) {
        Block newBlock = (Block) SerializeUtils.unSerialize(msgBody);
        if (!validationService.processNewMinedBlock(newBlock)) {
            logger.info("校验新区块失败, hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
            return buildPacket(MessagePacketType.RES_NEW_BLOCK, new PacketBody(newBlock, false), "校验新区块失败");
        }
        logger.info("校验新区块成功并存入数据库，hash={}, height={}", newBlock.getHash(), newBlock.getBlockHeader().getHeight());
        // 广播给其他peer
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
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

    public synchronized void receiveFile(MutablePair<byte[], Peer> nodePair, String path, String name) {
        Peer node = nodePair.getRight();
        logger.info("收到file，长度为{}; 发送方：{}:{}", nodePair.getLeft().length, node.getIp(), node.getPort());
        // 将node存入数据库
        if(peerService.addSupplierPeer(nodePair.getRight())){
            logger.info("supplier信息存入数据库");
        }
        // 收到program后，放入队列
        if(programService.addProgramQueue(nodePair)){
            ArrayDeque<MutablePair<byte[], Peer>> queue = programService.getProgramQueue();
            for (MutablePair<byte[], Peer> peerMutablePair : queue) {
                logger.info("队列中内容：文件长度为{},node为{}",peerMutablePair.getLeft().length,peerMutablePair.getRight());
            }
        }
//        programService.byteToFile(fileByte, path, name);
    }

    public synchronized void receiveNewPathRank(byte[] msgBody) {
        logger.info("收到本轮新路径排名");
        LinkedHashMap<String, List<NewPath>> rank =
                (LinkedHashMap<String, List<NewPath>>) SerializeUtils.unSerialize(msgBody);
        rank.entrySet().stream().forEach(stringListEntry ->
                System.out.println(stringListEntry.getKey() + ":" + stringListEntry.getValue()));
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
