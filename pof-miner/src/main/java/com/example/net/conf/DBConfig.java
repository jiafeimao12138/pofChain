package com.example.net.conf;

import com.example.base.store.DBStore;
import com.example.base.store.RocksDBStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DBConfig {
    @Bean(value = "dbStore")
    public DBStore dbStore(MinerConfig minerConfig){
        return new RocksDBStore(minerConfig.getRepo());
    }
}
