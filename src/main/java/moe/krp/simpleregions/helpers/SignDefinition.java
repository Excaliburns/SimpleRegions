package moe.krp.simpleregions.helpers;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.krp.simpleregions.util.TimeUtils;

import java.time.Duration;

@Data @NoArgsConstructor
public class SignDefinition {
    Double cost;
    Vec3D location;

    @Setter(AccessLevel.NONE)
    String originalDuration;
    String duration;
    boolean neverExpire = false;

    transient String regionName;

    public void initInfiniteDuration() {
        neverExpire = true;
    }

    public void initDuration(final String duration) {
        this.originalDuration = duration;
        this.duration = duration;
    }

    public boolean isValid() {
        return this.cost != null && this.location != null && this.regionName != null
                && ((this.originalDuration != null && this.duration != null) || this.neverExpire);
    }

    public String getDuration() {
        return neverExpire ? "Never!" : duration;
    }

    public Duration tickDownTime(final Duration duration) {
        if (neverExpire) {
            return Duration.ofDays(1); // always return a duration in the future, never expire.
        }

        final Duration thisDuration = TimeUtils.getDurationFromTimeString(this.duration);
        final Duration newDuration = thisDuration.minus(duration);
        this.duration = TimeUtils.getTimeStringFromDuration(newDuration);
        return newDuration;
    }
}
