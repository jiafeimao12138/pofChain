package com.example.net.client;

import com.example.base.utils.SerializeUtils;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.intf.TioClientHandler;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.intf.Packet;

@Component
@RequiredArgsConstructor
public class P2pClientHandler extends BaseTioHandler implements TioClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pClientHandler.class);
    private final MessageClientHandler clientHandler;

    @Override
    public Packet heartbeatPacket(ChannelContext channelContext) {
        return null;
    }

    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception {
        MessagePacket messagePacket = (MessagePacket) packet;
        byte[] body = messagePacket.getBody();
        byte type = messagePacket.getType();
        if (body == null) {
            logger.debug("null msg body, client: {}, drop it.", channelContext.getClientNode());
            return;
        }
        PacketBody packetBody;
        switch (type) {
            case MessagePacketType.HELLO_MESSAGE:
                logger.info("收到server握手消息:{}，来自：{}", SerializeUtils.unSerialize(body), channelContext);
                break;
            case MessagePacketType.RES_NEW_BLOCK:
                packetBody = (PacketBody) SerializeUtils.unSerialize(body);
                if (packetBody.isSuccess()) {
                    logger.info("对方成功接收区块");
                } else {
                    logger.info("对方拒绝接收该区块");
                }
                break;
            case MessagePacketType.RES_NEW_MESSAGE:
                packetBody = (PacketBody) SerializeUtils.unSerialize(body);
                if (packetBody.isSuccess()) {
                    logger.info("对方已接收到信息");
                }
                break;
            case MessagePacketType.RES_BLOCK_BY_HEIGHT:
                clientHandler.receiveGetBlockByHeightRes(body);
                break;
            case MessagePacketType.RES_HEIGHT:
                clientHandler.receiveHeight(body);
                break;
        }
    }
}
