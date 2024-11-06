package com.example.web.service;


import com.example.base.Exception.IllegalBlockException;
import com.example.base.Exception.WindowFileException;
import com.example.base.entities.Block;
import com.example.base.entities.Payload;
import com.example.base.entities.Transaction;
import com.example.base.store.DBStore;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.WindowFileUtils;
import com.example.miner.Miner;
import com.example.miner.chain.Chain;
import com.example.miner.pof.PoFMiner;
import com.example.miner.pof.ProofOfFuzzing;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:28
 */

@Service
public class BlockServiceImpl implements BlockService{

    private static final Logger logger = LoggerFactory.getLogger(BlockServiceImpl.class);

    private final DBStore rocksDBStore;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public BlockServiceImpl(DBStore dbStore) {
        this.rocksDBStore = dbStore;
    }

    @Override
    public void startMining() {
        new Thread(() -> {
            logger.info("开始进行Fuzzing挖矿");
            try {
//                Block preBlock = getLatestBlock();
//                miner.mineAndFuzzing(preBlock);
//                ProofOfFuzzing proofOfFuzzing = ProofOfFuzzing.newProofOfFuzzing(preBlock);
//                proofOfFuzzing.run();
                executeCommand();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void executeCommand() {
        ProcessBuilder processBuilder = new ProcessBuilder();
//        指定工作目录
        processBuilder.directory(new java.io.File("/home/wj/pofChain/AFL"));

        // Linux command
        processBuilder.command("afl-fuzz", "-i", "fuzz_in/", "-o", "fuzz_out", "./afl_testfiles/objfiles/string_length");

        try {
            Process process = processBuilder.start();

            // 获取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int num = 1;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("new window start")){
                    List<Transaction> transactions = new ArrayList<>();
                    List<Payload> triples;
                    try {
                        triples = WindowFileUtils.windowFilesToTriple(
                                "/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + num,
                                "/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + num);
                        num ++;
                    } catch (WindowFileException e) {
                        throw new RuntimeException(e);
                    }
                    Block preBlock = getLatestBlock();
                    Block newBlock = computeWindowHash(preBlock, transactions, triples);
                    logger.info("挖矿成功，新区块高度为{}，hash={}，前一个区块hash={}",
                            newBlock.getHeight(), newBlock.GetHash(),newBlock.getHashPreBlock());
                    // 广播
                    ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
                    logger.info("广播新Block,hash={}", newBlock.GetHash());
                    storeBlock(newBlock);
                    rocksDBStore.get(BLOCK_PREFIX + newBlock.getHeight());
//                    rocksDBStore.close();
                    logger.info("存入新区块，高度为{}，hash={}", newBlock.getHeight(), newBlock.GetHash());

                }
            }

            // 等待命令执行完毕
            int exitCode = process.waitFor();
            System.out.println("\nExited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (IllegalBlockException e) {
            throw new RuntimeException(e);
        }
    }

    public Block computeWindowHash(Block preBlock, List<Transaction> transactionList, List<Payload> triples){
        Random random = new Random();
        long nonce = random.nextLong();
        long timestamp = System.currentTimeMillis();
        long height = preBlock.getHeight() + 1;

        Block newBlock = new Block();
        newBlock.setNVersion(1);
        newBlock.setTransactions(transactionList);
        newBlock.setHashPreBlock(preBlock.GetHash());
        newBlock.setNTime(timestamp);
        newBlock.setNNonce(nonce);
        newBlock.setHeight(height);
        newBlock.setTriples(triples);
        newBlock.setHashMerkleRoot("");
        String sha256 = newBlock.GetHash();
        System.out.println("SHA256: " + sha256);
        return newBlock;
    }

    //存储新区块到本地区块链中
    public boolean storeBlock(Block newBlock) {
        if (!rocksDBStore.put(BLOCK_PREFIX + newBlock.height, newBlock)) {
            logger.info("存入新区块失败");
            return false;
        }
        if (!rocksDBStore.put(HEIGHT, newBlock.height)){
            logger.info("存入最新高度失败");
            return false;
        }

        logger.info("存入新区块，高度为{}，hash={}", newBlock.height, newBlock.GetHash());
        return true;
    }

    // 获取本地区块链中最新的链
    public Block getLatestBlock() throws IllegalBlockException {
        Block block = new Block();
        Optional<Object> o = rocksDBStore.get(HEIGHT);
        if (o.isPresent()) {
            long height = (long)o.get();
            block = getBlockByHeight(height);
            return block;
        }
        return block;
    }

    @Override
    public boolean validateNewBlock(Block newBlock) {
        return true;
    }

    @Override
    public void testBlocks() {

    }

    // 根据高度查询区块
    @Override
    public Block getBlockByHeight(long height) throws IllegalBlockException {
        Optional<Object> o = rocksDBStore.get(BLOCK_PREFIX + height);
        if (o.isPresent()) {
            Block block = (Block) o.get();
            return block;
        } else {
            throw new IllegalBlockException("不存在该高度的区块");
        }
    }

    @Override
    public Block getBlockByHash(String hash) {
        return null;
    }
}
