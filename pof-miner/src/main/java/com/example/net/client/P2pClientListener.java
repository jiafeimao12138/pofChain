package com.example.net.client;

import com.example.net.conf.P2pNetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.intf.TioClientListener;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;

@Component
public class P2pClientListener implements TioClientListener {

    private static final Logger logger = LoggerFactory.getLogger(P2pClientListener.class);

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        if (isConnected) {
            logger.info("P2pClientListener.java : Connect server {} successfully", channelContext.getServerNode());
            // bind peer to group
            Tio.bindGroup(channelContext, P2pNetConfig.NODE_GROUP_NAME);
        }
    }

    @Override
    public void onAfterDecoded(ChannelContext channelContext, Packet packet, int i) throws Exception {

    }

    @Override
    public void onAfterReceivedBytes(ChannelContext channelContext, int i) throws Exception {

    }

    @Override
    public void onAfterSent(ChannelContext channelContext, Packet packet, boolean b) throws Exception {

    }

    @Override
    public void onAfterHandled(ChannelContext channelContext, Packet packet, long l) throws Exception {

    }

    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String s, boolean b) throws Exception {
        Tio.unbindGroup(P2pNetConfig.NODE_GROUP_NAME, channelContext);
    }
}
