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

    public static void main(String[] args) throws Exception {
        DBStore rocksDBStore = new RocksDBStore("/home/wj/datastore/wallet");
        // 创建钱包A，表示用户A
        Wallet wallet = WalletUtils.getInstance().createWallet();
        // 校验公钥hash和钱包地址
        byte[] pubKeyHash = wallet.getPubKeyHash();
        String address = wallet.getAddress();
        System.out.println("PubKeyHash: " + wallet.getPubKeyHash());
        System.out.println("wallet address : " + wallet.getAddress());

        // 挖矿奖励交易创建
        Transaction coinbaseTX = Transaction.newCoinbaseTX(wallet.getAddress(), 2);
        coinbaseTX.setCreateTime(System.currentTimeMillis());
        coinbaseTX.setTxId(coinbaseTX.getTxId());
        // 加入UTXOSet
        // 校验TXOutput中的公钥hash是否正确
        List<TXOutput> txOutputs = coinbaseTX.getOutputs();
        byte[] txId1 = coinbaseTX.getTxId();
        String s1 = ByteUtils.bytesToHex(txId1);
        String txId = ByteUtils.bytesToHex(coinbaseTX.getTxId());
        System.out.println(txId);
        rocksDBStore.put(WalletPrefix.UTXO_PREFIX.getPrefix() + txId, txOutputs);
        Optional<Object> o = rocksDBStore.get(WalletPrefix.UTXO_PREFIX.getPrefix() + txId);
        System.out.println(o);
//        for (TXOutput txOutput : (List<TXOutput>) o.get()) {
//            System.out.println(txOutput.toString());
//        }

        rocksDBStore.close();
        // 钱包余额查询
        List<TXOutput> utxOs = UTXOSet.findUTXOs(wallet.getPubKeyHash());
        int balance = 0;
        for (TXOutput utxO : utxOs) {
            balance += utxO.getValue();
        }
        System.out.println(balance);


        // 生成普通交易
        // 创建钱包B，表示用户B
        Wallet wallet1 = WalletUtils.getInstance().createWallet();
        String address1 = wallet1.getAddress();
        Transaction utxoTransaction = Transaction.newUTXOTransaction(address, address1, 3);
        byte[] utxoTransactionTxId = utxoTransaction.getTxId();
        String utxoTXIdStr = ByteUtils.bytesToHex(utxoTransactionTxId);
        List<TXOutput> outputs = utxoTransaction.getOutputs();
        // 存储utxo，先假设一存储就可以用
        DBStore rocksDBStore1 = new RocksDBStore("/home/wj/datastore/wallet");
        rocksDBStore1.put(WalletPrefix.UTXO_PREFIX.getPrefix() + utxoTXIdStr, outputs);
        rocksDBStore1.close();
        List<TXOutput> utxOs1 = UTXOSet.findUTXOs(wallet1.getPubKeyHash());
        int balance1 = 0;
        for (TXOutput utxO : utxOs1) {
            balance1 += utxO.getValue();
        }
        System.out.println("balance1: " + balance1);

    }
}
