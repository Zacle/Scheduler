package com.zacle.scheduler.data.database.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Convert Date object to a type that can be persisted to the database
 */

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
