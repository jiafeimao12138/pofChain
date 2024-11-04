package com.example;

import com.example.base.entities.Block;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.CmdArgsParser;
import com.example.base.utils.SerializeUtils;
import com.example.web.service.BlockService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PofRunner {
    static final Logger logger = LoggerFactory.getLogger(PofRunner.class);

    private String repo;
    private RocksDBStore dbStore;
    private final CmdArgsParser parser;

    public PofRunner(String[] args) {
        parser = CmdArgsParser.getInstance(args);
        List<String> args1 = parser.getArgs();
        for (int i = 0; i < args1.size(); i++) {
            System.out.println(args1.get(i));
        }
        repo = parser.getOption("repo");
        if (StringUtils.isEmpty(repo)) {
            throw new IllegalArgumentException("repo is required");
        }
    }

    public boolean preparation() throws IOException {
        // 命令类型genesis、mine、init
        String commandType = parser.getArgs().get(0);
        Block block;
        File repo_dir = new File(repo);
        switch (commandType) {
            case "genesis" :
                logger.info("创世节点生成中。。。。");
                if (repo_dir.exists()) {
                    throw new RuntimeException(String.format("A genesis repo is already initialized in '%s'", repo_dir));
                }
                dbStore = new RocksDBStore(repo);
                block = generateGenesisBlock();

                logger.info("生成创世区块文件");
                String genesisFile = System.getProperty("user.dir") + "/genesis.car";
                byte[] bytes = SerializeUtils.serialize(block);
                FileOutputStream fos = new FileOutputStream(genesisFile);
                fos.write(bytes);
                fos.close();

                genPropertiesFile(parser);
                dbStore.close();
                break;

            case "mine" :
                logger.info("矿工节点加入中。。。。。");
                if (repo_dir.exists()) {
                    throw new RuntimeException(String.format("A miner repo is already initialized in '%s'", repo_dir));
                }
                dbStore = new RocksDBStore(repo);
                genPropertiesFile(parser);
                break;
        }
        return true;
    }

    public Block generateGenesisBlock() {

        Block genesisBlock = new Block();
        genesisBlock.setHashPreBlock("00000000000000");
        genesisBlock.setNBits(0x1d00ffff);
        genesisBlock.setNNonce(2083236893);
        genesisBlock.setTransactions(new ArrayList<>());
        genesisBlock.setNVersion(1);
        genesisBlock.setHashMerkleRoot("");
        genesisBlock.setHeight(1);
        genesisBlock.setNTime(System.currentTimeMillis());

        dbStore.put(BlockService.BLOCK_PREFIX + genesisBlock.GetHash(), genesisBlock);
        dbStore.put(BlockService.HEIGHT_PREFIX, genesisBlock.getHeight());

        logger.info("Successfully create genesis block and store in database. Hash is {}.", genesisBlock.GetHash());
        return genesisBlock;
    }

    // 生成配置文件
    private void genPropertiesFile(CmdArgsParser parser) throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty("server.address", parser.getOption("api.addr", "127.0.0.1"));
        properties.setProperty("server.port", parser.getOption("api.port", "8001"));
        properties.setProperty("pof.repo", repo);
//		默认可挖矿
        properties.setProperty("pof.enable-mining", parser.getOption("enable-mining", "false"));
        properties.setProperty("p2p.address", parser.getOption("p2p.addr", "127.0.0.1"));
        properties.setProperty("p2p.port", parser.getOption("p2p.port", "2345"));

        // load common properties
        properties.setProperty("genesis.address", "127.0.0.1");
        properties.setProperty("genesis.port", "2345");

        // disable tio logs
        properties.setProperty("logging.level.org.tio.server", "off");
        properties.setProperty("logging.level.org.tio.client", "off");

        properties.store(new FileWriter(repo + "/node.properties"), "Node config file");

    }
}
