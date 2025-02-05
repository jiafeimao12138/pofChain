package com.example.web.service;

import com.example.base.entities.wallet.Wallet;

public interface WalletService {
    Wallet createWallet();
    int getBalance(String address);
    boolean send(String address, int value);
}
