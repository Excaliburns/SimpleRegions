package moe.krp.simpleregions.helpers;

import lombok.Data;
import moe.krp.simpleregions.util.ConfigUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    private transient RegionTypeConfiguration configuration;
    private transient boolean markedForDeletion;
    private transient boolean dirty;

    public RegionDefinition(String name, Vec3D lowerBound, Vec3D upperBound, UUID creator, final String regionType) {
        this.name = name;
        this.world = lowerBound.getWorld(); // these should be the same
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.creator = creator;
        this.regionType = regionType;
        this.configuration = ConfigUtils.getRegionTypeConfiguration(regionType);
        dirty = true;
        otherAllowedPlayers = new HashMap<>();
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
}
