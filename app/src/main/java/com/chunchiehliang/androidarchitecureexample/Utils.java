package com.chunchiehliang.androidarchitecureexample;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Chun-Chieh Liang on 8/6/18.
 */
public class Utils {

    private static final int DATE_TODAY = 0;
    private static final int DATE_PAST = -1;

    /** Get the difference between the event date and today */
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



}
