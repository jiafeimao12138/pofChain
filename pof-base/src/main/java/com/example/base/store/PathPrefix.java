package com.example.base.store;

public enum PathPrefix {

    PATH_PREFIX("/path/hash/"),
    PATH_FUZZER_PREFIX("/path/hash/fuzzer/"),
    PATH_TIME_PREFIX("/path/hash/time/"),
    ;

    private String prefix;
    PathPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
