package com.example.net.server;

import com.example.net.conf.P2pNetConfig;
import org.springframework.stereotype.Component;
import org.tio.server.TioServer;
import org.tio.server.TioServerConfig;

import javax.annotation.PostConstruct;
import java.io.IOException;

// 启动 p2pserver

@Component
public class P2pServer {
    private final TioServerConfig tioServerConfig;
    private final P2pNetConfig p2pNetConfig;


    public P2pServer(P2pNetConfig p2pNetConfig, P2pServerHandler p2pServerHandler, P2pServerListener p2pServerListener) {
        TioServerConfig serverConfig = new TioServerConfig(P2pNetConfig.SERVER_NAME, p2pServerHandler, p2pServerListener);
        serverConfig.setHeartbeatTimeout(0);
        this.p2pNetConfig = p2pNetConfig;
        this.tioServerConfig = serverConfig;
    }

    @PostConstruct
    public void start() throws IOException {
        TioServer tioServer = new TioServer(tioServerConfig);
        tioServer.start(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort());
    }
}