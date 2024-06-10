package moe.krp.simpleregions.helpers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.TimeUtils;

import java.time.Duration;

@Data @NoArgsConstructor
public class SignDefinition {
    Double cost;
    Vec3D location;

    @Setter(AccessLevel.NONE)
    String originalDuration;
    String duration;

    transient String regionName;

    public void initDuration(final String duration) {
        this.originalDuration = duration;
        this.duration = duration;
    }

    public boolean isValid() {
        return this.cost != null && this.location != null && this.originalDuration != null && this.duration != null && this.regionName != null;
    }

    public Duration tickDownTime(final Duration duration) {
        final Duration thisDuration = TimeUtils.getDurationFromTimeString(this.duration);
        final Duration newDuration = thisDuration.minus(duration);
        this.duration = TimeUtils.getTimeStringFromDuration(newDuration);
        return newDuration;
    }
}
