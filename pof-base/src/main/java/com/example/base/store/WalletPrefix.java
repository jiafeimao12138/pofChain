package com.example.base.store;

public enum WalletPrefix {

    UTXO_PREFIX("/wallet/utxo/"),
    TX_PREFIX("/wallet/tx/"),
    ;

    private String prefix;
    WalletPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getPrefix() {
        return prefix;
    }
}
