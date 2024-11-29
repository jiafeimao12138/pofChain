package com.example.net.events;

import org.springframework.context.ApplicationEvent;

import java.io.File;

public class NewTargetProgramEvent extends ApplicationEvent {
    public NewTargetProgramEvent(byte[] fileBytes) {
        super(fileBytes);
    }
}
