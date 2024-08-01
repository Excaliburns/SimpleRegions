package moe.krp.simpleregions.helpers;

import com.sk89q.worldedit.util.formatting.text.format.Style;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.krp.simpleregions.util.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.w3c.dom.Text;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    public void initDuration(final String originalDuration, final String durationRemaining) {
        this.originalDuration = originalDuration;
        this.duration = durationRemaining;
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
