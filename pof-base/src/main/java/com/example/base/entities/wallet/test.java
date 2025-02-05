package com.example.base.entities.wallet;

import com.google.common.base.Joiner;
import org.bitcoinj.base.Address;
import org.bitcoinj.base.Coin;
import org.bitcoinj.base.ScriptType;
import org.bitcoinj.base.Sha256Hash;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.ECKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Wallet;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicSeed;

import static org.bitcoinj.base.Coin.valueOf;
import static org.bitcoinj.testing.FakeTxBuilder.roundTripTransaction;

public class test {

    public static void main(String[] args) throws Exception {

        // 1. 生成公钥和私钥
        for (int i = 0; i < 10; i++) {
            ECKey key = new ECKey();
            String privateKey = key.getPrivateKeyAsHex();
            String publicKey = key.getPublicKeyAsHex();
            String address = key.toAddress(ScriptType.P2PKH, TestNet3Params.get().network()).toString();
//
//            System.out.println("私钥: " + privateKey);
//            System.out.println("公钥: " + publicKey);
//            System.out.println("比特币地址: " + address);
        }

        // 使用 TestNet3 网络配置
        NetworkParameters params = TestNet3Params.get();
        // 初始化上下文1
        Context context = new Context(params);
//        Context.getOrCreate();
        // 创建一个空的钱包
        Wallet wallet = new Wallet(params, KeyChainGroup.createBasic(params));
        // 检查钱包是否有至少一个密钥，如果没有则创建
        if (wallet.getKeyChainGroupSize() < 1) {
            // 生成一个新的密钥
            wallet.importKey(new ECKey());
        }
        wallet.importKey(new ECKey());
        // 获取钱包中的第一个 ECKey
        for (int i = 0; i < 2; i++) {
            ECKey ecKey = wallet.getImportedKeys().get(i);
//            System.out.println("PrivKey: " + ecKey.getPrivKey());
//            System.out.println("PrivateKeyAsHex: " + ecKey.getPrivateKeyAsHex());
            // 获取地址
//            System.out.println("Address.fromKey: " + Address.fromKey(params, ecKey, ScriptType.P2PKH).toString());
        }

//        DeterministicSeed seed = wallet.getKeyChainSeed();
//        System.out.println("Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
//        System.out.println("Seed birthday is: " + seed.getCreationTimeSeconds());

        String seedCode = "yard impulse luxury drive today throw farm pepper survey wreck glass federal";
        long creationtime = 1409478661L;
        DeterministicSeed seed1 = new DeterministicSeed(seedCode, null, "", creationtime);
        System.out.println("Seed words are: " + Joiner.on(" ").join(seed1.getMnemonicCode()));
        System.out.println("Seed birthday is: " + seed1.getCreationTimeSeconds());
        Wallet restoredWallet = Wallet.fromSeed(params,seed1, ScriptType.P2PKH);
        // 生成新的密钥对
        restoredWallet.freshReceiveKey();
        restoredWallet.freshReceiveKey();
        restoredWallet.freshReceiveKey();

        List<DeterministicKey> leafKeys = restoredWallet.getActiveKeyChain().getLeafKeys();
        for (DeterministicKey leafKey : leafKeys) {
            System.out.println("HD公钥: " + leafKey.getPublicKeyAsHex());
            System.out.println("HD私钥: " + leafKey.getPrivateKeyAsHex());
        }

//        ECKey ecKey = restoredWallet.getImportedKeys().get(0);
//        ECKey ecKey1 = restoredWallet.getImportedKeys().get(1);
//        System.out.println("PrivKey: " + ecKey.getPrivKey());
//        System.out.println("PrivateKeyAsHex: " + ecKey.getPrivateKeyAsHex());
//        // 获取地址
        Address address = Address.fromKey(params, leafKeys.get(0), ScriptType.P2PKH);
        Address address1 = Address.fromKey(params, leafKeys.get(1), ScriptType.P2PKH);
        System.out.println("Address.fromKey: " + address);
        System.out.println("Address1.fromKey: " + address1);


        // 2. 签名消息
//        String message = "Test message";
//        byte[] messageBytes = message.getBytes();
//        byte[] signature = key.sign(Sha256Hash.of(messageBytes)).encodeToDER();
//        String signatureHex = Utils.HEX.encode(signature);
//        System.out.println("签名: " + signatureHex);
//
//        // 3. 验证签名
//        ECKey keyToVerify = ECKey.fromPublicOnly(new BigInteger(publicKey, 16));
//        TransactionSignature txSig = TransactionSignature.decodeFromDER(signature);
//        boolean valid = keyToVerify.verify(Sha256Hash.of(messageBytes), txSig);
//        System.out.println("签名有效性: " + valid);

        Coin value = valueOf(10, 0);
        Transaction tx = createFakeTxWithChangeAddress(value, address, address1);
        String string = tx.toString();
        System.out.println("transaction: " + string);
        // 模拟将交易输出添加到钱包
        for (TransactionOutput output : tx.getOutputs()) {
            System.out.println("output: " + output);
            restoredWallet.addCoinsReceivedEventListener((w, tx1, prevBalance, newBalance) -> {
                Context.propagate(context);
                System.out.println("新余额: " + newBalance.toFriendlyString());
                System.out.println("钱包余额" + restoredWallet.getBalance());
             }
            );
            restoredWallet.receivePending(tx, null); // 处理待接收的交易
        }

        Coin balance = restoredWallet.getBalance();
        System.out.println("balance: " + balance.toFriendlyString());


    }

    /**
     * Create a fake TX of sufficient realism to exercise the unit tests. Two outputs, one to us, one to somewhere
     * else to simulate change. There is one random input.
     */
    public static Transaction createFakeTxWithChangeAddress(Coin value, Address to, Address changeOutput) {
        Transaction t = new Transaction();
        TransactionOutput outputToMe = new TransactionOutput(t, value, to);
        t.addOutput(outputToMe);
        TransactionOutput change = new TransactionOutput(t, valueOf(1, 11), changeOutput);
        t.addOutput(change);
        // Make a previous tx simply to send us sufficient coins. This prev tx is not really valid but it doesn't
        // matter for our purposes.
        Transaction prevTx = new Transaction();
        TransactionOutput prevOut = new TransactionOutput(prevTx, value, to);
        prevTx.addOutput(prevOut);
        // Connect it.
        t.addInput(prevOut).setScriptSig(ScriptBuilder.createInputScript(TransactionSignature.dummy()));
        // Fake signature.
        // Serialize/deserialize to ensure internal state is stripped, as if it had been read from the wire.
        return roundTripTransaction(t);
    }

}
