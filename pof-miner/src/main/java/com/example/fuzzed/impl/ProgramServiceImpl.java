package com.example.fuzzed.impl;

import com.example.base.entities.*;
import com.example.base.utils.ByteUtils;
import com.example.base.utils.SerializeUtils;
import com.example.base.utils.Sha256Hash;
import com.example.fuzzed.ProgramService;
import com.example.net.conf.ApplicationContextProvider;
import com.example.net.conf.P2pNetConfig;
import com.example.net.events.GetProgramQueue;
import com.example.net.events.NewTargetProgramEvent;
import com.example.web.service.PeerService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private static final Logger logger = LoggerFactory.getLogger(ProgramServiceImpl.class);
    private final P2pNetConfig p2pNetConfig;
    private final PeerService peerService;
    // 待测程序队列，队列头为下一次fuzzing的程序
    private final ProgramQueue programQueue;
    private final PayloadManager payloadManager;
    private final int MAX_REQ = 10;
    private final NewPathManager newPathManager;

    @Override
    public boolean uploadTask(String path, String fuzzPath, String name, String desc, Reward reward) {
        File file = new File(path);
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            Program program = new Program("", name, desc, fileBytes,
                    fuzzPath, new Peer(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort()),
                    reward, 0, 0, 0);
            program.setHash(ByteUtils.bytesToHex(Sha256Hash.hash(SerializeUtils.serialize(program))));
            ApplicationContextProvider.publishEvent(new NewTargetProgramEvent(program));
            programQueue.addProgramList(program);
            programQueue.addProgramMap(program);
            logger.info("已广播待测可执行文件：{}, 长度:{}, Node:{}", file.getName(), fileBytes.length, program.getPeer());
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // supplier 将待测程序转换为目标可执行文件
//    @Override
//    public boolean prepareTargetProgram(String path, String objPath, String name, String desc) {
//
//        try {
//            ProcessBuilder processBuilder = new ProcessBuilder();
//            processBuilder.command("afl-gcc", "-no-pie", path, "-o", objPath);
//            Process process = processBuilder.start();
//
//            // 等待命令执行完毕
//            int exitCode = process.waitFor();
//            System.out.println("afl-gcc complete. Exited with code: " + exitCode);
//
//            File file = new File(objPath);
//            // 读取文件的字节
//            byte[] fileBytes = Files.readAllBytes(file.toPath());
//            // 广播program
//            if (reward == null)
//                return false;
//            Program program = new Program(ByteUtils.bytesToHex(Sha256Hash.hash(fileBytes)), name, desc, fileBytes,
//                    new Peer(p2pNetConfig.getServerAddress(), p2pNetConfig.getServerPort()), reward, 0, 0, 0);
//            ApplicationContextProvider.publishEvent(new NewTargetProgramEvent(program));
//            programQueue.addProgramList(program);
//            programQueue.addProgramMap(program);
//            logger.info("已广播待测可执行文件：{}, 长度:{}, Node:{}", file.getName(), fileBytes.length, program.getPeer());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return true;
//    }

    // fuzzer 将接收到的byte[]转换为file，准备fuzz
    @Override
    public String byteToFile(byte[] fileBytes, String path, String name) {
        String absolutePath = path + "/" + name;
        File file = new File(absolutePath);
        try {
            file.createNewFile();
            // 添加执行权限
            file.setExecutable(true);
            try(FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileBytes);
                logger.info("克隆待测程序成功：{}", file.getName());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file.getAbsolutePath();
    }



    @Override
    public boolean receiveProgram(Program program, String directorypath) {
        Peer node = program.getPeer();
        logger.info("收到file，长度为{}; 发送方：{}:{}", program.getProgramCode().length, node.getIp(), node.getPort());
        // 将node存入数据库
        if(!peerService.addSupplierPeer(program.getPeer())){
            logger.info("supplier信息存入数据库");
            return false;
        }
        // 存入本地
        String fileName = "Program_" + program.getHash().substring(0,5);
        byteToFile(program.getProgramCode(), directorypath, fileName);
//        program.setProgramCode(null);
        // 收到program后，放入队列
        programQueue.addProgramMap(program);
        programQueue.addProgramList(program);
//        if(programQueue.addProgramList(program)){
//            CopyOnWriteArrayList<Program> queue = programQueue.getProgramList();
//            for (Program p : queue) {
//                logger.info("队列中内容：文件长度为{},node为{}",p.getProgramCode().length, p.getPeer());
//            }
//        }
//        logger.info("再获取一次ProgramQueue: {}", programQueue.getProgramQueue());

        return true;
    }

    @Override
    public ConcurrentHashMap<String, Program> getTasks() {
        ConcurrentHashMap<String, Program> taskMap = programQueue.getTaskMap();
        return taskMap;
    }

    @Override
    public int getCrashNum() {
        return newPathManager.getCrashNum();
    }

    @Override
    public Pair<String, Peer> chooseTargetProgram(String directorypath, CopyOnWriteArrayList<Program> queue) {
        if (queue.isEmpty()) {
            return null;
        }
        // 选择第一个任务
        Program program = queue.get(0);
        payloadManager.setProgramHash(program.getHash());
        String fuzzPath = program.getFuzzPath();
        Peer node = program.getPeer();
        if (!peerService.addSupplierPeer(node)) {
            logger.info("更新supplier失败");
        }
        Pair<String, Peer> programInfo = new ImmutablePair<>(fuzzPath, node);
        return programInfo;
    }

    @Override
    public boolean receiveTask(Program program, String directorypath) throws IOException {
        Peer node = program.getPeer();
        logger.info("收到file，长度为{}; 发送方：{}:{}", program.getProgramCode().length, node.getIp(), node.getPort());
        // 将node存入数据库
        if(!peerService.addSupplierPeer(program.getPeer())){
            logger.info("supplier信息存入数据库");
            return false;
        }
        byte[] base64Code = program.getProgramCode();
        String base64Data = new String(base64Code);
        base64Data = base64Data.replaceAll("\\s", "");
        // 解码 Base64
        byte[] tarGzBytes = Base64.getDecoder().decode(base64Data);
        // 解压 .tar.gz
        extractTarGz(tarGzBytes, directorypath);
        program.setProgramCode(null);
        // 收到program后，放入队列
        programQueue.addProgramMap(program);
        programQueue.addProgramList(program);
        return true;
    }

    public void extractTarGz(byte[] tarGzBytes, String outputDir) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(tarGzBytes);
             GZIPInputStream gzipStream = new GZIPInputStream(byteStream);
             TarArchiveInputStream tarStream = new TarArchiveInputStream(gzipStream)) {
            TarArchiveEntry entry;
            while ((entry = tarStream.getNextTarEntry()) != null) {
                File outputFile = new File(outputDir, entry.getName());

                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // 确保父目录存在
                    outputFile.getParentFile().mkdirs();
                    // 写入文件
                    try (FileOutputStream fos = new FileOutputStream(outputFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = tarStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
            }
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


}
