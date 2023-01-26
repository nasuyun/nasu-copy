package com.nasuyun.tool.copy.exec.task;

import com.nasuyun.tool.copy.action.Action;
import com.nasuyun.tool.copy.action.State;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
public class Task {
    private String name;
    private Date startTime;
    private Date stopTime;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Action action;
    private String message;

    public Task(String name, Action action) {
        this.name = name;
        this.action = action;
        this.startTime = new Date();
    }

    public State getState() {
        return action.state();
    }

    public String getType() {
        return action.type();
    }

    public Object executeAction() {
        return action.execute();
    }

    public Action action() {
        return action;
    }

    public void cancel() {
        action.cancel();
    }
}
