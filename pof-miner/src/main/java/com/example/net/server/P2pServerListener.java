package com.example.net.server;

import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlocksEvent;
import com.example.web.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerListener;

@Component
public class P2pServerListener implements TioServerListener {

    private static final Logger logger = LoggerFactory.getLogger(P2pServerListener.class);

    @Override
    public boolean onHeartbeatTimeout(ChannelContext channelContext, Long aLong, int i) {
        return false;
    }

    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        if (isConnected) {
            logger.info("P2pServerListener.java : 连接新节点: {}", channelContext.getClientNode());
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

    }
}
