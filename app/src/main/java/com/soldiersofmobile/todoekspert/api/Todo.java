package com.soldiersofmobile.todoekspert.api;

import java.io.Serializable;

public class Todo implements Serializable {
    public String content;
    public boolean done;

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
