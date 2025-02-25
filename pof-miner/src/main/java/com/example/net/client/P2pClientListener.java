package com.example.net.client;

import com.example.base.entities.Message;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.conf.P2pNetConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.intf.TioClientListener;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;

@Component
@RequiredArgsConstructor
public class P2pClientListener implements TioClientListener {

    private static final Logger logger = LoggerFactory.getLogger(P2pClientListener.class);

    // 这里的channelContext是发消息给它的server的，不管是本节点的server还是其他节点的server
    @Override
    public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
        if (isConnected) {
            logger.info("Connect server {} successfully", channelContext);
            // bind peer to group
            Tio.bindGroup(channelContext, P2pNetConfig.NODE_GROUP_NAME);
            logger.info("成功绑定节点至 group: " + P2pNetConfig.NODE_GROUP_NAME);
//            MessagePacket hellopacket = new MessagePacket();
//            hellopacket.setType(MessagePacketType.HELLO_MESSAGE);
//            // 向连接的server发送握手消息
//            Message message = new Message("", "", "我是客户端");
//            message.setTimestamp(System.currentTimeMillis());
//            logger.info("send msg: {}, to {}", message, channelContext);
//            hellopacket.setBody(SerializeUtils.serialize(message));
//            Tio.send(channelContext, hellopacket);
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

    // 断开连接
    @Override
    public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String s, boolean b) throws Exception {
        Tio.unbindGroup(P2pNetConfig.NODE_GROUP_NAME, channelContext);
    }
}
