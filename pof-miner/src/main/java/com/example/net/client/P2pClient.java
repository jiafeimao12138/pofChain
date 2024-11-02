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
import org.tio.core.Node;
import org.tio.core.Tio;

import javax.annotation.PostConstruct;

// 启动 p2pClient
@Component
public class P2pClient {
    private static final Logger logger = LoggerFactory.getLogger(P2pClient.class);

    private TioClient tioClient;
    private final TioClientConfig tioClientConfig;
    private final P2pNetConfig p2pNetConfig;
    public P2pClient(P2pNetConfig p2pNetConfig, P2pClientHandler p2pClientHandler, P2pClientListener p2pClientListener) {
        // autoReconnect
        ReconnConf reconnConf = new ReconnConf(5000L, 20);
        TioClientConfig tioClientConfig = new TioClientConfig(p2pClientHandler, p2pClientListener, reconnConf);
        // disable heartbeat from tio framework
        tioClientConfig.setHeartbeatTimeout(0);
        this.tioClientConfig = tioClientConfig;
        this.p2pNetConfig = p2pNetConfig;
    }
    @PostConstruct
    public void start() throws Exception{
        this.tioClient = new TioClient(tioClientConfig);
        //连接创世节点
        // TODO：如果连接创世节点失败了，尝试连接其他节点。或者给出一个列表什么的用来连接，参考下别的区块连是怎么做的
        connect(new Node(p2pNetConfig.getGenesisAddress(), p2pNetConfig.getGenesisPort()));
    }

    public void sendToGroup(MessagePacket messagePacket) {
        if (P2pNetConfig.SERVERS.size() > 0) {
            Tio.sendToGroup(tioClientConfig, P2pNetConfig.NODE_GROUP_NAME, messagePacket);
        }
    }

    // 连接新节点
    public boolean connect(Node node) throws Exception
    {
        if (StringUtils.equals(node.getIp(), p2pNetConfig.getServerAddress()) && node.getPort() == p2pNetConfig.getServerPort()) {
            logger.info("skip self connections, {}", node.toString());
            return false;
        }

        // 如果这个节点已经连接了，则返回false
        if (P2pNetConfig.SERVERS.containsKey(node)) {
            return false;
        }

        P2pNetConfig.SERVERS.put(node, true);
        ClientChannelContext channelContext = tioClient.connect(node);

        // send self server connection info
        Peer server = new Peer(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort());
        MessagePacket packet = new MessagePacket();
        packet.setType(MessagePacketType.REQ_NEW_PEER);
        packet.setBody(SerializeUtils.serialize(server));
        Tio.send(channelContext, packet);
        return true;
    }


}
