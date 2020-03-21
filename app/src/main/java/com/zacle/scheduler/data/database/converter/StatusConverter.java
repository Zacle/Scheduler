package com.zacle.scheduler.data.database.converter;

import androidx.room.TypeConverter;

import com.zacle.scheduler.utils.EventStatus;

import static com.zacle.scheduler.utils.EventStatus.COMING;
import static com.zacle.scheduler.utils.EventStatus.COMPLETED;
import static com.zacle.scheduler.utils.EventStatus.RUNNING;

public class StatusConverter {
    @TypeConverter
    public static EventStatus getStatus(int status) {
        if (status == COMING.getCode()) {
            return COMING;
        } else if (status == RUNNING.getCode()) {
            return RUNNING;
        } else if (status == COMPLETED.getCode()) {
            return COMPLETED;
        } else {
            throw new IllegalArgumentException("Could not recognize status");
        }
    }

    @TypeConverter
    public static int getCode(EventStatus status) {
        return status.getCode();
    }
}
