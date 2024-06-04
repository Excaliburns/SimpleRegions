package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.helpers.RegionDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

public class BuyRegionIcon extends Icon {
    final RegionDefinition regionDefinition;

    public BuyRegionIcon(final RegionDefinition regionDefinition) {
        super(Material.CLOCK);
        setName(ChatColor.GOLD +
                ">> " +
                ChatColor.BOLD + ChatColor.GOLD +
                "BUY REGION"
        );
        setLore(ChatColor.GRAY +
                "Purchase this region for: \n" +
                (regionDefinition.getRelatedSign().getCost() != 0 ? (
                        ChatColor.GOLD   + "$" +
                        ChatColor.YELLOW + regionDefinition.getRelatedSign().getCost()
                ) : (
                        ChatColor.GREEN + "Free!"
                ))
        );
        this.regionDefinition = regionDefinition;
        this.onClick(event -> {
            event.setCancelled(true);
            final OfflinePlayer player = Bukkit.getOfflinePlayer(event.getWhoClicked().getName());
            if (SimpleRegions.getEconomy().getBalance(player) < regionDefinition.getRelatedSign().getCost()) {
                ChatUtils.sendErrorMessage(player.getPlayer(), "You don't have enough money to buy this region!");
                return;
            }

            ChatUtils.sendMessage(
                    player.getPlayer(),
                    String.format("You have successfully bought the region %s!", regionDefinition.getName())
            );
            SimpleRegions.getEconomy()
                         .withdrawPlayer(player, regionDefinition.getRelatedSign().getCost());
            SimpleRegions.getStorageManager()
                         .setRegionOwned(regionDefinition.getName(), player.getUniqueId());
        });
    }
}