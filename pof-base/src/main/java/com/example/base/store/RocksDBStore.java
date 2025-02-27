package com.example.base.store;

import com.example.base.entities.transaction.TXOutput;
import com.example.base.utils.SerializeUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class RocksDBStore implements DBStore{
    static final Logger logger = LoggerFactory.getLogger(RocksDBStore.class);

//    rocksdb连接
    private RocksDB rocksDB;

    public RocksDBStore(String path) {
        String dataPath = String.format("%s/datastore", path);
        logger.info("rocksDB path:{}", dataPath);
        try {
            File directory = new File(dataPath);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new FileNotFoundException(dataPath);
            }
            rocksDB = RocksDB.open(new Options().setCreateIfMissing(true), dataPath);
        } catch (RocksDBException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//    write
    public boolean put(String key, Object value) {
        try {
            rocksDB.put(key.getBytes(), SerializeUtils.serialize(value));
            return true;
        }catch (RocksDBException e){
            return false;
        }
    }
    public Optional<Object> get(String key)
    {
        try {
            return Optional.of(SerializeUtils.unSerialize(rocksDB.get(key.getBytes())));
        } catch (Exception e) {
            // ignore
//            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<TXOutput> getUTXO(String key) {
        try {
            Optional<Object> o = Optional.of(SerializeUtils.unSerialize(rocksDB.get(key.getBytes())));
            if (o.isPresent()) {
                List<TXOutput> txOutputs = (List<TXOutput>)o.get();
                return txOutputs;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean delete(String key)
    {
        try {
            rocksDB.delete(key.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public <T> List<T> search(String keyPrefix)
    {
        ArrayList<T> list = new ArrayList<>();
        RocksIterator iterator = rocksDB.newIterator(new ReadOptions());
        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            String key = new String(iterator.key());
            if (!key.startsWith(keyPrefix)) {
                continue;
            }
            list.add((T) SerializeUtils.unSerialize(iterator.value()));
        }
        return list;
    }

    public <T> Map<String, T> searchforWallet(String keyPrefix)
    {
        Map<String, T> map = new HashMap<>();
        RocksIterator iterator = rocksDB.newIterator(new ReadOptions());
        for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
            String key = new String(iterator.key());
            if (!key.startsWith(keyPrefix)) {
                continue;
            }
            map.put(key.substring(WalletPrefix.UTXO_PREFIX.getPrefix().length()),
                    (T) SerializeUtils.unSerialize(iterator.value()));
        }
        return map;
    }



    public void close()
    {
        if (rocksDB != null) {
            rocksDB.close();
            logger.info("rocksDB关闭");
        }
    }


}
