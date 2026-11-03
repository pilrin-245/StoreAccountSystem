package com.store.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类。
 */
public final class DateUtil {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String MONTH_PATTERN = "yyyy-MM";

    private DateUtil() {
    }

    public static String formatDate(Date date) {
        return date == null ? "" : new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static String formatDateTime(Date date) {
        return date == null ? "" : new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }

    public static String formatMonth(Date date) {
        return date == null ? "" : new SimpleDateFormat(MONTH_PATTERN).format(date);
    }

    public static String today() {
        return formatDate(new Date());
    }

    public static String currentMonth() {
        return formatMonth(new Date());
    }

    /**
     * 解析 yyyy-MM-dd，返回当天 00:00:00；解析失败返回 null。
     */
    public static Timestamp parseStart(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            sdf.setLenient(false);
            return new Timestamp(sdf.parse(date.trim()).getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 解析 yyyy-MM-dd，返回当天 23:59:59；解析失败返回 null。
     */
    public static Timestamp parseEnd(String date) {
        Timestamp start = parseStart(date);
        if (start == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static Timestamp daysAgo(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static Timestamp startOfMonth(String month) {
        if (month == null || month.trim().isEmpty()) {
            month = currentMonth();
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(MONTH_PATTERN);
            sdf.setLenient(false);
            Date date = sdf.parse(month.trim());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            return new Timestamp(calendar.getTimeInMillis());
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 指定月份的最后一天 23:59:59。
     */
    public static Timestamp endOfMonth(String month) {
        Timestamp start = startOfMonth(month);
        if (start == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.SECOND, -1);
        return new Timestamp(calendar.getTimeInMillis());
    }
}
