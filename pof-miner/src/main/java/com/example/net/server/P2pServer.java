package com.example.net.server;

import com.example.base.entities.Node;
import com.example.base.entities.NodeType;
import com.example.base.utils.CryptoUtils;
import com.example.net.conf.P2pNetConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tio.server.TioServer;
import org.tio.server.TioServerConfig;

import javax.annotation.PostConstruct;
import java.io.IOException;

// 启动 p2pserver
@Component
public class P2pServer {
    private static final Logger logger = LoggerFactory.getLogger(P2pServer.class);

    private final TioServerConfig tioServerConfig;
    private final P2pNetConfig p2pNetConfig;
    private final Node me;

    public P2pServer(P2pNetConfig p2pNetConfig, P2pServerHandler p2pServerHandler, P2pServerListener p2pServerListener, Node me) {
        this.me = me;
        TioServerConfig serverConfig = new TioServerConfig(P2pNetConfig.SERVER_NAME, p2pServerHandler, p2pServerListener);
        serverConfig.setHeartbeatTimeout(0);
        this.p2pNetConfig = p2pNetConfig;
        this.tioServerConfig = serverConfig;
    }

    @PostConstruct
    public void start() throws IOException {
        String address = CryptoUtils.SHA256(p2pNetConfig.getServerAddress() + p2pNetConfig.getServerPort());
        me.setAddress(address);
        logger.info("Node: {}", me);
        TioServer tioServer = new TioServer(tioServerConfig);
        tioServer.start(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort());
    }

    public Node getMe() {
        return me;
    }

    public void setMeType(NodeType nodeType) {
        me.setType(nodeType);
    }
}
