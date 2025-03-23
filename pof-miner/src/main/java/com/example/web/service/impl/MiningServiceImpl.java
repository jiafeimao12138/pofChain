package com.example.web.service.impl;


import com.example.base.Exception.WindowFileException;
import com.example.base.entities.*;
import com.example.base.entities.block.Block;
import com.example.base.entities.block.BlockHeader;
import com.example.base.entities.transaction.Transaction;
import com.example.base.utils.SerializeUtils;
import com.example.base.utils.WindowFileUtils;
import com.example.exception.TransactionNotExistException;
import com.example.fuzzed.ProgramService;
import com.example.net.base.Mempool;
import com.example.net.base.MessagePacket;
import com.example.net.base.MessagePacketType;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.net.events.TerminateAFLEvent;
import com.example.web.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.tio.client.ClientChannelContext;
import org.tio.core.Node;

import javax.annotation.PostConstruct;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.*;

/**
 * @author jiafeimao
 * @date 2024年09月16日 20:28
 */

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class MiningServiceImpl implements MiningService {

    private static final Logger logger = LoggerFactory.getLogger(MiningServiceImpl.class);

    private final ChainService chainService;
    private final ProgramService programService;
    private final ValidationService validationService;
    private final P2pClient p2pClient;
    // 存储中间值
    private final PayloadManager payloadManager;
    private final ProgramQueue programQueue;
    private CopyOnWriteArrayList<Payload> triples;
    private final com.example.base.entities.Node node1;
    private final Mempool mempool;
    private final TransactionService transactionService;
    private final WalletService walletService;

    private int hitCount = 0;
    private long endWindow = 0;
    private long lastWindowEnd = System.currentTimeMillis();
    private Peer supplier;

    @Value("${fuzzer.targetProgramDir}")
    private String targetProgramQueueDir;
    @Value("${fuzzer.fuzz_out}")
    private String fuzzOut;
    @Value("${fuzzer.fuzz_in}")
    private String fuzzIn;
    @Value("${fuzzer.windowFiles}")
    private String windowFiles;
    @Value("${fuzzer.output}")
    private String output;
    @Value("${afl.directory}")
    private String aflDirectory;
    @Value("${fuzzer.window.record}")
    private String recordFile;
    @Value("${wallet.address}")
    private String addressFilePath;
    @Value("${afl.pathfile}")
    private String testcasefile;
    @Value("${enclave.path}")
    private String enclavePath;

    Path path = null;
    //TODO: 每挖出x个区块更改一次head，类比bitcoin
    @PostConstruct
    public void readProperties() {
        System.out.println("readProperties");
        System.out.println(fuzzOut);
        System.out.println(fuzzIn);
        System.out.println(windowFiles);
        System.out.println(output);
        System.out.println(aflDirectory);
    }

    @Override
    public void startMining() {

        path = Paths.get(output);
//        if (!deleteAFLFiles()) {
//            logger.info("删除AFL文件错误");
//            return;
//        }
        Path signalPath = Paths.get("/home/wj/dockerAFLdemo/pofChain/start_java_signal.txt");
        try {
            Files.write(signalPath, new byte[0], StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            logger.info("开始进行Fuzzing");
            try {
                Pair<String, Peer> targetProgram;
                CopyOnWriteArrayList<Program> queue = programQueue.getProgramList();
                // 如果不存在program文件夹就生成
                File directory = new File(targetProgramQueueDir);
                directory.mkdirs();
                targetProgram = programService.chooseTargetProgram(targetProgramQueueDir, queue);
                if (targetProgram == null) {
                    logger.info("待测程序队列为空，当前无待测程序");
                } else {
                    String tobeFuzzedPath = targetProgram.getLeft();
                    supplier = targetProgram.getRight();
                    executeCommand(tobeFuzzedPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            int exec_time = 0;
            logger.info("开始监控窗口文件");
            //@TODO: 动态调整难度
            List<BigInteger> interval = generateRandomHashHead(5);
            BigInteger head = interval.get(0);
            BigInteger end = interval.get(1);
            try {
                String content = "动态区间：[" + head.toString() + "," + end.toString() + "]\n";
                Files.write(path, content.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 轮询
            while (true) {
                try {
                    // 轮询检查信号文件
                    if (Files.exists(signalPath) && new String(Files.readAllBytes(signalPath)).trim().equals("start_java")) {
//                        logger.info("收到信号，开始计算hash:{}", exec_time);
                        // 执行计算hash的逻辑
                        doMiningthing(Paths.get(testcasefile), head, end);
                        // 执行完毕后，通知 Enclave 继续
                        Files.write(signalPath, "resume".getBytes());
//                        logger.info("resume:{}", exec_time);
                        exec_time++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WindowFileException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
        new Thread(() -> {
            try {
                logger.info("开始监控afl");
                // 创建 ProcessBuilder 实例
                ProcessBuilder processBuilder = new ProcessBuilder("./app");
                // 设置执行路径文件夹
                processBuilder.directory(new java.io.File(enclavePath));
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                // 启动进程
                Process process = processBuilder.start();
                // 读取输出
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                }
                // 等待进程结束并获取退出状态
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void executeCommand(String targetProgram) {
        ProcessBuilder processBuilder = new ProcessBuilder();
//        指定工作目录，必须是AFL，否则会段错误
        processBuilder.directory(new java.io.File(aflDirectory));
        String fuzzOutDir = fuzzOut + node1.getAddress().substring(0,6);
        String in = "../programQueue/potrace/testcase";
//        processBuilder.command("afl-fuzz", "-i", fuzzIn , "-o", fuzzOutDir , targetProgram);
        processBuilder.command("afl-fuzz", "-i", in , "-o", fuzzOutDir , targetProgram);
        try {
            Process process = processBuilder.start();
            // 等待命令执行完毕
            int exitCode = process.waitFor();
            System.out.println("AFL结束运行，退出代码: " + exitCode);
            if(exitCode == 0) {
                ApplicationContextProvider.publishEvent(new TerminateAFLEvent(1));
                logger.info("已终止AFL");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Deprecated
    public void windowFilesWatcher(BigInteger head, BigInteger end) {
        // 设置需要监控的目录路径
        Path testcases_dir = Paths.get(windowFiles + "/window_testcases");
        Path path_dir = Paths.get(windowFiles + "/window_paths");
        try {
            if (Files.notExists(testcases_dir)) {
                Files.createDirectories(testcases_dir);
            }
            if (Files.notExists(path_dir)) {
                Files.createDirectories(path_dir);
            }
            // 创建 WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();
            // 注册目录以监控创建事件
            testcases_dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            path_dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            // 存储新生成的文件
            Deque<Path> testcasefilesList =  new LinkedList<>();
            Deque<Path> pathfilesList = new LinkedList<>();

            logger.info("开始监控目录: {},{}", testcases_dir, path_dir);
            while (true) {
                // 等待下一个事件
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.err.println("监控被中断: " + e.getMessage());
                    return;
                }
                // 处理事件
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    // 确保事件是创建事件
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        // 获取新创建的文件名
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path newFilePath = ((Path) key.watchable()).resolve(ev.context());
                        System.out.println("检测到新文件: " + newFilePath);
                        // 存储
                        if (key.watchable().equals(testcases_dir)){
                            testcasefilesList.offer(newFilePath);
                        } else if (key.watchable().equals(path_dir)) {
                            pathfilesList.offer(newFilePath);
                        }
                        System.out.println("=============");
                        for (Path path1 : testcasefilesList) {
                            System.out.print("|" + path1.getFileName().toString() + ",");
                        }
                        System.out.println();
                        for (Path path1 : pathfilesList) {
                            System.out.print("|" + path1.getFileName().toString() + ",");
                        }
                        System.out.println();
                        if (testcasefilesList.size() > 1 && pathfilesList.size() > 1) {
                            doMiningthing(testcasefilesList.pop(), head, end);
                        }
                    }
                }
                // 重置 WatchKey
                boolean valid = key.reset();
                if (!valid) {
                    System.err.println("监控键无效，退出监控...");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("无法监控目录: " + e.getMessage());
        } catch (WindowFileException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void doMiningthing(Path testcaseFile,
                              BigInteger head,
                              BigInteger end) throws WindowFileException, IOException, InterruptedException {
        // 如果文件为空
        if (!Files.exists(testcaseFile)) {
            logger.info("testcaseFile不存在");
            return;
        } else if (Files.size(testcaseFile) == 0) {
            logger.info("testcaseFile为空");
            return;
        }
        triples = WindowFileUtils.parsetestfile(
                testcaseFile.toAbsolutePath().toString(),
                recordFile
                );
        // 向中间值添加本轮挖矿的path信息，等到新区块成功挖出后再置空
        payloadManager.setPayloads(triples);
//        if(!extractTrailingNumber(testcaseFile.toString()).equals(extractTrailingNumber(pathFile.toString()))) {
//            logger.error("错误！！！读取窗口文件错误！！！,{},{}", testcaseFile, pathFile);
//        }
//        int fileNum = extractTrailingNumber(testcaseFile.toString());
//        logger.info("本次处理文件num={}", fileNum);
        // 每20个文件清理一次
//        if(fileNum % 20 == 1 && fileNum > 1) {
//            for (int i = 1; i <= 20; i++) {
//                deleteFile(windowFiles + "/window_testcases/testcase_" + (fileNum- 21 + i));
//                deleteFile(windowFiles + "/window_paths/testfile_" + (fileNum- 21 + i));
//            }
//            logger.info("本次清理完毕, 范围是{}到{}", fileNum - 20, fileNum - 1);
//        }
        Block preBlock = chainService.getLocalLatestBlock();
        logger.info("本次计算区块hash的preBlock：高度为{}, Hash为{}", preBlock.getBlockHeader().getHeight(), preBlock.getBlockHash());

        // 从mempool中选择transactions
        List<Transaction> transactions = chooseTransactions();
        // 在交易列表最前端加上coinbase交易
        Transaction coinbaseTX = createCoinbaseTX(transactions, 0, preBlock.getBlockHeader().getHeight());
        if (transactions.size() == 0) {
            transactions.add(coinbaseTX);
        } else {
            transactions.set(0, coinbaseTX);
        }
        Block newBlock = computeWindowHash(preBlock, transactions, triples);
        String newHash = newBlock.getBlockHash();
//        logger.info("比较一下：newHash={}, newBlock.getHash={}", newHash, newBlock.getHash());
        logger.info("新区块中的payload长度为：{}", newBlock.getPayloads().size());
        // 当命中区间
        if(isInInterval(newHash, head, end)) {
            // 挖矿成功，并且提交payloads
            if (whenmined(newBlock, newHash, supplier)) {
                logger.info("挖矿成功，并且提交payloads");
                triples.clear();
                // 清空文件内容
                Files.newBufferedWriter(testcaseFile).close();
                logger.info("已清空窗口文件");
            } else {
                logger.info("失败");
            }
        } else {
            Files.write(this.path, "not hit".getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        BigInteger newHashInteger = new BigInteger(newHash, 16);
//        String content = "," + hitCount + "," + fileNum + "," + newHashInteger + "\n";
//        Files.write(this.path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    }

    public Block computeWindowHash(Block preBlock, List<Transaction> transactionList, List<Payload> payloads){
        Random random = new Random();
        long nonce = random.nextLong();
        long timestamp = System.currentTimeMillis();
        long height = preBlock.getBlockHeader().getHeight() + 1;


        BlockHeader blockHeader = new BlockHeader();
        blockHeader.setHashPreBlock(preBlock.getBlockHash());
        blockHeader.setNTime(timestamp);
        blockHeader.setNNonce(nonce);
        blockHeader.setHeight(height);

        Block newBlock = new Block(blockHeader, transactionList, payloads);
        newBlock.setBlockHash(newBlock.getHash());
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
    public boolean whenmined(Block newBlock, String newHash, Peer supplier) throws IOException, InterruptedException {
        hitCount ++;
        logger.info("挖矿成功，新区块高度为{}，hash={}，前一个区块hash={}",
                newBlock.getBlockHeader().getHeight(), newHash,newBlock.getBlockHeader().getHashPreBlock());
        // 广播新区块
        ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));

        logger.info("广播新Block:{}", newBlock.getBlockHash());
        endWindow = System.currentTimeMillis();
        logger.info("endWindow: {}", endWindow);
        long time = endWindow - lastWindowEnd;
        lastWindowEnd = endWindow;
        Files.write(path, Long.toString(time).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        // 校验新区块
        if (validationService.processNewMinedBlock(newBlock)) {
            // 提交给supplier
            payloadManager.setNewBlock(newBlock);
            // fuzzerAddress获取
            String address = walletService.getWalletAddress(0);
            payloadManager.setAddress(address);
            MessagePacket messagePacket = new MessagePacket();
            messagePacket.setType(MessagePacketType.PAYLOADS_SUBMIT);
            messagePacket.setBody(SerializeUtils.serialize(payloadManager));
            // 发送给supplier
            List<ClientChannelContext> channelContextList = p2pClient.getChannelContextList();
            // 在维护的列表中查找supplier
            for (ClientChannelContext channelContext : channelContextList) {
                Node serverNode = channelContext.getServerNode();
                if (serverNode.getIp().equals(supplier.getIp()) && serverNode.getPort() == supplier.getPort()) {
                    // supplier在列表中，直接发送消息即可
                    logger.info("提交之前payloads：{}", payloadManager.getPayloads().size());
                    p2pClient.sendToNode(channelContext, messagePacket);
                    payloadManager.setNull();
                    return true;
                }
            }
            // 如果没查到，重新连接
            try {
                ClientChannelContext channelContext = p2pClient.connect(new Node(supplier.getIp(), supplier.getPort()));
                p2pClient.sendToNode(channelContext, messagePacket);
                payloadManager.setNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 移除mempool和数据库中已打包的交易
            try {
                validationService.removeTransactions(newBlock);
            } catch (TransactionNotExistException e) {
                logger.error("移除已打包的交易失败");
            }
            return true;
        }
        return false;
    }

    /**
     * 从mempool中选择待打包的交易
     * 使用贪心算法选择交易费用高的交易
     */
    private List<Transaction> chooseTransactions() {
        Map<Transaction, Double> txOrderByFees = mempool.txOrderByFees();
        ArrayList<Transaction> transactions = new ArrayList<>();
        int totalSize = 0;
        for (Map.Entry<Transaction, Double> entry : txOrderByFees.entrySet()) {
            Transaction transaction = entry.getKey();
            int size = mempool.getObjectSize(transaction);
            totalSize += size;
            if (totalSize > Block.BLOCK_MAX_SIZE)
                break;
            transactions.add(transaction);
        }
        return transactions;
    }

    private Transaction createCoinbaseTX(List<Transaction> transactions, int index, long height) {
        // 计算所有的fee
        int feeTotal = transactions.stream().mapToInt(Transaction::getFee).sum();
        // 获取收款地址
        List<String> addresses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(addressFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addresses.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = addresses.get(index);
        Transaction coinbaseTransaction = transactionService.
                createCoinbaseTransaction(address, height, Transaction.BLOCK_REWARD, feeTotal);
        return coinbaseTransaction;
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
            System.out.println("退出代码: " + exitCode);
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

    public boolean deleteAFLFiles() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("./deleteAFLfiles.sh");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("退出代码: " + exitCode);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public Integer extractTrailingNumber(String str) {
        Pattern pattern = Pattern.compile("(\\d+)$");  // 匹配字符串末尾的数字
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));  // 提取并转换为整型
        }

        return null;  // 如果没有找到数字，返回 null
    }


}
