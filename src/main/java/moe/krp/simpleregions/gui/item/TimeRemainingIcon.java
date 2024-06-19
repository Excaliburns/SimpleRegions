package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

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
        final List<String> lore = new ArrayList<>();
        if (regionDefinition.getUpkeepTimer() != null) {
            lore.add(ChatColor.GRAY + "Next Upkeep: " + ChatColor.YELLOW + regionDefinition.getUpkeepTimer());
        }
        lore.add(ChatColor.GRAY + "Rental time remaining: " + ChatColor.YELLOW + regionDefinition.getRelatedSign().getDuration());
        setLore(lore);
    }
}
