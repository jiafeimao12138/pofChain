package com.example.net.server;

import com.example.base.entities.Message;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.GetBlocksEvent;
import com.example.web.service.ChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.intf.TioServerListener;

@Component
public class P2pServerListener implements TioServerListener {

    private static final Logger logger = LoggerFactory.getLogger(P2pServerListener.class);

    @Override
    public boolean onHeartbeatTimeout(ChannelContext channelContext, Long aLong, int i) {
        return false;
    }

    // 客户端与服务器TCP握手完成后调用该方法
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        if (isConnected) {
            logger.info("P2pServerListener.java : 连接新节点: {};{}", channelContext.getServerNode(), channelContext.getClientNode());
            // 请求ProgramQueue

//            MessagePacket hellopacket = new MessagePacket();
//            hellopacket.setType(MessagePacketType.HELLO_MESSAGE);
//            Message msg = new Message("", "", "与服务器握手完成");
//            msg.setTimestamp(System.currentTimeMillis());
//            hellopacket.setBody(SerializeUtils.serialize(msg));
//            Tio.send(channelContext, hellopacket);
//            logger.info("send hello message：{}, to {}", msg, channelContext.getClientNode());
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
