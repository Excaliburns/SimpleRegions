package moe.krp.simpleregions.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import moe.krp.simpleregions.util.TimeUtils;

import java.time.Duration;

@Data @AllArgsConstructor
public class SignDefinition {
    double cost;
    Vec3D location;
    String duration;

    public void tickDownTime(final Duration duration) {
        final Duration thisDuration = TimeUtils.getDurationFromTimeString(this.duration);
        final Duration newDuration = thisDuration.minus(duration);
        this.duration = TimeUtils.getTimeStringFromDuration(newDuration);
    }
}