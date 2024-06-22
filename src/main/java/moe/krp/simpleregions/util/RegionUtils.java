package moe.krp.simpleregions.util;

import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RegionUtils {
    public static void removeBlocksOnExpiry(
            final World world,
            final RegionDefinition region
    ) {
        final Set<String> whitelist = new HashSet<>(region.getConfiguration().getRemoveItemBlockFilter());

        for (long x = region.getLowerBound().getX(); x <= region.getUpperBound().getX(); x++) {
            for (long y = region.getLowerBound().getY(); y <= region.getUpperBound().getY(); y++) {
                for (long z = region.getLowerBound().getZ(); z <= region.getUpperBound().getZ(); z++) {
                    final Block block = world.getBlockAt((int) x, (int) y, (int) z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    if (whitelist.isEmpty()) {
                        block.breakNaturally();
                        continue;
                    }
                    final boolean isSign = block.getState() instanceof Sign;
                    if (whitelist.contains("all-signs") && isSign) {
                        block.breakNaturally();
                    }
                    else if (whitelist.contains(block.getType().toString())) {
                        block.breakNaturally();
                    }
                }
            }
        }
    }

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
