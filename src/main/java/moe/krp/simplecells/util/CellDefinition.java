package moe.krp.simplecells.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.BoundingBox;

import java.util.UUID;

@Getter
@Setter
public class CellDefinition {
    private String name;
    private Vec3D lowerBound;
    private Vec3D upperBound;
    private UUID ownedBy;
    private UUID creator;
    private Vec3D signLocation;
    private transient boolean markedForDeletion;
    private transient boolean dirty;

    public CellDefinition(String name, Vec3D lowerBound, Vec3D upperBound, UUID creator) {
        this.name = name;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.creator = creator;
        dirty = true;
    }

    public String toString() {
        return "CellDefinition{" +
                "name='" + name + '\'' +
                ", lowerBound=" + lowerBound +
                ", upperBound=" + upperBound +
                ", ownedBy=" + ownedBy +
                ", signLocation=" + signLocation +
                '}';
    }
}
