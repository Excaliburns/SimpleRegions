package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.gui.MemberManagementGui;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MemberManagementIcon extends Icon {
    final RegionDefinition regionDefinition;

    public MemberManagementIcon(
            final Player clicker,
            final RegionDefinition regionDefinition
    ) {
        super(Material.VILLAGER_SPAWN_EGG);
        this.regionDefinition = regionDefinition;
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.YELLOW +
                "MANAGE MEMBERS"
        );
        setLore(ChatColor.GRAY + "Manage members of the region.");
        final Gui gui = new MemberManagementGui(
                clicker,
                MemberManagementGui.getMemberManagementGuiId(clicker.getName()),
                regionDefinition
        );
        this.onClick( event -> gui.open() );
    }
}
