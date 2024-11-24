package com.example.base.store;

public enum BlockPrefix {
    BLOCK_HEIGHT_PREFIX("/blocks/height/"),
    //用于维护本地区块最新高度
    HEIGHT("/blocks/latestheight/"),
    BLOCK_HASH_PREFIX("/blocks/hash/"),
    CHAIN_HEIGHT("/chain/height/");

    private String prefix;

    BlockPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
