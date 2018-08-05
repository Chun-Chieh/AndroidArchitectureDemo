package com.chunchiehliang.androidarchitecureexample.database.converter;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * @author Chun-Chieh Liang on 8/2/18.
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
