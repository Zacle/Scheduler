package com.zacle.scheduler.utils;

public enum EventStatus {
    COMING(0),
    RUNNING(1),
    COMPLETED(2);

    private int code;

    EventStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
