package com.example.net.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinerConfig {
    @Value("${pof.repo}")
    private String repo;

    @Value("${pof.enable-mining}")
    private boolean enableMining;

    public boolean isEnableMining() {
        return enableMining;
    }

    public String getRepo() {
        return repo;
    }
}
