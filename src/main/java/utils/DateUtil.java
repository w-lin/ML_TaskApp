package utils;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private final static String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

    public static Long getISO8601timeFromString(String dateStr)
    {
        if(dateStr==null || dateStr.trim().equals(""))
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_DATE_FORMAT);
        try {
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            return null;
        }
    }
}
