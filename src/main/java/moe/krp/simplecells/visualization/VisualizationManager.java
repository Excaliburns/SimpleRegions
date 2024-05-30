package moe.krp.simplecells.visualization;

import moe.krp.simplecells.SimpleCells;
import moe.krp.simplecells.util.CellDefinition;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VisualizationManager {
    private static final Set<UUID> playersVisualizingSet = new HashSet<>();

    public static void displayVisualizationForCell(
            final Player player,
            final CellDefinition definition
    ) {
        synchronized (playersVisualizingSet) {
            if (playersVisualizingSet.contains(player.getUniqueId())) {
                player.sendMessage("You are already visualizing a cell");
                return;
            }

            player.sendMessage("Visualizing cell " + definition.getName() + " for 7 seconds");
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
                        player.sendMessage("Stopped visualizing cell " + definition.getName());
                        this.cancel();
                    }
                }
            }.runTaskTimer(SimpleCells.getInstance(), 0, 20);
        }
    }
}
