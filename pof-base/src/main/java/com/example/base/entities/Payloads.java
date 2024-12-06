package com.example.base.entities;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Payloads {
    private List<Payload> payloadList = new ArrayList<>();
    public Payloads() {
    }

    public Payloads(List<Payload> payloadList) {
        this.payloadList = payloadList;
    }

    public List<Payload> getPayloads() {
        return payloadList;
    }

    public void setPayloads(List<Payload> payloadList) {
        this.payloadList = payloadList;
    }

    public void addPayloads(List<Payload> payloadList1) {
        this.payloadList.addAll(payloadList1);
    }

    public void setNull() {
        this.payloadList.clear();
    }
}
