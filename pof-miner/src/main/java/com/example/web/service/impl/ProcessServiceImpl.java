package com.example.web.service.impl;

import com.example.web.service.ProcessService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProcessServiceImpl implements ProcessService {
    @Override
    public List<String> findProcessIds(String partialName) throws Exception {
        ArrayList<String> processList = new ArrayList<>();
        try {
            // 使用 ps 命令查找 AFL 进程
            ProcessBuilder processBuilder = new ProcessBuilder("ps", "aux");
            Process process = processBuilder.start();

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("afl-fuzz")) { // 假设 AFL 进程名包含 "afl-fuzz"
                    String[] parts = line.trim().split("\\s+");
                    String pid = parts[1]; // PID 通常是第二个字段
                    processList.add(pid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    @Override
    public void suspendProcess(String pid) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("kill", "-STOP", pid);
        builder.start().waitFor();
    }

    @Override
    public void resumeProcess(String pid) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("kill", "-CONT", pid);
        builder.start().waitFor();
    }

    @Override
    public void killProcess(String pid) throws Exception {
        ProcessBuilder builder = new ProcessBuilder("kill", "-9", pid);
        builder.start().waitFor();
    }

    @Override
    public void stopProcess(String pid) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("kill", "-2", pid);
        processBuilder.start().waitFor();
    }
}
