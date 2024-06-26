package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ChangeOwnerIcon extends Icon {
    public ChangeOwnerIcon(
            final RegionDefinition regionDefinition
    ) {
        super(Material.PAPER);
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.GOLD +
                "CHANGE OWNER"
        );
        this.onClick(event -> {
            final Player clicker = (Player) event.getWhoClicked();
            new AnvilGUI.Builder()
                    .onClick((slot, stateSnapshot) -> {
                        if (slot != AnvilGUI.Slot.OUTPUT) {
                            return Collections.emptyList();
                        }

                        final String playerName = stateSnapshot.getText();
                        if (playerName == null || playerName.isEmpty()) {
                            ChatUtils.sendErrorMessage(stateSnapshot.getPlayer(), "Please enter a player name.");
                            return Collections.emptyList();
                        }
                        if (playerName.equalsIgnoreCase(clicker.getName())) {
                            ChatUtils.sendErrorMessage(stateSnapshot.getPlayer(), "You can't change to yourself!");
                            return Collections.emptyList();
                        }
                        final UUID playerId = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                        final int playerOwnedRegions = SimpleRegions.getStorageManager().getNumberOfOwnedRegionsForPlayer(
                                regionDefinition.getRegionType(),
                                playerId
                        );
                        if (regionDefinition.getConfiguration().getOwnerLimit() - 1 >= playerOwnedRegions) {
                            ChatUtils.sendErrorMessage(stateSnapshot.getPlayer(), "That player can't accept any more cells!");
                            return Collections.emptyList();
                        }

                        return List.of(AnvilGUI.ResponseAction.close(),
                                AnvilGUI.ResponseAction.run(
                                        () -> SimpleRegions.getStorageManager().setRegionOwned(
                                                regionDefinition.getName(),
                                                playerId,
                                                playerName
                                        )));
                    })
                    .text("Rename this!")
                    .title("Enter a player name")
                    .plugin(SimpleRegions.getInstance())
                    .open(clicker);
        });
    }
}
