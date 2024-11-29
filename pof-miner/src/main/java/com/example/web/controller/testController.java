package com.example.web.controller;

import com.example.web.service.testservice1;
import com.example.web.service.testservice2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class testController {
    @Autowired
    private testservice1 service1;
    @Autowired
    private testservice2 service2;

    @RequestMapping("/setPayloads")
    public void getPayloads() {
        System.out.println(service1.setPayload().getPayloads());
    }

    @RequestMapping("/getPayloads")
    public void getPayloads2() {
        System.out.println(service2.getPayloads());
    }
}
