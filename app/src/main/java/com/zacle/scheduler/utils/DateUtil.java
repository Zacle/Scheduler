package com.zacle.scheduler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    private DateUtil() {}

    public static Date getDate(int year, int month, int day, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute);
        return calendar.getTime();
    }

    public static String formatDate(long date) {
        String pattern = "EEE, d MMM yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String formatedDate = simpleDateFormat.format(date);
        return formatedDate;
    }

    public static Date parseDate(String date) {
        String pattern = "EEE, d MMM yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date parsedDate = null;
        try {
            parsedDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedDate;
    }

    public static String formatTime(long date) {
        String pattern = "hh:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String formatedDate = simpleDateFormat.format(date);
        return formatedDate;
    }

    public static Date parseTime(String date) {
        String pattern = "hh:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date parsedTime = null;
        try {
            parsedTime = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsedTime;
    }
}
