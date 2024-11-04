package com.example.base.store;

import com.example.base.utils.SerializeUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RocksDBStore {
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
            return Optional.of(SerializeUtils.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                unSerialize(rocksDB.get(key.getBytes())));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
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

    public void close()
    {
        if (rocksDB != null) {
            rocksDB.close();
            logger.info("rocksDB关闭");
        }
    }


}
