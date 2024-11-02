package com.example;

import com.example.base.utils.CmdArgsParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class MinerApplication {

    public static void main(String[] args) throws IOException {
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
