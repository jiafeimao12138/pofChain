package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class MinerApplication {

    public static void main(String[] args) throws IOException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("./deleteDBfiles.sh");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String s : args) {
            System.out.println(s);
        }
        PofRunner pofRunner = new PofRunner(args);
        if (!pofRunner.preparation()) {
            System.exit(0);
        }
        SpringApplication.run(MinerApplication.class, args);
    }

}
