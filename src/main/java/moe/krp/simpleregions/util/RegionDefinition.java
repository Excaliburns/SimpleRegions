package moe.krp.simpleregions.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class RegionDefinition {
    private String name;
    private String regionType;
    private Vec3D lowerBound;
    private Vec3D upperBound;
    private UUID ownedBy;
    private UUID creator;
    private SignDefinition relatedSign;
    private List<UUID> otherAllowedPlayers;

    private transient boolean markedForDeletion;
    private transient boolean dirty;

    public RegionDefinition(String name, Vec3D lowerBound, Vec3D upperBound, UUID creator, final String regionType) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.creator = creator;
        this.regionType = regionType;
        dirty = true;
        otherAllowedPlayers = new ArrayList<>();
    }
}
