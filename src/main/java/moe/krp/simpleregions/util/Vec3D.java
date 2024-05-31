package moe.krp.simpleregions.util;

import com.sk89q.worldedit.math.BlockVector3;
import lombok.Data;
import org.bukkit.Location;

@Data
public class Vec3D {
    private long x;
    private long y;
    private long z;
    private String world;

    public Vec3D(final Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getWorld().getName();
    }

    public Vec3D(final BlockVector3 point, final String world) {
        this.x = point.getX();
        this.y = point.getY();
        this.z = point.getZ();
        this.world = world;
    }
}
