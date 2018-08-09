package com.chunchiehliang.androidarchitecureexample;

import android.support.v4.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Chun-Chieh Liang on 8/6/18.
 */
public class Utils {

    private static final int DATE_TODAY = 0;
    private static final int DATE_PAST = -1;
    private static final String DATE_FORMAT = "MM/dd/yyyy";

    /**
     * Get the difference between the event date and today
     */
    public static int getDayDiff(Date eventDate) {
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTime(new Date());
        currentCal.set(Calendar.HOUR_OF_DAY, 0);
        currentCal.set(Calendar.MINUTE, 0);
        currentCal.set(Calendar.SECOND, 0);
        currentCal.set(Calendar.MILLISECOND, 0);

        long unit = 1000 * 60 * 60 * 24;
        long dayDiff = (eventDate.getTime() - currentCal.getTime().getTime()) / unit;
        return (int) dayDiff;
    }

    /**
     * Get the corresponding color of the remain days
     *
     * @param dayDiff Difference between the event date and today
     * @return resource id of the color
     */
    public static int getDayColor(int dayDiff) {
        int dayColorResourceId;

        if (dayDiff <= DATE_PAST) {
            dayColorResourceId = R.color.colorPast;
        } else if (dayDiff == DATE_TODAY) {
            dayColorResourceId = R.color.colorToday;
        } else if (dayDiff <= 3) {
            dayColorResourceId = R.color.color1Week;
        } else if (dayDiff <= 14) {
            dayColorResourceId = R.color.color2Week;
        } else if (dayDiff <= 21) {
            dayColorResourceId = R.color.color3Week;
        } else if (dayDiff <= 30) {
            dayColorResourceId = R.color.color1Month;
        } else if (dayDiff <= 60) {
            dayColorResourceId = R.color.color2Month;
        } else if (dayDiff <= 180) {
            dayColorResourceId = R.color.color6Month;
        } else {
            dayColorResourceId = R.color.colorDefault;
        }
        return ContextCompat.getColor(BaseApp.getContext(), dayColorResourceId);
    }


    private static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    }


    public static String dateToString(Date date) {
        return getDateFormat().format(date);
    }


    public static Date stringToDate(String dateString) {
        Date date = null;
        try {
            date = getDateFormat().parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

}
