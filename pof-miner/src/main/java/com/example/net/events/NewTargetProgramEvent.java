package com.example.net.events;

import org.springframework.context.ApplicationEvent;
import org.tio.core.Node;


public class NewTargetProgramEvent extends ApplicationEvent {
    private Node node;
    public NewTargetProgramEvent(byte[] fileBytes, Node node) {
        super(fileBytes);
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
