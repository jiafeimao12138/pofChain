package com.example.net.client;

import com.example.net.base.BaseTioHandler;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.intf.TioClientHandler;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

@Component
public class P2pClientHandler extends BaseTioHandler implements TioClientHandler {

    private static final Logger logger = LoggerFactory.getLogger(P2pClientHandler.class);
    private final MessageClientHandler clientHandler;

    public P2pClientHandler(MessageClientHandler clientHandler) {
        this.clientHandler = clientHandler;
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
//        switch (type) {
//            case MessagePacketType.
//        }
    }
}
