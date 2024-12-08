package com.example.web.service.impl;


import com.example.base.Exception.WindowFileException;
import com.example.base.entities.*;
import com.example.base.store.BlockPrefix;
import com.example.base.store.DBStore;
import com.example.base.utils.SerializeUtils;
import com.example.base.utils.WindowFileUtils;
import com.example.fuzzed.ProgramService;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.web.service.ChainService;
import com.example.web.service.MiningService;
import com.example.web.service.PeerService;
import com.example.web.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tio.client.ClientChannelContext;
import org.tio.core.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:28
 */

@Service
@RequiredArgsConstructor
public class MiningServiceImpl implements MiningService {

    private static final Logger logger = LoggerFactory.getLogger(MiningServiceImpl.class);

    private final DBStore rocksDBStore;
    private final ChainService chainService;
    private final ProgramService programService;
    private final PeerService peerService;
    private final ValidationService validationService;
    private final P2pClient p2pClient;
    // 存储中间值
    private final Payloads payloads;
    private List<Payload> triples;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();
    private int hitCount = 0;
    long endWindow = 0;
    long lastWindowEnd = System.currentTimeMillis();

    @Value("${targetProgramQueueDir}")
    private String targetProgramQueueDir;

    Path path = Paths.get("output.txt");
    //TODO: 每挖出x个区块更改一次head，类比bitcoin

    @Override
    public void startMining() {
        new Thread(() -> {
            logger.info("开始进行新一轮Fuzzing挖矿");
            try {
                String tobeFuzzedPath = programService.chooseTargetProgram(targetProgramQueueDir);
                if (tobeFuzzedPath == null) {
                    logger.info("待测程序队列为空，当前无待测程序");
                }
                executeCommand(tobeFuzzedPath, peerService.getSupplierPeer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void executeCommand(String targetProgram, Peer supplier) {
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
        processBuilder.command("afl-fuzz", "-i", "fuzz_in/", "-o", "fuzz_out", targetProgram);

        try {
            Process process = processBuilder.start();
            // 获取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            int num;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("this window end")){
                    String[] nums = line.split("num=");
                    num = Integer.parseInt(nums[1]);
                    List<Transaction> transactions = new ArrayList<>();
                    try {
                        triples = WindowFileUtils.windowFilesToTriple(
                                "/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + num,
                                "/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + num);
                        // 向中间值添加本轮挖矿的path信息，等到新区块成功挖出后再置空
                        payloads.addPayloads(triples);
                        logger.info("本次处理文件num={}", num);
                        // 每20个文件清理一次
                        if(num % 20 == 1) {
                            for (int i = 1; i <= 20; i++) {
                                deleteFile("/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + (num- 21 + i));
                                deleteFile("/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + (num- 21 + i));
                            }
//                            logger.info("本次清理完毕, num={}", num);
                        }
                        Block preBlock = chainService.getLocalLatestBlock();
                        Block newBlock = computeWindowHash(preBlock, transactions, triples);
                        String newHash = newBlock.getHash();

                        logger.info("新区块中的payload长度为：{}", newBlock.getBlockHeader().getTriples().size());
                        // 当命中区间
                        if(isInInterval(newHash, head, end)) {
                            whenmined(newBlock, newHash);
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
        } 
    }

    public Block computeWindowHash(Block preBlock, List<Transaction> transactionList, List<Payload> triples){
        Random random = new Random();
        long nonce = random.nextLong();
        long timestamp = System.currentTimeMillis();
        long height = preBlock.getBlockHeader().getHeight() + 1;

        Block newBlock = new Block();
        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setNVersion(1);
        blockHeader.setHashPreBlock(preBlock.getHash());
        blockHeader.setNTime(timestamp);
        blockHeader.setNNonce(nonce);
        blockHeader.setHeight(height);
        blockHeader.setTriples(triples);
        blockHeader.setHashMerkleRoot("");

        newBlock.setBlockHeader(blockHeader);
        newBlock.setTransactions(transactionList);
        String sha256 = newBlock.getHash();
        System.out.println("SHA256: " + sha256);
        return newBlock;
    }

    public boolean isInInterval(String hash, BigInteger head, BigInteger end) {
        BigInteger hashInteger = new BigInteger(hash, 16);
        // 在[head,end]中
        if(hashInteger.compareTo(head) >= 0 && hashInteger.compareTo(end) <= 0 ) {
            return true;
        }
        //不在[head,end]中
        return false;
    }

    // 当寻找到满足要求的区块后
    public void whenmined(Block newBlock, String newHash) throws IOException {
        hitCount ++;
        logger.info("挖矿成功，新区块高度为{}，hash={}，前一个区块hash={}",
                newBlock.getBlockHeader().getHeight(), newHash,newBlock.getBlockHeader().getHashPreBlock());
        // 广播
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
        logger.info("广播新Block,hash={}", newHash);
        endWindow = System.currentTimeMillis();
        logger.info("endWindow: {}", endWindow);
        long time = endWindow - lastWindowEnd;
        lastWindowEnd = endWindow;
        Files.write(path, Long.toString(time).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        // 校验新区块
        if (validationService.processNewMinedBlock(newBlock)) {
            // 提交给supplier
            payloads.setNewBlock(newBlock);
            // @TODO: fuzzerAddress获取
            String address = "";
            payloads.setAddress(address);
            MessagePacket messagePacket = new MessagePacket();
            messagePacket.setType(MessagePacketType.PAYLOADS_SUBMIT);
            messagePacket.setBody(SerializeUtils.serialize(payloads));
            // 发送给supplier
            Peer supplier = peerService.getSupplierPeer();
            List<ClientChannelContext> channelContextList = p2pClient.getChannelContextList();
            // 在维护的列表中查找supplier
            for (ClientChannelContext channelContext : channelContextList) {
                Node serverNode = channelContext.getServerNode();
                if (serverNode.getIp().equals(supplier.getIp()) && serverNode.getPort() == supplier.getPort()) {
                    // supplier在列表中，直接发送消息即可
                    p2pClient.sendToNode(channelContext, messagePacket);
                    payloads.setNull();
                }
            }
            // 如果没查到，重新连接
            try {
                ClientChannelContext channelContext = p2pClient.connect(new Node(supplier.getIp(), supplier.getPort()));
                p2pClient.sendToNode(channelContext, messagePacket);
                payloads.setNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
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

    @Override
    public List<Payload> getPayloads() {
        List<Payload> payloads1 = payloads.getPayloads();
        return payloads1;
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
