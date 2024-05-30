package moe.krp.simplecells.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class RegionUtils {
    public static Location[] getCuboidPointsFromBounds(Vec3D c1, Vec3D c2, World world) {
        ArrayList<Location> locationArrayList = new ArrayList<>();

        double minX = Math.min(c1.getX(), c2.getX());
        double minY = Math.min(c1.getY(), c2.getY());
        double minZ = Math.min(c1.getZ(), c2.getZ());
        double maxX = Math.max(c1.getX(), c2.getX());
        double maxY = Math.max(c1.getY(), c2.getY());
        double maxZ = Math.max(c1.getZ(), c2.getZ());

        for (double x = minX; x <= maxX + 1; x += .25) {
            for (double y = minY; y <= maxY + 1; y += .25) {
                for (double z = minZ; z <= maxZ + 1; z += .25) {
                    int components = 0;
                    if (x == minX || x == maxX + 1) components++;
                    if (y == minY || y == maxY + 1) components++;
                    if (z == minZ || z == maxZ + 1) components++;
                    if (components >= 2) {
                        locationArrayList.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        return locationArrayList.toArray(new Location[0]);
    }
}
