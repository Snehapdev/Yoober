package com.database.yoober.yoober_app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String validateDateFormat(String userInput) {
        try {
            SimpleDateFormat dateFormatWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat dateFormatWithoutTime = new SimpleDateFormat("yyyy-MM-dd");

            Date parsedDate;

            if (userInput.length() == 10) {
                // If only the date is provided without time, append the time part
                parsedDate = dateFormatWithoutTime.parse(userInput);
                return dateFormatWithTime.format(parsedDate);
            } else {
                // If the user provides date and time, validate the format
                parsedDate = dateFormatWithTime.parse(userInput);
                return dateFormatWithTime.format(parsedDate);
            }
        } catch (ParseException e) {
            System.out.println(
                    "Invalid format. Please enter the date and time in the correct format (YYYY-MM-DD HH:MM:SS):\n");
            return "Incorrect Format";
        }
    }

    // You can add other static methods if needed
}
