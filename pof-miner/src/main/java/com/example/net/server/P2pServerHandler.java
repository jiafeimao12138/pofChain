package com.example.net.server;

import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.base.entities.Peer;
import com.example.base.entities.Program;
import com.example.base.entities.transaction.Transaction;
import com.example.base.utils.SerializeUtils;
import com.example.fuzzed.ProgramService;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class P2pServerHandler extends BaseTioHandler implements TioServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pServerHandler.class);

    private final MessageServerHandler serverHandler;
    private final ProgramService programService;
    private final Node node;

    @Value("${fuzzer.targetProgramDir}")
    private String targetProgramQueueDir;

    public P2pServerHandler(MessageServerHandler serverHandler, ProgramService programService, Node node) {
        this.serverHandler = serverHandler;
        this.programService = programService;
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
                if (type == NodeType.FUZZER) {
                    responsePacket = serverHandler.receiveNewBlock_fuzzer(msgBody);
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
            case MessagePacketType.REQ_BLOCK_HEADER:
                responsePacket = serverHandler.receiveGetBlockHeaderByHeight(msgBody);
                break;
            case MessagePacketType.REQ_HEIGHT:
                logger.info("处理主链最新高度请求");
                responsePacket = serverHandler.receiveHeightReq(msgBody);
                break;
            case MessagePacketType.PUBLISH_FILE:
                Program program = (Program) SerializeUtils.unSerialize(msgBody);
                logger.info("收到新待测程序, filesize:{}, node:{}", program.getProgramCode().length, node);
//                serverHandler.receiveFile(program, targetProgramQueueDir, "program_");
                programService.receiveProgram(program, targetProgramQueueDir);
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
            case MessagePacketType.BROADCAST_TX:
                logger.info("收到新交易");
                Transaction transaction = (Transaction) SerializeUtils.unSerialize(msgBody);
                boolean result = serverHandler.processNewTransaction(transaction);
                if (!result) {
                    logger.info("检查到交易不合法：{}", transaction.getTxIdStr());
                }
            default:
                logger.error("错误消息！！！");
        }
        logger.info("server回复client: channelContext:{}", channelContext);
        if (responsePacket != null){
            Tio.send(channelContext, responsePacket);
        }
    }
}
