package com.example.web.service;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;

import java.util.List;

public interface WalletService {
    String createWallet();
    String getWalletAddress(int index);
    int getBalance(String address);
    boolean send(String address, int value);
    List<TXOutput> getUTXOs(String address);
    void removeSpentTXOutput(Transaction transaction);
}
