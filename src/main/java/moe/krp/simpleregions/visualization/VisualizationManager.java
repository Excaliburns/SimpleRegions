package moe.krp.simpleregions.visualization;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VisualizationManager {
    public static void displayVisualizationForRegion(
            final Player player,
            final RegionDefinition definition
    ) {
        ChatUtils.sendMessage(player, "Visualizing region " + definition.getName() + " for 7 seconds");
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
                    ChatUtils.sendMessage(player, "Stopped visualizing region " + definition.getName());
                    this.cancel();
                }
            }
        }.runTaskTimer(SimpleRegions.getInstance(), 0, 20);
    }
}
