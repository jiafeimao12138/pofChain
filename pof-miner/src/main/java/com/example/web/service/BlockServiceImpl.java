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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
    private int hitCount = 0;
    long endWindow = 0;
    long lastWindowEnd = System.currentTimeMillis();

    Path path = Paths.get("output.txt");
    //TODO: 每挖出x个区块更改一次head，类比bitcoin

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
        //区间
        List<BigInteger> interval = generateRandomHashHead(6);
        BigInteger head = interval.get(0);
        BigInteger end = interval.get(1);
        try {
            String content = "动态区间：[" + head.toString() + "," + end.toString() + "]\n";
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
                if (line.contains("this window end")){
                    List<Transaction> transactions = new ArrayList<>();
                    try {
                        List<Payload> triples;
                        triples = WindowFileUtils.windowFilesToTriple(
                                "/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + num,
                                "/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + num);
                        num ++;
                        // 每20个文件清理一次
                        if(num % 20 == 1) {
                            for (int i = 1; i <= 20; i++) {
                                deleteFile("/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + (num- 21 + i));
                                deleteFile("/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + (num- 21 + i));
                            }
//                            logger.info("本次清理完毕, num={}", num);
                        }
                        Block preBlock = getLatestBlock();
                        Block newBlock = computeWindowHash(preBlock, transactions, triples);
                        String newHash = newBlock.GetHash();


                        if(isInInterval(newHash, head, end)) {
                            hitCount ++;
                            logger.info("hitCount = {}, totalWindowNum = {}", hitCount, num-1);
                            logger.info("挖矿成功，新区块高度为{}，hash={}，前一个区块hash={}",
                                    newBlock.getHeight(), newHash,newBlock.getHashPreBlock());
                            // 广播
//                        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
//                        logger.info("广播新Block,hash={}", newBlock.GetHash());
                            storeBlock(newBlock);
                            rocksDBStore.get(BLOCK_PREFIX + newBlock.getHeight());
//                        logger.info("存入新区块，高度为{}，hash={}", newBlock.getHeight(), newBlock.GetHash());
                            endWindow = System.currentTimeMillis();
                            logger.info("endWindow: {}", endWindow);
                            long time = endWindow - lastWindowEnd;
                            lastWindowEnd = endWindow;
                            Files.write(path, Long.toString(time).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.write(path, "not hit".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        }
                        BigInteger newHashInteger = new BigInteger(newHash, 16);
                        String content = "," + hitCount + "," + (num-1) + "," + newHashInteger + "\n";
                        Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        triples.clear();
                    } catch (WindowFileException e) {
                        throw new RuntimeException(e);
                    }
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

    public boolean isInInterval(String hash, BigInteger head, BigInteger end) {
        BigInteger hashInteger = new BigInteger(hash, 16);
        logger.info("hash: {}", hashInteger);
        // 在[head,end]中
        if(hashInteger.compareTo(head) >= 0 && hashInteger.compareTo(end) <= 0 ) {
            return true;
        }
        //不在[head,end]中
        return false;
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
//        logger.info("存入新区块，高度为{}，hash={}", newBlock.height, newBlock.GetHash());
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
        logger.info("newBlock hash: {}", newBlock.GetHash());
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

    @Override
    public boolean AFLswitchRoot() {
        try {
            // 指定脚本的路径
            ProcessBuilder processBuilder = new ProcessBuilder("./switchRoot.sh");
            // 启动进程
            Process process = processBuilder.start();
            // 获取脚本输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待脚本执行完成
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径只有单个文件
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public List<BigInteger> generateRandomHashHead(int bit) {
        // 创建安全随机数生成器
        SecureRandom secureRandom = new SecureRandom();
        // 生成一个 256 位的随机数（32 字节）
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        // 将随机字节转换为 BigInteger
        BigInteger head = new BigInteger(1, randomBytes);
        // 计算掩码 2^bit - 1
        BigInteger maxLimit = BigInteger.ONE.shiftLeft(bit).subtract(BigInteger.ONE);
        // 使用按位与操作确保结果小于 2^bit
        head = head.and(maxLimit);
        // 区间长度是 2^(256-bit)
        BigInteger intervalLength = BigInteger.valueOf(1).shiftLeft((256-bit));
        //区间尾
        BigInteger end = head.add(intervalLength);
        List<BigInteger> res = new ArrayList<>();
        res.add(head);
        res.add(end);
        return res;
    }
}
