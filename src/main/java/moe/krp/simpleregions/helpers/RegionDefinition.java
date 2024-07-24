package moe.krp.simpleregions.helpers;

import com.google.gson.annotations.Since;
import lombok.Data;
import moe.krp.simpleregions.util.ConfigUtils;
import moe.krp.simpleregions.util.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
public class RegionDefinition {
    private String name;
    private String regionType;
    private String world;
    private Vec3D lowerBound;
    private Vec3D upperBound;
    private UUID owner;
    private UUID creator;
    private SignDefinition relatedSign;
    private HashMap<UUID, String> otherAllowedPlayers;
    @Since(1.3)
    private String upkeepTimer;
    @Since(1.3)
    private UUID previousOwner;

    private transient RegionTypeConfiguration configuration;
    private transient boolean markedForDeletion;
    private transient boolean dirty;
    private transient double upkeepCost;

    public RegionDefinition(String name, Vec3D lowerBound, Vec3D upperBound, UUID creator, final String regionType, final String upkeepTimer) {
        this(name, lowerBound, upperBound, creator, regionType);
        this.upkeepTimer = upkeepTimer;
    }

    public RegionDefinition(String name, Vec3D lowerBound, Vec3D upperBound, UUID creator, final String regionType) {
        this.name = name;
        this.world = lowerBound.getWorld(); // these should be the same
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.creator = creator;
        this.regionType = regionType;
        setConfiguration(regionType);
        dirty = true;
        otherAllowedPlayers = new HashMap<>();
    }

    public void setConfiguration(final String regionType) {
        this.configuration = ConfigUtils.getRegionTypeConfiguration(regionType);
        if (configuration.getUpkeepInterval() != null) {
            if (this.upkeepTimer == null) {
                this.upkeepTimer = TimeUtils.getTimeStringFromDuration(configuration.getUpkeepInterval());
            }
        }
    }

    public void setRegionType(final String regionType) {
        this.regionType = regionType;
        this.configuration = ConfigUtils.getRegionTypeConfiguration(regionType);
        this.dirty = true;
    }

    public void clearOwnerAndReset() {
        this.otherAllowedPlayers = new HashMap<>();
        this.owner = null;
        if (this.getRelatedSign() != null) {
            this.getRelatedSign().setDuration(this.getRelatedSign().getOriginalDuration());
        }
        this.dirty = true;
    }

    public RegionDefinition getRegionIfPointWithin(final Vec3D point) {
        final boolean isWithin = point.getX() >= lowerBound.getX() && point.getX() <= upperBound.getX()
                              && point.getY() >= lowerBound.getY() && point.getY() <= upperBound.getY()
                              && point.getZ() >= lowerBound.getZ() && point.getZ() <= upperBound.getZ();
        return isWithin ? this : null;
    }

    public Set<Vec3D> getEnvelopingChunkVectors() {
        final HashSet<Vec3D> chunkVectors = new HashSet<>();

        for (int x = (int) lowerBound.getX(); x <= (int) upperBound.getX() + 15; x += 16) {
            for (int z = (int) lowerBound.getZ(); z <= (int) upperBound.getZ() + 15; z += 16) {
                chunkVectors.add(new Vec3D(x >> 4, 0, z >> 4, world));
            }
        }

        return chunkVectors;
    }

    public Duration tickDownTime(final Duration minusDuration) {
        final Duration thisDuration = TimeUtils.getDurationFromTimeString(upkeepTimer);
        final Duration newDuration = thisDuration.minus(minusDuration);
        this.upkeepTimer = TimeUtils.getTimeStringFromDuration(newDuration);
        return newDuration;
    }

    public double getUpkeepCost() {
        return configuration.getUpkeepCost();
    }

    public Duration getUpkeepInterval() {
        return configuration.getUpkeepInterval().isZero() || configuration.getUpkeepInterval().isNegative() ? null : configuration.getUpkeepInterval();
    }

    public List<Component> getFormattedChatInformation() {
        final ArrayList<Component> components = new ArrayList<>();
        components.add(
                Component.text("----------------")
                         .color(TextColor.color(0xFFBA08))
                         .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED)
                         .append(Component.text("Region Info"))
                         .append(Component.text("----------------"))
                        .append(Component.newline())
        );

