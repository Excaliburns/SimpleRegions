package moe.krp.simpleregions.gui;

import mc.obliviate.inventory.Gui;
import moe.krp.simpleregions.gui.item.AddMemberIcon;
import moe.krp.simpleregions.gui.item.AdditionalOwnerIcon;
import moe.krp.simpleregions.gui.item.DeleteMemberIcon;
import moe.krp.simpleregions.gui.item.GoBackIcon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.util.UUID;
import java.util.function.Consumer;

public class MemberManagementGui extends Gui {
    final RegionDefinition regionDefinition;

    public MemberManagementGui(
            Player player,
            String id,
            RegionDefinition regionDefinition
    ) {
        super(player, id, "Manage Members", 4);
        this.regionDefinition = regionDefinition;
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        int slots = 0;
        for (final UUID otherPlayer : regionDefinition.getOtherAllowedPlayers()) {
            if (slots > 26) break;
            addItem(slots, new AdditionalOwnerIcon(
                    ItemUtils.getSkullForPlayerUuid(otherPlayer, null),
                    regionDefinition
            ));
            slots++;
        }

        final Consumer<InventoryClickEvent> onBack =
                (e) -> new ManageRegionGui(player, ManageRegionGui.getManageRegionGuiId(player.getName()), regionDefinition).open();

        addItem(27, new GoBackIcon(onBack));
        addItem(31, new AddMemberIcon(regionDefinition));
    }

    public static String getMemberManagementGuiId(String playerName) {
        return "member_management_gui_" + playerName;
    }
}
