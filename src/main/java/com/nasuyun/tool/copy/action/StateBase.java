package com.nasuyun.tool.copy.action;

public class StateBase implements State {

    private Status status;
    private String message;

    public StateBase() {
        this.status = Status.WAIT;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    public void status(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void failure(String message) {
        this.status = Status.FAILED;
        this.message = message;
    }
}
