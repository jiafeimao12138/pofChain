package com.example.web.controller;

import com.example.base.entities.transaction.Transaction;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.events.NewTransactionEvent;
import com.example.web.service.TransactionService;
import com.example.web.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RestController
@RequestMapping("wallet")
@RequiredArgsConstructor
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);


    private final WalletService walletService;
    private final TransactionService transactionService;
    private final DBStore dbStore;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();

    @Value("${wallet.address}")
    private String filePath;
    /**
     * 创建新钱包并保存钱包数据到磁盘
     */
    @RequestMapping("createNewWallet")
    public void createNewWallet() {
        // 创建一个空的钱包
        String address = walletService.createWallet();
        // 将地址保存到本地文件中
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                writer.write(address);
                writer.newLine(); // 每个地址后换行
            logger.info("钱包地址已成功保存到{}" ,filePath);
        } catch (IOException e) {
            e.printStackTrace(); // 打印异常信息
        }
    }

    /**
     * 发送新交易并广播
     * @param value
     * @param toaddress
     * @param index 付款钱包地址的索引
     */
    @RequestMapping("sendCoin")
    public void sendCoinAndBroadcast(@RequestParam int value,
                                     @RequestParam String toaddress,
                                     @RequestParam int index,
                                     @RequestParam int fee) {
        // 从本地文件中读取地址
        List<String> addresses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                addresses.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Wallet senderWallet = WalletUtils.getInstance().getWallet(addresses.get(index));
        String address = senderWallet.getAddress();
        // 创建交易并签名
        try {
            // mock
            MockCoinbaseTX(address);
            Transaction commonTransaction = transactionService.createCommonTransaction(address, toaddress, value, fee);
//            广播交易
            ApplicationContextProvider.publishEvent(new NewTransactionEvent(commonTransaction));
            transactionService.storeMempool(commonTransaction);
//            持久化存储
            writeLock.lock();
            String txIdStr = commonTransaction.getTxIdStr();
            dbStore.put(WalletPrefix.TX_PREFIX.getPrefix() + txIdStr, commonTransaction);
            dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txIdStr, commonTransaction.getOutputs());
            // @TODO 后面看要不要手动close
//        dbStore.close();
            writeLock.unlock();
            logger.info("已将交易{}存入数据库", txIdStr);
            logger.info("已广播交易：{}", commonTransaction.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping("getBalance")
    public void getBalance(@RequestParam String address) {
        int balance = walletService.getBalance(address);
        logger.info("地址{}的余额为{}", address, balance);
    }

    private void MockCoinbaseTX(String address) throws Exception {
        Transaction coinbaseTransaction = transactionService.createCoinbaseTransaction(address, 1, 10, 2);
        transactionService.storeMempool(coinbaseTransaction);
        writeLock.lock();
        String txId = coinbaseTransaction.getTxIdStr();
        dbStore.put(WalletPrefix.TX_PREFIX.getPrefix() + txId, coinbaseTransaction);
        dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, coinbaseTransaction.getOutputs());
//        dbStore.close();
        writeLock.unlock();
        logger.info("已将交易{}存入数据库", txId);
    }
}
