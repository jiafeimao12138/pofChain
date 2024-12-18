package com.example.web.service;

import java.util.List;

public interface ProcessService {
    List<String> findProcessIds(String partialName) throws Exception;
    void suspendProcess(String pid) throws Exception;
    void resumeProcess(String pid) throws Exception;
    void killProcess(String pid) throws Exception;
    void stopProcess(String pid) throws Exception;
}
