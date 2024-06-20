package com.cnrs.opentraduction.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public final static String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";


    public static String formatLocalDate(LocalDateTime dateTime, String format) {

        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }
}
