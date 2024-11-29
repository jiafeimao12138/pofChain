package com.example.fuzzed;

import java.nio.file.Path;

public interface ProgramService {
    boolean prepareTargetProgram(String path, String objPath);
    void byteToFile(byte[] fileBytes, String path, String name);
    Path chooseTargetProgram(String path);
}
