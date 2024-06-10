package moe.krp.simpleregions.listeners;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class RegionListeners implements Listener {
    final static StorageManager storageManager = SimpleRegions.getStorageManager();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();
        final Vec3D location = new Vec3D(e.getBlock().getLocation());
        final RegionDefinition def = storageManager.findRegionByPoint(location);
        if (def == null) {
            return;
        }

        if (player.hasPermission("SimpleRegions.bypass")) {
            return;
        }

        if (
                def.getOwner() == null
                || !def.getOwner().equals(player.getUniqueId())
                || !def.getOtherAllowedPlayers().contains(player.getUniqueId())
        ) {
            ChatUtils.sendErrorMessage(player, "You aren't allowed to break blocks in this region!");
            e.setCancelled(true);
        }
    }
}
