package com.zacle.scheduler.service.alarm;

public interface Alarm {
    void startAlarm(int id, String name);
    void editAlarm(int id, String name);
    void cancelAlarm(int id);
}
