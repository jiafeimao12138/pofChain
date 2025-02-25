package com.example.base.entities;

import com.example.base.crypto.CryptoUtils;

public class Peer {
    private String id;
    private String ip;
    private int port;
    public Peer(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
        this.id = CryptoUtils.SHA256(this.toString());
    }
    public Peer()
    {
        this(null, 0);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getUrl() {
        return String.format("%s:%s", getIp(), getPort());
    }

    @Override
    public String toString()
    {
        return String.format("%s:%s", getIp(), getPort());
    }
}
