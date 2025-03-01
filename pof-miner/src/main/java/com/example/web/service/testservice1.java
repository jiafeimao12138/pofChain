package com.example.web.service;

import com.example.base.entities.Payload;
import com.example.base.entities.PayloadManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class testservice1 {
    private final PayloadManager payloadManager;

    public PayloadManager setPayload() {
        List<Payload> payloadList = new ArrayList<>();
        payloadList.add(new Payload("hello", Arrays.asList(new Integer[]{12,123,123,12,4}), true));
        payloadList.add(new Payload("hello12", Arrays.asList(new Integer[]{12,123,123,12,4}), true));
        payloadList.add(new Payload("hello34", Arrays.asList(new Integer[]{12,1123,12,4}), false));
//        payloads.addPayloads(payloadList);
        return payloadManager;
    }
}
