package com.example.net.server;

import com.example.base.entities.Block;
import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

@Component
public class P2pServerHandler extends BaseTioHandler implements TioServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pServerHandler.class);

    private final MessageServerHandler serverHandler;
    private final Node node;

    @Value("${targetProgramQueueDir}")
    private String targetProgramQueueDir;

    public P2pServerHandler(MessageServerHandler serverHandler, Node node) {
        this.serverHandler = serverHandler;
        this.node = node;
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        MessagePacket messagePacket = (MessagePacket) packet;
        byte msgType = messagePacket.getType();
        byte[] msgBody = messagePacket.getBody();
        if (msgBody == null) {
            logger.debug("P2pServerHandler.java : null message body, client: {}, drop it.", channelContext.getClientNode());
            return;
        }

        MessagePacket responsePacket = null;
        switch (msgType){
            case MessagePacketType.HELLO_MESSAGE:
                logger.info("收到client握手消息: {}", SerializeUtils.unSerialize(msgBody));
                responsePacket = serverHandler.helloMessage(msgBody);
                break;
            case MessagePacketType.REQ_NEW_PEER:
                logger.info("收到REQ_NEW_PEER消息");
                serverHandler.newPeerConnect(msgBody);
                break;
            case MessagePacketType.REQ_NEW_BLOCK:
                logger.info("处理接收到的新区块");
                // 根据节点类型选择不同操作
                NodeType type = node.getType();
                String address = node.getAddress();
                if (type == NodeType.FUZZER) {
                    responsePacket = serverHandler.receiveNewBlock(msgBody, address);
                } else if (type == NodeType.OBSERVER){
                    responsePacket = serverHandler.receiveNewBlock_observer(msgBody);
                } else {
                    responsePacket = serverHandler.receiveNewBlock_supplier(msgBody);
                }
                break;
            case MessagePacketType.REQ_NEW_MESSAGE:
                logger.info("处理接收到的新消息");
                responsePacket = serverHandler.receiveNewMsg(msgBody);
                break;
            case MessagePacketType.REQ_BLOCK_BY_HEIGHT:
                responsePacket = serverHandler.receiveGetBlockByHeight(msgBody);
                break;
            case MessagePacketType.REQ_HEIGHT:
                logger.info("处理主链最新高度请求");
                responsePacket = serverHandler.receiveHeightReq(msgBody);
                break;
            case MessagePacketType.PUBLISH_FILE:
                MutablePair<byte[], Peer> nodePair = (MutablePair<byte[], Peer>) SerializeUtils.unSerialize(msgBody);
                byte[] fileByte = nodePair.getLeft();
                Peer node = nodePair.getRight();
                logger.info("收到新待测程序, filesize:{}, node:{}", fileByte.length, node);
                serverHandler.receiveFile(nodePair, targetProgramQueueDir, "program_");
                break;
            case MessagePacketType.NEW_PATH_RANK:
                // @TODO 实时推送给前端
                serverHandler.receiveNewPathRank(msgBody);
                break;
            case MessagePacketType.PAYLOADS_SUBMIT:
                if(serverHandler.processPayloads(msgBody, System.currentTimeMillis())) {
                    logger.info("成功接收payloads");
                }else {
                    logger.info("丢弃payloads");
                }
                break;
            case MessagePacketType.PROGRAM_QUEUQ_REQ:
                responsePacket = serverHandler.responseProgramQueue();
                logger.info("ProgramQueue请求已处理");
                break;
            case MessagePacketType.TERMINATE_FUZZING:
                logger.info("收到终止AFL消息");
                serverHandler.terminatingAFL();
                break;
        }
        logger.info("server回复client: channelContext:{}", channelContext);
        if (responsePacket != null){
            Tio.send(channelContext, responsePacket);
        }
    }
}
