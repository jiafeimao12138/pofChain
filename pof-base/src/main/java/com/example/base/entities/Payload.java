package com.example.base.entities;


import java.io.Serializable;
import java.util.List;

public class Payload implements Serializable {
    private String input;
    private List<Integer> path;
    private boolean isCrash;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public List<Integer> getPath() {
        return path;
    }

    public void setPath(List<Integer> path) {
        this.path = path;
    }

    public boolean isCrash() {
        return isCrash;
    }

    public void setCrash(boolean crash) {
        isCrash = crash;
    }

    public Payload(){

    }
    public Payload(String input, List<Integer> path, boolean isCrash) {
        this.input = input;
        this.path = path;
        this.isCrash = isCrash;
    }

    @Override
    public String toString() {
        return "Payload{" +
                "input='" + input + '\'' +
                ", path='" + path + '\'' +
                ", isCrash=" + isCrash +
                '}';
    }
}
