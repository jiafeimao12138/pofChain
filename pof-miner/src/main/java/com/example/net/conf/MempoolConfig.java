package com.example.net.conf;

import com.example.net.base.Mempool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MempoolConfig {
    @Bean(value = "mempool")
    public Mempool mempool() {
        return new Mempool();
    }
}
