package com.example;

import com.example.base.entities.block.Block;
import com.example.base.entities.block.BlockHeader;
import com.example.base.entities.transaction.TXInput;
import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;
import com.example.base.store.BlockPrefix;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.CmdArgsParser;
import com.example.base.utils.SerializeUtils;
import com.example.web.service.impl.FakeTXGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public boolean preparation() throws Exception {
        String commandType = parser.getArgs().get(0);
        Block block;
        Path repoPath = Paths.get(repo);
        switch (commandType) {
            case "genesis" :
                logger.info("创世节点启动中。。。。");

                if (Files.exists(repoPath) && Files.isDirectory(repoPath)) {
                    break;
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
                break;

            case "miner" :
                logger.info("节点加入中。。。。。");
                if (Files.exists(repoPath) && Files.isDirectory(repoPath)) {
                    break;
                }
                dbStore = new RocksDBStore(repo);
                generateGenesisBlock();
                genPropertiesFile(parser);
                dbStore.close();
                break;
        }
        return true;
    }

    // 递归删除目录及其内容
    public boolean deleteDirectory(File directory) {
        // 确保目录存在
        if (directory.exists()) {
            // 如果是目录，递归删除子文件和子目录
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteDirectory(file); // 递归删除
                    }
                }
            }
            // 删除空文件夹或文件
            return directory.delete();
        }
        return false; // 如果目录不存在
    }

    public Block generateGenesisBlock() {

        Block genesisBlock = new Block();
        BlockHeader genesisBlockHeader = new BlockHeader();
        genesisBlockHeader.setHashPreBlock("00000000000000");
        genesisBlockHeader.setNBits(0x1d00ffff);
        genesisBlockHeader.setNNonce(2083236893);
        genesisBlockHeader.setHashMerkleRoot("");
        genesisBlockHeader.setHeight(0l);
        genesisBlockHeader.setNTime(1742827391000l);
        genesisBlock.setBlockHeader(genesisBlockHeader);
        Transaction transaction = newCoinbaseTX("000000", 0, Transaction.BLOCK_REWARD);
        ArrayList<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        genesisBlock.setTransactions(transactions);
        genesisBlock.setBlockHash(genesisBlock.getHash());

        dbStore.put(BlockPrefix.BLOCK_HEIGHT_PREFIX.getPrefix() + genesisBlock.getBlockHeader().getHeight(), genesisBlock);
        dbStore.put(BlockPrefix.HEIGHT.getPrefix(), genesisBlock.getBlockHeader().getHeight());
        dbStore.put(BlockPrefix.BLOCK_HASH_PREFIX.getPrefix() + genesisBlock.getBlockHash(), genesisBlock);
        dbStore.close();

        logger.info("创世区块创建成功，Hash：{}.", genesisBlock.getBlockHash());
        return genesisBlock;
    }

    public Transaction newCoinbaseTX(String toAddress, long blockHeight, int blockReward) {
        Transaction coinBaseTX = new Transaction();
        // 创建交易输入
        byte[] coinBaseData = ByteBuffer.allocate(8).putLong(blockHeight).array();
        TXInput txInput = TXInput.coinbaseInput(coinBaseData);
        coinBaseTX.addInput(txInput);

        // 创建交易输出
        TXOutput txOutput = TXOutput.newTXOutput(blockReward, toAddress);
        coinBaseTX.addOutput(txOutput);
        coinBaseTX.setCreateTime(1742660358l);
        logger.info("coinBaseTX长度: {}", coinBaseTX.getInputs().get(0).getPreviousTXId().length);
        return coinBaseTX;
    }

    // 生成配置文件
    private void genPropertiesFile(CmdArgsParser parser) throws IOException
    {
        Properties properties = new Properties();
        properties.setProperty("server.address", parser.getOption("api.addr", "192.168.110.134"));
        properties.setProperty("server.port", parser.getOption("api.port", "8001"));
        properties.setProperty("pof.repo", repo);
//		默认可挖矿
        properties.setProperty("pof.enable-mining", parser.getOption("enable-mining", "true"));
        properties.setProperty("p2p.address", parser.getOption("p2p.addr", "192.168.110.134"));
        properties.setProperty("p2p.port", parser.getOption("p2p.port", "2345"));

        // load common properties
        properties.setProperty("genesis.address", parser.getOption("genesis.address", "192.168.110.134"));
        properties.setProperty("genesis.port", parser.getOption("genesis.port", "2345"));

        // disable tio logs
        properties.setProperty("logging.level.org.tio.server", "off");
        properties.setProperty("logging.level.org.tio.client", "off");

        properties.store(new FileWriter(repo + "/node.properties"), "Node config file");

    }
}
