package com.example.fuzzed.impl;

import com.example.base.entities.Peer;
import com.example.fuzzed.ProgramService;
import com.example.net.client.P2pClient;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.conf.P2pNetConfig;
import com.example.net.events.NewTargetProgramEvent;
import com.sun.jmx.remote.internal.ArrayQueue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tio.core.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private static final Logger logger = LoggerFactory.getLogger(ProgramServiceImpl.class);
    private final P2pNetConfig p2pNetConfig;
    // 待测程序队列，队列头为下一次fuzzing的程序
    ArrayDeque <MutablePair<byte[], Peer>> ProgramQueue = new ArrayDeque<>(16);

    // 将待测程序转换为目标可执行文件
    @Override
    public boolean prepareTargetProgram(String path, String objPath) {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("afl-gcc", "-no-pie", path, "-o", objPath);
            Process process = processBuilder.start();

            // 等待命令执行完毕
            int exitCode = process.waitFor();
            System.out.println("afl-gcc complete. Exited with code: " + exitCode);

            File file = new File(objPath);
            // 读取文件的字节
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            // 广播该目标可执行文件和ip
            MutablePair<byte[], Peer> pair = new MutablePair<>(fileBytes,
                    new Peer(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort()));
            ApplicationContextProvider.publishEvent(new NewTargetProgramEvent(pair));
            logger.info("已广播待测可执行文件：{}, 长度:{}, Node:{}", file.getName(), fileBytes.length, pair.getRight());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    // 将接收到的byte[]转换为file，准备fuzz
    @Override
    public void byteToFile(byte[] fileBytes, String path, String name) {
        String absolutePath = path + name + System.currentTimeMillis();
        File file = new File(absolutePath);
        try {
            file.createNewFile();
            try(FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileBytes);
                // 添加执行权限
                ProcessBuilder processBuilder = new ProcessBuilder("sudo", "chmod", "+x", absolutePath);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                process.waitFor();
                logger.info("克隆待测程序成功：{}", file.getName());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path chooseTargetProgram(String directorypath) {

        try {
            Path path = Paths.get(directorypath);
            Path oldestFile = findOldestFile(path);
            if (oldestFile != null) {
                logger.info("选择队列头：{}", oldestFile.toString());
                return oldestFile;
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path findOldestFile(Path directory) throws IOException {
        return Files.list(directory)
                .filter(Files::isRegularFile) // 只选择文件
                .reduce((first, second) -> {
                    try {
                        BasicFileAttributes attrs1 = Files.readAttributes(first, BasicFileAttributes.class);
                        BasicFileAttributes attrs2 = Files.readAttributes(second, BasicFileAttributes.class);
                        return attrs1.creationTime().compareTo(attrs2.creationTime()) < 0 ? first : second;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return first; // 发生错误时返回第一个文件
                    }
                })
                .orElse(null); // 如果没有文件，返回 null
    }

    @Override
    public ArrayDeque<MutablePair<byte[], Peer>> getProgramQueue() {
        return ProgramQueue;
    }

    @Override
    public boolean addProgramQueue(MutablePair<byte[], Peer> pair) {
        return ProgramQueue.offer(pair);
    }
}
