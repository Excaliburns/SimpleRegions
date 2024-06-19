package moe.krp.simpleregions.listeners;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.enums.InteractionType;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.util.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class RegionListeners implements Listener {
    final static StorageManager storageManager = SimpleRegions.getStorageManager();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void placeBlockEvent(final BlockPlaceEvent e) {
        final Player player = e.getPlayer();
        if (player.hasPermission("SimpleRegions.bypass")) {
            return;
        }

        storageManager.findRegionByPoint(e.getBlockPlaced().getLocation())
                .filter(ConfigUtils.playerAllowed(player, InteractionType.PLACE_BLOCK).negate())
                .ifPresent( def -> {
                    ChatUtils.sendErrorMessage(player, "You aren't allowed to place blocks in this region!");
                    e.setCancelled(true);
                });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerInteractEvent(final PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        if (player.hasPermission("SimpleRegions.bypass")) {
            return;
        }

        if (!e.hasBlock() || e.getClickedBlock() == null) {
            return;
        }
        storageManager.findRegionByPoint(e.getClickedBlock().getLocation())
                .ifPresent( regionDefinition -> {
                    InteractionType type;
                    if (e.getClickedBlock().getType().equals(Material.CHEST)) {
                        type = InteractionType.CHEST;
                    }
                    else if (regionDefinition.getRelatedSign() != null
                            && regionDefinition.getOwner() == null
                            && regionDefinition.getRelatedSign().getLocation().equals(new Vec3D(e.getClickedBlock().getLocation()))
                    ) {
                        type = InteractionType.BUY;
                    }
                    else if (e.getClickedBlock().getState() instanceof Sign) {
                        type = InteractionType.SIGN;
                    }
                    else {
                        type = InteractionType.INTERACT;
                    }
                    boolean allowed = ConfigUtils.playerAllowed(player, type).test(regionDefinition);
                    if (!allowed) {
                        ChatUtils.sendErrorMessage(player, "You aren't allowed to interact in this region!");
                        e.setUseInteractedBlock(Event.Result.DENY);
                        e.setUseItemInHand(Event.Result.DENY);
                        e.setCancelled(true);
                    }
                });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(final BlockBreakEvent e) {
        final Player player = e.getPlayer();
        if (player.hasPermission("SimpleRegions.bypass")) {
            return;
        }

        final Vec3D location = new Vec3D(e.getBlock().getLocation());
        storageManager.findRegionByPoint(location)
                .filter(ConfigUtils.playerAllowed(player, InteractionType.BREAK_BLOCK).negate())
                .ifPresent(def -> {
                    ChatUtils.sendErrorMessage(player, "You aren't allowed to break blocks in this region!");
                    e.setCancelled(true);
                });
    }
}