        components.add(
                Component.text("Name: ").color(TextColor.color(0x3F88C5))
                        .append(Component.text(name).color(TextColor.color(0xFDFBA08)))
                        .append(Component.text(" (type=" + regionType +")").color(TextColor.color(0x778EA0)))
        );
        components.add(
                Component.text("World: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(world).color(TextColor.color(0xFDFBA08)))
        );
        components.add(
                Component.text("Lower Bound: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text("[X=" + lowerBound.getX() + ", Y=" + lowerBound.getY() + ", Z=" + lowerBound.getZ() + ", world=" + lowerBound.getWorld() + "]").color(TextColor.color(0xFDFBA08)))

        );
        components.add(
                Component.text("Upper Bound: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text("[X=" + upperBound.getX() + ", Y=" + upperBound.getY() + ", Z=" + upperBound.getZ() + ", world=" + upperBound.getWorld() + "]").color(TextColor.color(0xFDFBA08)))

        );
        components.add(
                Component.text("Owner: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(owner != null ? owner.toString() : "None").color(TextColor.color(0xFDFBA08)))
        );
        components.add(
                Component.text("Creator: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(creator.toString()).color(TextColor.color(0xFDFBA08)))
        );
        if (relatedSign == null) {
            components.add(
                    Component.text("Related Sign: ").color(TextColor.color(0x3F88C5))
                             .append(Component.text("None").color(TextColor.color(0xFDFBA08)))
            );
        }
        else {
            components.add(
                    Component.empty().append(Component.text("Related Sign:").color(TextColor.color(0x3F88C5)))
                             .color(TextColor.color(0xFDFBA08))
                             .append(Component.newline())
                             .append(Component.text("    Cost=" + relatedSign.getCost()))
                             .append(Component.newline())
                             .append(Component.text("    Location=[X=" + relatedSign.getLocation().getX() + ", Y=" + relatedSign.getLocation().getY() + ", Z=" + relatedSign.getLocation().getZ() + ", world=" + relatedSign.getLocation().getWorld() + "]"))
                             .append(Component.newline())
                             .append(Component.text("    Original Duration=" + relatedSign.getOriginalDuration()))
                             .append(Component.newline())
                             .append(Component.text("    Duration=" + relatedSign.getDuration()))
                             .append(Component.newline())
                             .append(Component.text("    Never Expire=" + relatedSign.isNeverExpire()))
            );
        }
        final List<Component> otherAllowedPlayersComponents = new ArrayList<>();
        otherAllowedPlayersComponents.add(
                Component.text("Other Allowed Players: ").color(TextColor.color(0x3F88C5))
        );
        if (otherAllowedPlayers.isEmpty()) {
            otherAllowedPlayersComponents.set(0, otherAllowedPlayersComponents.get(0).append(
                    Component.text("None!").color(TextColor.color(0xFDFBA08))
            ));
        }
        else {
            otherAllowedPlayers.forEach( (key, value) ->
                    otherAllowedPlayersComponents.add(
                            Component.text("[UUID=" + key.toString() + ", Name=" + value + "]").color(TextColor.color(0xFDFBA08))
                    )
            );
        }
        components.addAll(otherAllowedPlayersComponents);

        components.add(
                Component.text("Original Upkeep: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(getUpkeepInterval() != null ? TimeUtils.getTimeStringFromDuration(getUpkeepInterval()) : "None!").color(TextColor.color(0xFDFBA08)))
        );
        components.add(
                Component.text("Upkeep Timer: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(upkeepTimer != null ? upkeepTimer : "None!").color(TextColor.color(0xFDFBA08)))
        );
        components.add(
                Component.text("Upkeep Cost: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(upkeepCost).color(TextColor.color(0xFDFBA08)))
        );
        components.add(
                Component.text("Previous Owner: ").color(TextColor.color(0x3F88C5))
                         .append(Component.text(previousOwner != null ? previousOwner.toString() : "None!").color(TextColor.color(0xFDFBA08)))
        );

        return components;
    }
}
