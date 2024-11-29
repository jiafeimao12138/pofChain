package com.example.net.events;

import com.example.base.entities.NewPath;
import org.springframework.context.ApplicationEvent;

import java.util.LinkedHashMap;
import java.util.List;

public class NewPathRank extends ApplicationEvent {

    public NewPathRank(LinkedHashMap<String, List<NewPath>> rank) {
        super(rank);
    }
}
