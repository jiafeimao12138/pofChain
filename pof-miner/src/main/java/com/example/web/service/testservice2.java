package com.example.web.service;

import com.example.base.entities.Payload;
import com.example.base.entities.PayloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class testservice2 {
    private final PayloadManager payloadManager;

    public List<Payload> getPayloadManager() {
        List<Payload> payloads1 = payloadManager.getPayloads();
        System.out.println("getPayloads: " + payloads1);
        payloadManager.setNull();
        return payloadManager.getPayloads();
    }
}
