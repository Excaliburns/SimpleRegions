package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.gui.MemberManagementGui;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DeleteMemberIcon extends Icon {
    public DeleteMemberIcon(
            final RegionDefinition regionDefinition,
            final UUID playerUuid
    ) {
        super(Material.BARRIER);
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.RED +
                "DELETE MEMBER"
        );
        this.onClick(event -> {
            SimpleRegions.getStorageManager().removeAllowedPlayer(
                    regionDefinition.getName(),
                    playerUuid
            );
            ChatUtils.sendMessage(
                    event.getWhoClicked(),
                    "Removed player from region."
            );
            new MemberManagementGui(
                    (Player) event.getWhoClicked(),
                    ManageMemberGui.getManageMemberGuiId(event.getWhoClicked().getName()),
                    regionDefinition
            ).open();
        });
    }
}
