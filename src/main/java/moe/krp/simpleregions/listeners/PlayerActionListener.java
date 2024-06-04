package moe.krp.simpleregions.listeners;

import mc.obliviate.inventory.Gui;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.gui.BuyRegionGui;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerActionListener {
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
        final RegionDefinition def = storageManager.getRegionDefinitionByLocation(location);
        if (def == null) {
            return;
        }
        final SignDefinition signDef = def.getRelatedSign();
        if (signDef == null) {
            return;
        }

        event.setCancelled(true);
        final Gui gui = new BuyRegionGui(
                event.getPlayer(), "buy_region_gui_"+event.getPlayer().getName().toLowerCase(),
                def
        );
        gui.open();
    }
}
