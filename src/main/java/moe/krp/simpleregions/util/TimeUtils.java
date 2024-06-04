package moe.krp.simpleregions.util;

import java.time.Duration;

public class TimeUtils {
    public static Duration getDurationFromTimeString(final String timeString) throws IllegalArgumentException {
        if (timeString == null) {
            return Duration.ZERO;
        }
        // format is 1d2h3m4s
        final String[] timeParts = timeString.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        Duration duration = Duration.ZERO;
        for (int i = 0; i < timeParts.length; i += 2) {
            final String timeUnit = timeParts[i + 1];
            final long timeValue = Long.parseLong(timeParts[i]);
            duration = switch (timeUnit) {
                case "d" -> duration.plusDays(timeValue);
                case "h" -> duration.plusHours(timeValue);
                case "m" -> duration.plusMinutes(timeValue);
                case "s" -> duration.plusSeconds(timeValue);
                default -> throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
            };
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
