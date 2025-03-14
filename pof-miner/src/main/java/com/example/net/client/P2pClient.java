package com.example.net.client;

import com.example.base.entities.Peer;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.conf.P2pNetConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.client.ClientChannelContext;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.TioClientConfig;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.Tio;
import org.tio.server.ServerGroupStat;

import javax.annotation.PostConstruct;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

// 启动 p2pClient
@Component
public class P2pClient {
    private static final Logger logger = LoggerFactory.getLogger(P2pClient.class);

    private TioClient tioClient;
    private final TioClientConfig tioClientConfig;
    private final P2pNetConfig p2pNetConfig;
    private List<ClientChannelContext> channelContextList;
    private final List<Peer> peerList;
    private static final CountDownLatch latch = new CountDownLatch(1);



    public P2pClient(P2pNetConfig p2pNetConfig,
                     P2pClientHandler p2pClientHandler,
                     P2pClientListener p2pClientListener,
                     List<Peer> peerList) {
        this.peerList = peerList;
        this.channelContextList = new ArrayList<>();
        // autoReconnect
        ReconnConf reconnConf = new ReconnConf(5000L, 20);
        TioClientConfig tioClientConfig = new TioClientConfig(p2pClientHandler, p2pClientListener, reconnConf);
        // disable heartbeat from tio framework
        tioClientConfig.setHeartbeatTimeout(0);
        this.tioClientConfig = tioClientConfig;
        this.p2pNetConfig = p2pNetConfig;
    }

    public List<ClientChannelContext> getChannelContextList() {
        System.out.println(channelContextList);
        return channelContextList;
    }

    @PostConstruct
    public void start() throws Exception{
        this.tioClient = new TioClient(tioClientConfig);
        logger.info("P2pClient start()");
        // 资格检验
        if(checkIfSupportSGX())
            connect(new Node(p2pNetConfig.getGenesisAddress(), p2pNetConfig.getGenesisPort()));
        else
            logger.error("不支持intel sgx");
    }

    public boolean checkIfSupportSGX() {
        return true;
    }

    // 发送给组
    public void sendToGroup(MessagePacket messagePacket) {
        if (P2pNetConfig.SERVERS.size() > 0) {
            Tio.sendToGroup(tioClientConfig, P2pNetConfig.NODE_GROUP_NAME, messagePacket);
        }
    }

    // 点对点发送消息
    public void sendToNode(ClientChannelContext channelContext, MessagePacket messagePacket) {
        Tio.send(channelContext, messagePacket);
    }




    // 连接新节点
    public ClientChannelContext connect(Node node) throws Exception
    {
        if (StringUtils.equals(node.getIp(), p2pNetConfig.getServerAddress()) && node.getPort() == p2pNetConfig.getServerPort()) {
            logger.info("skip self connections, {}", node);
            return null;
        }

        // 如果这个节点已经连接了，则返回false
        if (P2pNetConfig.SERVERS.containsKey(node)) {
            return null;
        }
        P2pNetConfig.SERVERS.put(node, true);
        logger.info("开始连接节点{}:{}", node.getIp(), node.getPort());
        ClientChannelContext channelContext = tioClient.connect(node);
        Thread.sleep(5000);
        logger.info("clientChannelContext:{}", channelContext);
        // send self server connection info
        Peer server = new Peer(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort());
        MessagePacket newPeerpacket = new MessagePacket();
        newPeerpacket.setType(MessagePacketType.REQ_NEW_PEER);
        newPeerpacket.setBody(SerializeUtils.serialize(server));
        Tio.send(channelContext, newPeerpacket);
        logger.info("send peer connection info to {}", node);
//        MessagePacket hellopacket = new MessagePacket();
//        hellopacket.setType(MessagePacketType.HELLO_MESSAGE);
//            hellopacket.setBody(SerializeUtils.serialize(new Message("","","hello")));
//            Tio.send(channelContext, hellopacket);
//            logger.info("send hello message to {}", node);
        channelContextList.add(channelContext);
        peerList.add(new Peer(node.getIp(), node.getPort()));
        logger.info("peerList: {}", peerList);
        return channelContext;
    }

}
