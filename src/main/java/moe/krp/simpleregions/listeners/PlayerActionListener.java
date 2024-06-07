package moe.krp.simpleregions.listeners;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.gui.BuyRegionGui;
import moe.krp.simpleregions.gui.ManageRegionGui;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerActionListener implements Listener {
    final static StorageManager storageManager = SimpleRegions.getStorageManager();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final BlockState blockState = block.getState();
        if (!(blockState instanceof Sign)) {
            return;
        }

        final Vec3D location = new Vec3D(block.getLocation());
        final RegionDefinition def = storageManager.getRegionDefinitionBySignLocation(location);
        if (def == null) {
            return;
        }
        final SignDefinition signDef = def.getRelatedSign();
        if (signDef == null) {
            return;
        }

        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE) && event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            storageManager.removeSign(def.getName());
            return;
        }

        if (def.getOwnedBy() == null) {
            event.setCancelled(true);
            new BuyRegionGui(
                    event.getPlayer(),
                    BuyRegionGui.getBuyRegionGuiId(event.getPlayer().getName()),
                    def
            ).open();
        }
        else if (def.getOwnedBy().equals(event.getPlayer().getUniqueId())) {
            new ManageRegionGui(
                    event.getPlayer(),
                    ManageRegionGui.getManageRegionGuiId(event.getPlayer().getName()),
                    def
            ).open();
        }
    }
}
