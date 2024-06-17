package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ClearOwnerIcon extends Icon {
    public ClearOwnerIcon(
            final RegionDefinition regionDefinition,
            final Player player
    ) {
        super(Material.BARRIER);
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.RED +
                "RELINQUISH OWNERSHIP"
        );
        this.onClick(event -> {
            SimpleRegions.getStorageManager().resetOwnership(
                    regionDefinition.getName()
            );
            ChatUtils.sendMessage(
                    event.getWhoClicked(),
                    "Deleted ownership from region."
            );
            player.closeInventory();
        });
    }
}
