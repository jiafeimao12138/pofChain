package com.example.base.utils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class LoggingMonitor {

//    @Value("${log.newPathlog}")

    private static final File crashLog = new File("log/crash.log");
    private static final File newPathlog = new File("log/newPath.log");

    public static void logChange(String key, MutablePair<Long, Long> value) {
        if (!newPathlog.exists()) {
            try {
                newPathlog.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (FileWriter writer = new FileWriter(newPathlog, true)) {
            writer.write(System.currentTimeMillis() + "-" + key + ":" + value.getLeft() + "/" + value.getRight() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logCrash(String key, int num) {
        if (!crashLog.exists()) {
            try {
                crashLog.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try (FileWriter writer = new FileWriter(crashLog, true)) {
            writer.write(System.currentTimeMillis() + "-" + key + ":" + num + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HashMap<Long, Long> readLogFile() {
        HashMap<Long, Long> pathMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(newPathlog))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(":");
                String timestamp = split[0].split("-")[0];
                String newPathNum = split[1].split("/")[0];
                pathMap.put(Long.parseLong(timestamp), Long.parseLong(newPathNum));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathMap;
    }

    public static void main(String[] args) {
        MutablePair<Long, Long> pair = new MutablePair<>();
        pair.setLeft(123l);
        pair.setRight(456l);
        logChange("123", pair);
        System.out.println(readLogFile());
    }
}
