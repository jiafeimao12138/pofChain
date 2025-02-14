package com.example.web.controller;

import com.example.web.service.ChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("common")
@RequiredArgsConstructor
public class ObserverController {

    private final ChainService chainService;

    @RequestMapping("getLocalBlocks")
    public List<Integer> getLocalBlocks() {
        return chainService.getLocalBlocksHeight();
    }
}
