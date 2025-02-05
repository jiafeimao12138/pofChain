package com.example.web.controller;

import org.bitcoinj.base.ScriptType;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wallet")
public class WalletController {

    // 创建新钱包
    @RequestMapping("/createNewWallet")
    public void createNewWallet() {
        // 使用 TestNet3 网络配置
        NetworkParameters params = TestNet3Params.get();
        // 创建一个空的钱包
        Wallet wallet = new Wallet(params, KeyChainGroup.createBasic(params));
    }

    // 发送新交易，每次
    @RequestMapping("/sendCoin")
    public void sendCoin(@RequestParam int value, @RequestParam String address) {

    }
}
