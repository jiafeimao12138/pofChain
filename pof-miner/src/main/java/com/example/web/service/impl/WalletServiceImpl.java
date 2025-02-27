package com.example.web.service.impl;

import com.example.base.entities.transaction.TXInput;
import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;
import com.example.base.entities.transaction.UTXOSet;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.store.DBStore;
import com.example.base.store.WalletPrefix;
import com.example.base.utils.Base58Check;
import com.example.base.utils.ByteUtils;
import com.example.web.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.properties")
public class WalletServiceImpl implements WalletService {

    private final UTXOSet utxoSet;
    @Value("${wallet.address}")
    private String addressFilePath;
    private final DBStore dbStore;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock writeLock = rwl.writeLock();

    @Override
    public String createWallet() {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        String address = wallet.getAddress();
        return address;
    }

    /**
     * 获取用户钱包地址
     * @param index 指多个钱包地址的索引号, 默认是用第一个
     * @return
     */
    @Override
    public String getWalletAddress(int index) {
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
        return addresses.get(index);
    }

    @Override
    public int getBalance(String address) {
        // 将地址转换为pubKeyHash
        // 反向转化为 byte 数组
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        List<TXOutput> utxOs = utxoSet.findUTXOs(pubKeyHash);
        int balance = 0;
        for (TXOutput utxO : utxOs) {
            balance += utxO.getValue();
        }
        return balance;
    }

    @Override
    public boolean send(String address, int value) {
        return false;
    }

    /**
     * 获取该地址的所有UTXO
     * @param address
     * @return
     */
    @Override
    public List<TXOutput> getUTXOs(String address) {
        byte[] versionedPayload = Base58Check.base58ToBytes(address);
        byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
        List<TXOutput> utxOs = utxoSet.findUTXOs(pubKeyHash);
        return utxOs;
    }

    /**
     * 移除已花费的TXOutput
     * @param transaction
     */
    @Override
    public void removeSpentTXOutput(Transaction transaction) {
        writeLock.lock();
        List<TXInput> inputs = transaction.getInputs();

        for (TXInput input : inputs) {
            String TXId = ByteUtils.bytesToHex(input.getPreviousTXId());
            List<TXOutput> utxos = dbStore.getUTXO(WalletPrefix.UTXO_PREFIX.getPrefix() + TXId);
            if (CollectionUtils.isEmpty(utxos))
                break;
            utxos.remove(input.getTxOutputIndex());
            dbStore.delete(WalletPrefix.UTXO_PREFIX.getPrefix() + TXId);
            if (!CollectionUtils.isEmpty(utxos)){
                dbStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + TXId, utxos);
            }
        }
        writeLock.unlock();
    }
}
