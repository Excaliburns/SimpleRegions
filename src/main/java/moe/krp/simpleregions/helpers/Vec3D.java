package moe.krp.simpleregions.helpers;

import com.sk89q.worldedit.math.BlockVector3;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Data
public class Vec3D {
    private long x;
    private long y;
    private long z;
    private String world;

    public Vec3D(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

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

    public Vec3D toChunkVec() {
        return new Vec3D((int) x >> 4, 0, (int) z >> 4, world);
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
    public Vector toVector() { return new Vector(x, y, z); }
}
