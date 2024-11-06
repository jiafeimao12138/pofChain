package com.example.net.client;

import com.example.base.utils.SerializeUtils;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.base.PacketBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.intf.TioClientHandler;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

@Component
public class P2pClientHandler extends BaseTioHandler implements TioClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pClientHandler.class);

    public P2pClientHandler() {

    }

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
        switch (type) {
            case MessagePacketType.RES_NEW_BLOCK:
                PacketBody packetBody = (PacketBody) SerializeUtils.unSerialize(body);
                if (packetBody.isSuccess()) {
                    logger.info("对方成功接收区块");
                } else {
                    logger.info("对方拒绝接收该区块");
                }
                break;
            case MessagePacketType.RES_NEW_MESSAGE:
                PacketBody packetBody1 = (PacketBody) SerializeUtils.unSerialize(body);
                if (packetBody1.isSuccess()) {
                    logger.info("对方已接收到信息");
                }
                break;
        }
    }
}
