package com.example.web.service.impl;

import com.example.web.service.ProcessService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class ProcessServiceImpl implements ProcessService {
    @Override
    public List<String> findProcessIds(String partialName) throws Exception {
        return Collections.emptyList();
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
