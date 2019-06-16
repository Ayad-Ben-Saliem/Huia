package ly.rqmana.huia.java.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class SQLUtils {

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

    public static long dateToTimestamp(LocalDate localDate){
        LocalDateTime birthday = LocalDateTime.of(localDate, LocalTime.MIN);
        String dateString = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").format(birthday);

        long epochMillis = Timestamp.valueOf(dateString).getTime();
        long epochSeconds = TimeUnit.SECONDS.convert(epochMillis, TimeUnit.MILLISECONDS);
        return epochSeconds;
    }

    public static LocalDate timestampToDate(long timestamp){

        long epochMillis = TimeUnit.MILLISECONDS.convert(timestamp, TimeUnit.SECONDS);
        return new Timestamp(epochMillis).toLocalDateTime().toLocalDate();
    }

}
