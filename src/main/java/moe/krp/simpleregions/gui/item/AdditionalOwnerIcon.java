package moe.krp.simpleregions.gui.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class AdditionalOwnerIcon extends Icon {
    public AdditionalOwnerIcon(
            final ItemStack playerHead,
            final RegionDefinition regionDefinition
    ) {
        super(playerHead);
        final PlayerProfile player = ((SkullMeta) playerHead.getItemMeta()).getPlayerProfile();
        if (player != null) {
            final UUID headUuid = player.getId();
            this.onClick( event -> {
                new ManageMemberGui(
                        (Player) event.getWhoClicked(),
                        ManageMemberGui.getManageMemberGuiId(event.getWhoClicked().getName()),
                        regionDefinition,
                        headUuid
                ).open();
            });
        }
        else {
            this.onClick( event -> {
                ChatUtils.sendMessage(
                        event.getWhoClicked(),
                        "This player is not a valid player."
                );
            });
        }
    }
}
