package com.example.base.entities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class NewPathManager {
    private HashMap<String, List<NewPath>> paths = new HashMap<>();

    public boolean addPathHashMap(String address, List<NewPath> paths) {
        if (this.paths.containsKey(address)) {
            return this.paths.get(address).addAll(paths);
        }
        this.paths.put(address, paths);
        return true;
    }

    public HashMap<String, List<NewPath>> getPaths() {
        return paths;
    }

}
