package com.example.web.service;

import com.example.base.entities.Payload;
import com.example.base.entities.Payloads;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class testservice2 {
    private final Payloads payloads;

    public List<Payload> getPayloads() {
        return payloads.getPayloads();
    }
}
