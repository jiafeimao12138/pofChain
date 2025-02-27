package com.example.base.store;

import com.example.base.entities.transaction.TXOutput;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public interface DBStore {
    boolean put(String key, Object value);

    // get an item from database with the specified key
    Optional<Object> get(String key);

    List<TXOutput> getUTXO(String key);

    // delete an item from database with the specified key
    boolean delete(String key);

    // search in database with key prefix
    <T> List<T> search(String keyPrefix);

    <T> Map<String, T> searchforWallet(String keyPrefix);

    // close the database
    void close();
}
