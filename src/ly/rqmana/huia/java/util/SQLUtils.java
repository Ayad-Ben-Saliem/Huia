package ly.rqmana.huia.java.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class SQLUtils {

    private static final String DATE_TIME_FORMAT
            = "[yyyy-MM-dd HH:mm:ss[.SSS[SSS]][ Z]]"
            + "[ddMMMyyyy HH:mm:ss[.SSS[SSS]][ Z]]"
            + "[yyyy-MM-dd'T'HH:mm:ss[.SSS[SSS]][ Z]]"
            + "[ddMMMyyyy'T'HH:mm:ss[.SSS[SSS]][ Z]]"
            + "[ddMMMyyyy:HH:mm:ss[.SSS[SSS]][ Z]]";

    private SQLUtils(){}

    public String stringToBoolean(boolean bool){
        return String.valueOf(bool);
    }

    public static boolean getBoolean(String booleanString){
        if (booleanString.equalsIgnoreCase("true"))
            return true;
        else if (booleanString.equalsIgnoreCase("false"))
            return false;
        else
            throw new IllegalArgumentException("Unable to obtain boolean from string ["+ booleanString+"].");
    }

    public static boolean getBoolean(int booleanInt){
        // any value other than 0 is true.
        return booleanInt != 0;
    }

    public static long localDate2Timestamp(LocalDate localDate){
        LocalDateTime birthday = LocalDateTime.of(localDate, LocalTime.MIN);
        String dateString = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(birthday);

        long epochMillis = Timestamp.valueOf(dateString).getTime();
        return TimeUnit.SECONDS.convert(epochMillis, TimeUnit.MILLISECONDS);
    }

    public static LocalDateTime timestamp2DateTime(long timestamp){
        long epochMillis = TimeUnit.MILLISECONDS.convert(timestamp, TimeUnit.SECONDS);
        return new Timestamp(epochMillis).toLocalDateTime();
    }

    public static LocalDateTime sqlDateTime2LocalDateTime(String SQLDateTime){
        return LocalDateTime.parse(SQLDateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static String localDateTime2SQLDateTime(LocalDateTime localDateTime){
        return DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(localDateTime);
    }
}
