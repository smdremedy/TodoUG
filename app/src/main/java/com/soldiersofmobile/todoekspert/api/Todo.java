package com.soldiersofmobile.todoekspert.api;

import java.io.Serializable;
import java.util.Map;

public class Todo implements Serializable {
    public String objectId;
    public String content;
    public boolean done;

    public Map<String, ParseAcl> ACL;

    public Todo() {
    }

    public Todo(String content, boolean done) {
        this.content = content;
        this.done = done;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "content='" + content + '\'' +
                ", done=" + done +
                '}';
    }
}
