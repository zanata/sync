package org.zanata.sync.util;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class DateUtil {
//    private final static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(
            DATE_TIME_FORMAT).format(date);
    }

    public static Date addMilliseconds(Date date, long milliseconds) {
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        LocalDateTime completedTime = ldt.plus(milliseconds, ChronoField.MILLI_OF_DAY.getBaseUnit());
        return Date.from(completedTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
