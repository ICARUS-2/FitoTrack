package de.tadris.fitness.data;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeSpanClassifier {
    public static String classifyTimeSpan(long spanStart, long spanEnd) {
        // Ensure valid input
        if (spanEnd < spanStart) {
            throw new IllegalArgumentException("span_end must be greater than or equal to span_start");
        }

        if (spanStart < 0){return "Overall";}

        long diffMillis = spanEnd - spanStart;
        long diffDays = Duration.ofMillis(diffMillis).toDays();

        ZonedDateTime startDate = Instant.ofEpochMilli(spanStart).atZone(ZoneId.systemDefault());
        ZonedDateTime endDate = Instant.ofEpochMilli(spanEnd).atZone(ZoneId.systemDefault());

        if (diffDays == 1) {
            return "Day";
            // on initial Statistics tab open with the week filter, diffDays is 7, otherwise it is 6
        } else if (diffDays == 7 || diffDays == 6) {
            return "Week";
        } else if (startDate.getMonth() != endDate.getMonth() || diffDays >= 28 && diffDays <= 31) {
            return "Month";
        } else if (startDate.getYear() != endDate.getYear()) {
            return "Year";
        } else {
            return "ERROR";
        }
    }

}
