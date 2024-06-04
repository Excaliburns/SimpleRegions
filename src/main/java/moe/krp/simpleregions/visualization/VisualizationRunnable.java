package moe.krp.simpleregions.visualization;

import lombok.Getter;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.RegionUtils;
import moe.krp.simpleregions.helpers.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VisualizationRunnable extends BukkitRunnable {
    final Location[] locations;
    final Player player;

    @Getter
    int timerCounter;
    @Getter
    final int timerLimit;
    final int period;

    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromBGR(0, 127, 255), .75f);

    public VisualizationRunnable(
            final Vec3D corner1,
            final Vec3D corner2,
            final Player player,
            final int timerCounter,
            final int timerLimit,
            final int period
    ) {
        this.locations = RegionUtils.getCuboidPointsFromBounds(
                corner1,
                corner2,
                Bukkit.getWorld(corner1.getWorld())
        );
        this.player = player;
        this.timerCounter = timerCounter;
        this.timerLimit = timerLimit;
        this.period = period;
        this.runTaskTimer(SimpleRegions.getInstance(), 0, period);
    }

    @Override
    public void run() {
        for (Location l : locations) {
            player.spawnParticle(Particle.REDSTONE, l, 1, dustOptions);
        }

        timerCounter++;
    }
}
