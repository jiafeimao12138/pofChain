package com.example.web.service.impl;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.UTXOSet;
import com.example.base.entities.wallet.Wallet;
import com.example.base.entities.wallet.WalletUtils;
import com.example.web.service.WalletService;

import java.util.List;

public class WalletServiceImpl implements WalletService {
    @Override
    public Wallet createWallet() {
        return WalletUtils.getInstance().createWallet();
    }

    @Override
    public int getBalance(String address) {
        List<TXOutput> utxOs = UTXOSet.findUTXOs(Wallet.getPubKey(address));
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
