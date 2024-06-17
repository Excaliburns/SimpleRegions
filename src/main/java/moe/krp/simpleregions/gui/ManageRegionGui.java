package moe.krp.simpleregions.gui;

import mc.obliviate.inventory.Gui;
import moe.krp.simpleregions.gui.item.ChangeOwnerIcon;
import moe.krp.simpleregions.gui.item.ClearOwnerIcon;
import moe.krp.simpleregions.gui.item.MemberManagementIcon;
import moe.krp.simpleregions.gui.item.TimeRemainingIcon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ManageRegionGui extends Gui {
    final RegionDefinition regionDefinition;

    public ManageRegionGui(Player player, String id, RegionDefinition regionDefinition) {
        super(player, id, "Manage " + regionDefinition.getRegionType(), 3);
        this.regionDefinition = regionDefinition;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        addItem(10, new TimeRemainingIcon(regionDefinition));
        addItem(12, new ClearOwnerIcon(regionDefinition, player));
        addItem(14, new ChangeOwnerIcon(regionDefinition));
        addItem(16, new MemberManagementIcon(player, regionDefinition));
    }

    public static String getManageRegionGuiId(String playerName) {
        return "manage_region_gui_" + playerName;
    }
}
