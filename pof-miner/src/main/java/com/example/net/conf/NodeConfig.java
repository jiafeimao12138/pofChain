package com.example.net.conf;

import com.example.base.entities.Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfig {
    @Bean(value = "node")
    public Node node() {
        return new Node();
    }
}
