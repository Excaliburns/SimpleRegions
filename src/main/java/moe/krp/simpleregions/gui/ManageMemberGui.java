package moe.krp.simpleregions.gui;

import mc.obliviate.inventory.Gui;
import moe.krp.simpleregions.gui.item.DeleteMemberIcon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.UUID;

public class ManageMemberGui extends Gui {
    private final UUID managingPlayer;
    private final RegionDefinition regionDefinition;

    public ManageMemberGui(
            final Player player,
            final String id,
            final RegionDefinition regionDefinition,
            final UUID managingPlayer
    ) {
        super (player, id, "Manage " + player.getName(), 3);
        this.regionDefinition = regionDefinition;
        this.managingPlayer = managingPlayer;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        fillGui(Material.BLACK_STAINED_GLASS);
        addItem(4, new DeleteMemberIcon(regionDefinition, managingPlayer));
    }

    public static String getManageMemberGuiId(String playerName) {
        return "manage_member_gui_" + playerName;
    }
}
