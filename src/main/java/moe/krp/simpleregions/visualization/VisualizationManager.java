package moe.krp.simpleregions.visualization;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.RegionDefinition;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VisualizationManager {
    private static final Set<UUID> playersVisualizingSet = new HashSet<>();

    public static void displayVisualizationForregion(
            final Player player,
            final RegionDefinition definition
    ) {
        synchronized (playersVisualizingSet) {
            if (playersVisualizingSet.contains(player.getUniqueId())) {
                player.sendMessage("You are already visualizing a region");
                return;
            }

            player.sendMessage("Visualizing region " + definition.getName() + " for 7 seconds");
            playersVisualizingSet.add(player.getUniqueId());
            new BukkitRunnable() {
                final VisualizationRunnable visualizationRunnable = new VisualizationRunnable(
                        definition.getLowerBound(),
                        definition.getUpperBound(),
                        player,
                        0,
                        14,
                        10
                );

                @Override
                public void run() {
                    if (visualizationRunnable.getTimerCounter() > visualizationRunnable.getTimerLimit()) {
                        visualizationRunnable.cancel();
                        playersVisualizingSet.remove(player.getUniqueId());
                        player.sendMessage("Stopped visualizing region " + definition.getName());
                        this.cancel();
                    }
                }
            }.runTaskTimer(SimpleRegions.getInstance(), 0, 20);
        }
    }
}
