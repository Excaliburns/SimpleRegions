package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class ConfirmDeleteIcon extends Icon {
    public ConfirmDeleteIcon(
            final RegionDefinition regionDefinition
    ) {
        super(Material.RED_STAINED_GLASS);
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.RED +
                "ARE YOU SURE YOU WANT TO RELINQUISH OWNERSHIP?"
        );
        this.onClick(event -> {
            SimpleRegions.getStorageManager().resetOwnership(
                    regionDefinition.getName()
            );
            ChatUtils.sendMessage(
                    event.getWhoClicked(),
                    "Deleted ownership from region."
            );
            event.getWhoClicked().closeInventory();
        });
    }
}
