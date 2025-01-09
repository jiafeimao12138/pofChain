package com.example.web.service;

import com.example.base.Exception.WindowFileException;
import com.example.base.entities.NewPath;
import com.example.base.entities.Payload;
import com.example.base.utils.WindowFileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class test {

    static String aflDirectory = "/home/wj/pofChain/AFL/";
    static String fuzzOut = "fuzz_out";
    static String fuzzIn = "fuzz_in";

    public static void executeCommand(String targetProgram) {
        ProcessBuilder processBuilder = new ProcessBuilder();
//        指定工作目录
        processBuilder.directory(new java.io.File(aflDirectory));
        String fuzzOutDir = fuzzOut;
        processBuilder.command("afl-fuzz", "-i", fuzzIn , "-o", fuzzOutDir , targetProgram);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待命令执行完毕
            int exitCode = process.waitFor();
            System.out.println("AFL结束运行，退出代码: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void directoryWatcher() {
        // 设置需要监控的目录路径
        Path testcases_dir = Paths.get("/home/wj/pofChain/AFL/afl_testfiles/windows/window_testcases"); // 替换为你的目录路径
        Path path_dir = Paths.get("/home/wj/pofChain/AFL/afl_testfiles/windows/window_paths"); // 替换为你的目录路径

        try {
            // 创建 WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // 注册目录以监控创建事件
            testcases_dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            path_dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            System.out.println("开始监控目录: " + testcases_dir + "和" + path_dir);

            // 进入监控循环
            while (true) {
                // 等待下一个事件
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.err.println("监控被中断: " + e.getMessage());
                    return;
                }

                // 处理事件
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    // 确保事件是创建事件
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        // 获取新创建的文件名
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path newFilePath = ((Path) key.watchable()).resolve(ev.context());
                        System.out.println("检测到新文件: " + newFilePath);
                    }
                }
                // 重置 WatchKey
                boolean valid = key.reset();
                if (!valid) {
                    System.err.println("监控键无效，退出监控...");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("无法监控目录: " + e.getMessage());
        }
    }

    // 处理文件的逻辑
    private static void processFile(String testcase, String pathfile) {
        try {
            List<Payload> payloadList = WindowFileUtils.windowFilesToTriple(testcase, pathfile, "");
            System.out.println("payload size:" + payloadList.size());
        } catch (WindowFileException e) {
            throw new RuntimeException(e);
        }
    }

    public void test(){
        ArrayList<NewPath> newPaths = new ArrayList<>();
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "1ed2e131qewqe", 1734295234));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,2,3,4}), "f231wsdefwih2", 1734295236));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqe", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqe", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqk", 1734295299));
        newPaths.add(new NewPath(Arrays.asList(new Integer[]{1,4,3,2}), "1ed2e131qewqk", 1734295299));
        Map<String, List<NewPath>> groupNewPath =
                newPaths.stream().collect(Collectors.groupingBy(NewPath::getFuzzerAddress));

        Comparator<List<NewPath>> comparator = new Comparator<List<NewPath>>() {
            @Override
            public int compare(List<NewPath> l1, List<NewPath> l2) {
                return Integer.compare(l2.size(), l1.size());
            }
        };
        LinkedHashMap<String, List<NewPath>> sortedmap = groupNewPath.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,e2)->e1, LinkedHashMap::new));
        sortedmap.forEach((key, value) -> {
            System.out.println(key + ":" + value);
        });
    }
    public static void main(String[] args) {
//        directoryWatcher();
        executeCommand("/home/wj/pofChain/AFL/afl_testfiles/test_afl_files/string_length");
    }
}
