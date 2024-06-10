package moe.krp.simpleregions.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.TimeUtils;

import java.time.Duration;

@Data
public class SignDefinition {
    double cost;
    Vec3D location;
    String originalDuration;
    String duration;

    transient String regionName;

    public SignDefinition(
            final double cost,
            final Vec3D location,
            final String originalDuration,
            final String regionName
    ) {
        this.cost = cost;
        this.location = location;
        this.originalDuration = originalDuration;
        this.duration = originalDuration;
        this.regionName = regionName;
    }

    public Duration tickDownTime(final Duration duration) {
        final Duration thisDuration = TimeUtils.getDurationFromTimeString(this.duration);
        final Duration newDuration = thisDuration.minus(duration);
        this.duration = TimeUtils.getTimeStringFromDuration(newDuration);
        return newDuration;
    }
}
