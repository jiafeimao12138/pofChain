package com.example.web.service.impl;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;
import com.example.base.utils.BlockUtils;
import com.example.base.utils.SerializeUtils;
import com.example.net.base.Mempool;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewTransactionEvent;
import com.example.web.service.TransactionService;
import com.example.web.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * fake交易生成，实验用
 */
@Component
@RequiredArgsConstructor
public class FakeTXGenerator {
    private static final int NUM_TRANSACTIONS = 300;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final Mempool mempool;

    @Value("${wallet.address}")
    private String filePath;
    private List<String> addresses = new ArrayList<>();

    // 生成fake交易
    public void generateTransactions() throws Exception {
        generateInitialUTXO(20);
        int i = 0;
        while(i < NUM_TRANSACTIONS) {
            // 随机选择输入address
            String addr = addresses.get(new Random().nextInt(20));
            // 随机选择输出address
            String addr1 = addresses.get(new Random().nextInt(20));
            // 查询余额,如果小于5就放弃这个地址
            int balance = walletService.getBalance(addr);
            if (balance <= 5)
                break;
            // 生成一个比这个余额小的数
            int spend = new Random().nextInt(balance - 5) + 1;
            Transaction commonTransaction = transactionService.createCommonTransaction(addr, addr1, spend, new Random().nextInt(4) + 1);
//            mempool.addTransaction(commonTransaction);
            transactionService.txStore(commonTransaction);
//            System.out.println(i + ":" + BlockUtils.getTransactionSize(commonTransaction));
            // 移除已花费UTXO
//            walletService.removeSpentTXOutput(commonTransaction);
//            int size = SerializeUtils.serialize(commonTransaction).length;
//            System.out.println("第" + i + "个交易:" + commonTransaction.getTxIdStr() + ", from: " + addr + ", to: " + addr1 + ", send: " + spend);
//            System.out.println("余额:" + walletService.getBalance(addr) + ", " + walletService.getBalance(addr1));
//            System.out.println("第" + i + "个交易大小：" + size);
            ApplicationContextProvider.publishEvent(new NewTransactionEvent(commonTransaction));
            System.out.println("已广播第" + i + "个交易");
            i ++;
        }
    }

    // 生成初始的100个UTXO
    private List<List<TXOutput>> generateInitialUTXO(int numUTXO) throws Exception {
        List<List<TXOutput>> utxoList = new ArrayList<>();
        for (int i = 0; i < numUTXO; i++) {
            String walletAddress = walletService.createWallet();
            addresses.add(walletAddress);
            Transaction transaction = MockOriginBalance(walletAddress);
            utxoList.add(transaction.getOutputs());
            // 将地址保存到本地文件中
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(walletAddress);
                writer.newLine(); // 每个地址后换行
            } catch (IOException e) {
                e.printStackTrace(); // 打印异常信息
            }

        }
        return utxoList;
    }

    private Transaction MockOriginBalance(String address) throws Exception {
        Transaction coinbaseTransaction = transactionService.createCoinbaseTransaction(address, 1, 100, 0);
        transactionService.txStore(coinbaseTransaction);
        return coinbaseTransaction;
    }
}
