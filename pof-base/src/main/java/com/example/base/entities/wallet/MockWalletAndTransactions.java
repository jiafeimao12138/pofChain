package com.example.base.entities.wallet;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.entities.transaction.Transaction;
import com.example.base.entities.transaction.UTXOSet;
import com.example.base.store.DBStore;
import com.example.base.store.RocksDBStore;
import com.example.base.store.WalletPrefix;
import com.example.base.utils.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MockWalletAndTransactions {

    private static final Logger logger = LoggerFactory.getLogger(MockWalletAndTransactions.class);

    public static void main(String[] args) {
        DBStore rocksDBStore = new RocksDBStore("/home/wj/datastore/wallet");
        // 创建钱包
        Wallet wallet = WalletUtils.getInstance().createWallet();
        logger.info("wallet address : " + wallet.getAddress());
        // 挖矿奖励交易创建
        Transaction coinbaseTX = Transaction.newCoinbaseTX(wallet.getAddress(), 2);
        System.out.println(ByteUtils.bytesToHex(coinbaseTX.getTxId()));
        System.out.println(ByteUtils.bytesToHex(coinbaseTX.getInputs().get(0).getParent().getTxId()));
        // 加入UTXOSet
        List<TXOutput> txOutputs = coinbaseTX.getOutputs();
        String txId = Hex.encodeHexString(coinbaseTX.getTxId());
        System.out.println(txId);
        rocksDBStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, txOutputs);
        Optional<Object> o = rocksDBStore.get(WalletPrefix.UTXO_PREFIX.getPrefix() + txId);
        System.out.println(o);
//        for (TXOutput txOutput : (List<TXOutput>) o.get()) {
//            System.out.println(txOutput.toString());
//        }

        rocksDBStore.close();
        // 钱包余额查询
        List<TXOutput> utxOs = UTXOSet.findUTXOs(Wallet.getPubKey(wallet.getAddress()));
        int balance = 0;
        for (TXOutput utxO : utxOs) {
            balance += utxO.getValue();
        }
        System.out.println("balance = " + balance);


    }
}
