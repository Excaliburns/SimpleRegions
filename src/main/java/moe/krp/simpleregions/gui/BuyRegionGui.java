package moe.krp.simpleregions.gui;

import mc.obliviate.inventory.Gui;
import moe.krp.simpleregions.gui.item.BuyRegionIcon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class BuyRegionGui extends Gui {
    final RegionDefinition regionDefinition;

    public BuyRegionGui(Player player, String id, RegionDefinition regionDefinition) {
        super(player, id, "Purchase " + regionDefinition.getRegionType(), 3);
        this.regionDefinition = regionDefinition;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        addItem(13, new BuyRegionIcon(regionDefinition));
    }

    public static String getBuyRegionGuiId(String playerName) {
        return "buy_region_gui_" + playerName;
    }
}
