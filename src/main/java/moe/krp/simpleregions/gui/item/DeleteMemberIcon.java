package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DeleteMemberIcon extends Icon {
    final RegionDefinition regionDefinition;

    public DeleteMemberIcon(
            final RegionDefinition regionDefinition
    ) {
        super(Material.BARRIER);
        this.regionDefinition = regionDefinition;
    }
}
