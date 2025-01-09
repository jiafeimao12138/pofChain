package com.example.miner.pof;

import com.example.base.Exception.WindowFileException;
import com.example.base.entities.Block;
import com.example.base.entities.BlockHeader;
import com.example.base.entities.Payload;
import com.example.base.entities.Transaction;
import com.example.base.store.BlockPrefix;
import com.example.base.store.RocksDBStore;
import com.example.base.utils.WindowFileUtils;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.web.service.MiningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jiafeimao
 * @date 2024年09月15日 15:01
 */
public class ProofOfFuzzing {

    private static final Logger logger = LoggerFactory.getLogger(ProofOfFuzzing.class);

    private final Block preBlock;
    private RocksDBStore rocksDBStore = new RocksDBStore("./data/genesis");

    public static ProofOfFuzzing newProofOfFuzzing(Block preBlock) {
        return new ProofOfFuzzing(preBlock);
    }

    public ProofOfFuzzing(Block preBlock) {
        this.preBlock = preBlock;
    }


//    运行工作量证明，这里执行AFL脚本
    public void run() {
            try {
                String shellCommand = "cd AFL && afl-fuzz -i fuzz_in/ -o fuzz_out ./afl_testfiles/objfiles/string_length";
                executeCommand(shellCommand);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();
//        指定工作目录
//        processBuilder.directory(new java.io.File("/home/wj/pofChain/AFL"));

        // Linux command
        processBuilder.command("afl-fuzz", "-i", "AFL/fuzz_in/", "-o", "AFL/fuzz_out", "./AFL/afl_testfiles/objfiles/string_length");

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
                                "AFL/afl_testfiles/window_testcases/testcase_" + num,
                                "AFL/afl_testfiles/window_paths/testfile_" + num, "");
                        num ++;
                    } catch (WindowFileException e) {
                        throw new RuntimeException(e);
                    }
                    Block newBlock = computeWindowHash(preBlock, transactions, triples);
                    logger.info("挖矿成功，新区块高度为{}，hash={}，前一个区块hash={}",
                            newBlock.getBlockHeader().getHeight(), newBlock.getHash(),newBlock.getBlockHeader().getHashPreBlock());
                    // 广播
                    ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
                    logger.info("广播新Block,hash={}", newBlock.getHash());

                    if (!rocksDBStore.put(BlockPrefix.BLOCK_HEIGHT_PREFIX.getPrefix() + newBlock.getBlockHeader().getHeight(), newBlock)) {
                        logger.info("存入新区块失败");
                    }
                    if (!rocksDBStore.put(BlockPrefix.HEIGHT.getPrefix(), newBlock.getBlockHeader().getHeight())){
                        logger.info("存入最新高度失败");
                    }
                    rocksDBStore.close();
                    logger.info("存入新区块，高度为{}，hash={}", newBlock.getBlockHeader().getHeight(), newBlock.getHash());
                }
            }

            // 等待命令执行完毕
            int exitCode = process.waitFor();
            System.out.println("\nExited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

/*
*   sha256(preBlock, nonce, transactions, List<input, path, is_craash>, timestamp)
*   返回执行窗口的hash值
* */
    public Block computeWindowHash(Block preBlock,
                                   List<Transaction> transactionList,
                                   List<Payload> triples){
        Random random = new Random();
        long nonce = random.nextInt();
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

}
