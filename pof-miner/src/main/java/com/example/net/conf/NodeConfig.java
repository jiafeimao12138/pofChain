package com.example.net.conf;

import com.example.base.entities.NewPath;
import com.example.base.entities.Node;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;

@Configuration
public class NodeConfig {
    @Bean(value = "node")
    public Node node() {
        return new Node();
    }
    @Bean(value = "newPath")
    public NewPath newPath() {
        return new NewPath();
    }
    @Bean(value = "newPathMap")
    public HashMap<String, List<NewPath>> NewPathMap() {
        HashMap<String, List<NewPath>> map = new HashMap<>();
        return map;
    }
}
