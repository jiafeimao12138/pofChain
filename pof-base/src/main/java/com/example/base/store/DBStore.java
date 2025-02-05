package com.example.base.store;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DBStore {
    boolean put(String key, Object value);

    // get an item from database with the specified key
    Optional<Object> get(String key);

    // delete an item from database with the specified key
    boolean delete(String key);

    // search in database with key prefix
    <T> List<T> search(String keyPrefix);

    <T> Map<String, T> searchforWallet(String keyPrefix);

    // close the database
    void close();
}
