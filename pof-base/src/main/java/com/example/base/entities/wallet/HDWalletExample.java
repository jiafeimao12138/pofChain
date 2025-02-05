package com.example.base.entities.wallet;
import org.bitcoinj.base.Address;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.core.*;

import java.io.File;
import java.util.List;

public class HDWalletExample {
    public static void main(String[] args) throws Exception {
        NetworkParameters params = TestNet3Params.get();

        // 创建钱包
        WalletAppKit kit = new WalletAppKit(params, new File("."), "hdwallet");
        kit.startAsync();
        kit.awaitRunning();
        Wallet wallet = kit.wallet();

        // 生成一个新的比特币地址（自动使用 BIP-32）
        Address newAddress = wallet.freshReceiveAddress();
        System.out.println("新地址: " + newAddress);

        // 获取钱包的种子短语
        DeterministicSeed seed = wallet.getKeyChainSeed();
        System.out.println("钱包种子（助记词）: " + seed.getMnemonicCode());
    }
}
