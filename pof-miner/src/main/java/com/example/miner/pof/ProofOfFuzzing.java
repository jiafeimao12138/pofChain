package com.example.miner.pof;

import com.example.base.Exception.WindowFileException;
import com.example.base.entities.Block;
import com.example.base.entities.Transaction;
import com.example.base.utils.CryptoUtils;
import com.example.base.utils.WindowFileUtils;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewBlockEvent;
import com.example.web.service.BlockServiceImpl;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private final String preHash;
    //当前区块高度
    private final long height;

    public BlockServiceImpl blockService;

    public static ProofOfFuzzing newProofOfFuzzing(String preHash, long height) {
        return new ProofOfFuzzing(preHash, height);
    }

    public ProofOfFuzzing(String preHash, long height) {
        this.preHash = preHash;
        this.height = height;
    }

//   TODO: 动态更新hash区间


//    运行工作量证明，这里执行AFL脚本
    public void run() {
//        while (true) {
            try {
////                String shellCommand1 = "./switchRoot.sh";
////                String shellCommand2 = "s";
//                String shellCommand3 = "sh /home/wj/pofChain/afl_exec.sh";
                String shellCommand3 = "cd AFL && afl-fuzz -i fuzz_in/ -o fuzz_out ./afl_testfiles/objfiles/string_length";
////                executeShellScript(shellCommand1);
                executeCommand(shellCommand3);
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }
    }

    public void executeCommand(String command) {
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
                    Block preBlock = new Block();
                    List<Transaction> transactions = new ArrayList<>();
                    List<Triple<String, List<Integer>, Boolean>> triples;
                    try {
                        triples = WindowFileUtils.windowFilesToTriple(
                                "/home/wj/pofChain/AFL/afl_testfiles/window_testcases/testcase_" + num,
                                "/home/wj/pofChain/AFL/afl_testfiles/window_paths/testfile_" + num);
                        num ++;
                    } catch (WindowFileException e) {
                        throw new RuntimeException(e);
                    }
                    Block newBlock = computeWindowHash(preBlock, transactions, triples);
                    // 广播
                    ApplicationContextProvider.publishEvent(new NewBlockEvent(newBlock));
                    logger.info("广播新Block,hash={}", newBlock.GetHash());
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
    public Block computeWindowHash(Block preBlock, List<Transaction> transactionList, List<Triple<String,List<Integer>,Boolean>> triples){
        Random random = new Random();
        long nonce = random.nextInt();
        long timestamp = System.currentTimeMillis();

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

    public void executeShellScript(String shellCommand) throws IOException, InterruptedException {
        logger.info("start to run afl shell-----------------------");
        Process process = Runtime.getRuntime().exec(shellCommand);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        int num = 1;
        //一直监控输出
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            //        需要放在循环里，一旦监测到new window start，即表示上个窗口执行结束了，并且也处理完了，接下来轮到计算hash

        }
        int exitCode = process.waitFor();
        System.out.println("Script exited with code: " + exitCode);
        reader.close();
    }

}