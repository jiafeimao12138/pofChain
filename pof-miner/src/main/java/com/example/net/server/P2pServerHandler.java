package com.example.net.server;

import com.example.base.utils.SerializeUtils;
import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
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
                logger.info("hello message: {}", SerializeUtils.unSerialize(msgBody));
                responsePacket = serverHandler.helloMessage(msgBody);
                break;
        }
        if (responsePacket != null){
            Tio.send(channelContext, responsePacket);
        }
    }
}
