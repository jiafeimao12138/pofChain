package com.example.base.entities;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Payloads {
    private List<Payload> payloadList;
    public Payloads() {

    }

    public List<Payload> getPayloads() {
        return payloadList;
    }

    public void setPayloads(List<Payload> payloadList) {
        this.payloadList = payloadList;
    }

    public void setNull() {
        this.payloadList.clear();
    }
}
