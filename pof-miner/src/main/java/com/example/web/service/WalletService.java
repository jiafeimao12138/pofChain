package com.example.web.service;

import com.example.base.entities.wallet.Wallet;

public interface WalletService {
    Wallet createWallet();
    int getBalance(Wallet wallet);
    boolean send(String address, int value);
}
