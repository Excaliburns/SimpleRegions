package moe.krp.simpleregions.util;

import java.time.Duration;
import java.util.Objects;

public class TimeUtils {
    public static Duration getDurationFromTimeString(final String timeString) throws IllegalArgumentException {
        if (timeString == null) {
            return Duration.ZERO;
        }
        // format is 1d2h3m4s
        final String[] timeParts = timeString.split("(?<=\\D)(?=\\d|-\\d)|(?<=\\d)(?=\\D)");
        Duration duration = Duration.ZERO;
        boolean isNegative = false;
        Long timeValue = null;
        for (final String token : timeParts) {
            if (token.equals("-")) {
                isNegative = true;
                continue;
            }
            if (timeValue == null) {
                timeValue = Long.parseLong(token);
                continue;
            }
            duration = switch (token) {
                case "d" -> isNegative ? duration.minusDays(timeValue) : duration.plusDays(timeValue);
                case "h" -> isNegative ? duration.minusHours(timeValue) : duration.plusHours(timeValue);
                case "m" -> isNegative ? duration.minusMinutes(timeValue) : duration.plusMinutes(timeValue);
                case "s" -> isNegative ? duration.minusSeconds(timeValue) : duration.plusSeconds(timeValue);
                default -> throw new IllegalArgumentException("Invalid time unit: " + token);
            };

            isNegative = false;
            timeValue = null;
        }

        return duration;
    }

    public static String getTimeStringFromDuration(final Duration duration) {
        final long days = duration.toDays();
        final long hours = duration.minusDays(days).toHours();
        final long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        final long seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();
        return days + "d" + hours + "h" + minutes + "m" + seconds + "s";
    }
}
