package com.example.net.server;

import com.example.base.entities.Block;
import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class P2pServerHandler extends BaseTioHandler implements TioServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pServerHandler.class);

    private final MessageServerHandler serverHandler;

    @Value("${targetProgramQueueDir}")
    private String targetProgramQueueDir;

    public P2pServerHandler(MessageServerHandler serverHandler) {
        this.serverHandler = serverHandler;
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
                responsePacket = serverHandler.receiveNewBlock(msgBody);
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
                logger.info("收到新待测程序, node:{}", node);
                serverHandler.receiveFile(nodePair, targetProgramQueueDir, "program_");
                break;
            case MessagePacketType.NEW_PATH_RANK:
                // @TODO 实时推送给前端
                serverHandler.receiveNewPathRank(msgBody);
                break;
        }
        logger.info("server回复client: channelContext:{}", channelContext);
        if (responsePacket != null){
            Tio.send(channelContext, responsePacket);
        }
    }
}
