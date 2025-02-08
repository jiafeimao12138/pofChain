package com.example.web.service.impl;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.UTXOSet;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.base.utils.Base58Check;
import com.example.web.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final UTXOSet utxoSet;
    @Override
    public String createWallet() {
        Wallet wallet = WalletUtils.getInstance().createWallet();
        String address = wallet.getAddress();
        return address;
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
}
