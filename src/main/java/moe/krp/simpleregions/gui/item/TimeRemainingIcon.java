package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class TimeRemainingIcon extends Icon {
    final RegionDefinition regionDefinition;

    public TimeRemainingIcon(final RegionDefinition regionDefinition) {
        super(Material.CLOCK);
        this.regionDefinition = regionDefinition;
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.GOLD +
                "TIME REMAINING"
        );
        setLore(ChatColor.GRAY + "Time Remaining: " + ChatColor.YELLOW + regionDefinition.getRelatedSign().getDuration());
    }
}
