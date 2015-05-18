package com.mrcornman.otp.utils;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Jonathan on 5/12/2015.
 */
public class PrettyTime {

    public final static long ONE_SECOND = 1000;
    public final static long ONE_MINUTE = ONE_SECOND * 60;
    public final static long ONE_HOUR = ONE_MINUTE * 60;
    public final static long ONE_DAY = ONE_HOUR * 24;

    private final static String[] birthdayFormats = {
            "mm/dd/yyyy", "yyyy"
    };

    private PrettyTime() {}

    /**
     * Convertes a formatted birthday string to a Date object. Returns null if
     * the birthday could not be converted.
     * @param birthdayString Must be in the format yyyy or mm/dd/yyyy
     * @return The birth Date
     */
    public static Date getDateFromBirthdayString(String birthdayString) {
        if(birthdayString == null || birthdayString.length() == 0) return null;

        for (String format : birthdayFormats) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date dateOfBirth = dateFormat.parse(birthdayString);
                return dateOfBirth;
            } catch (ParseException e) {}
        }

        return null;
    }

    /**
     * Converts a birth Date to an age. Returns -1
     * if the Date could not be converted.
     * @param birthDate Must in the format yyyy or mm/dd/yyyy
     * @return The age as an integer
     */
    public static int getAgeFromBirthDate(Date birthDate) {
        int age = -1;

        if(birthDate == null || birthDate == null) return age;

        Calendar today = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthDate);

        age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dob.get(Calendar.MONTH)) {
            age--;
        } else if (today.get(Calendar.MONTH) == dob.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)) {
            age--;
        }

        return age;
    }

    public static String getTimeAgoFromTimestamp(String timestamp) {
        String result = "Inconceivable!";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-00:02"));
        try {
            Date date = dateFormat.parse(timestamp);
            Date currDate = new Date();
            long duration = currDate.getTime() - date.getTime();
            //Log.i("hi", duration + "");
            long temp = 0;
            if (duration >= ONE_SECOND) {
                result = "";

                temp = duration / ONE_DAY;
                if (temp > 0) result += temp + " day" + (temp > 1 ? "s" : "");
                else {
                    temp = duration / ONE_HOUR;
                    if (temp > 0) result += temp + " hour" + (temp > 1 ? "s" : "");
                    else {
                        temp = duration / ONE_MINUTE;
                        if (temp > 0) result += temp + " minute" + (temp > 1 ? "s" : "");
                        else {
                            result += "a moment ";
                        }
                    }
                }

                result += " ago";
            }
        } catch (ParseException e) {
            //handle exception
            Log.e("Error", e.getMessage());
        }

        return result;
    }
}