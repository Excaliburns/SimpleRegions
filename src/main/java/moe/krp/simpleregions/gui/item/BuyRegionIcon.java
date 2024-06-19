package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Icon;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuyRegionIcon extends Icon {
    final RegionDefinition regionDefinition;

    public BuyRegionIcon(final RegionDefinition regionDefinition) {
        super(Material.EMERALD);
        final boolean isInfiniteWithUpkeep = regionDefinition.getUpkeepInterval() != null && regionDefinition.getRelatedSign().isNeverExpire();

        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.GREEN +
                "BUY REGION"
        );
        final List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Purchase this region for:");
        if (regionDefinition.getRelatedSign().getCost() != 0) {
            lore.add(ChatColor.GOLD + "$" + ChatColor.YELLOW + regionDefinition.getRelatedSign().getCost());
        }
        else {
            lore.add(ChatColor.GREEN + "Free!");
        }

        if (isInfiniteWithUpkeep) {
            lore.add(ChatColor.RED + "There is an upkeep for this plot!");
            lore.add(ChatColor.RED + "The upkeep cost is:");
            lore.add(ChatColor.GOLD + "$" + regionDefinition.getUpkeepCost());
            lore.add(ChatColor.RED + "Deducted at each interval:");
            lore.add(ChatColor.GOLD + TimeUtils.getTimeStringFromDuration(regionDefinition.getUpkeepInterval()));
            lore.add(ChatColor.RED + "If you do not have funds when they are deducted your region will become unowned!");
        }

        if (regionDefinition.getConfiguration().getRemoveItemsOnNewOwner()) {
            lore.add(ChatColor.RED + "This region is set to break the blocks inside");
            lore.add(ChatColor.RED + "if it is bought by a new owner.");
        }

        setLore(lore);
        this.regionDefinition = regionDefinition;
        this.onClick(event -> {
            event.setCancelled(true);
            final OfflinePlayer player = Bukkit.getOfflinePlayer(event.getWhoClicked().getName());
            final Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                onlinePlayer.closeInventory();
            }
            final int playerOwnedRegions = SimpleRegions.getStorageManager().getNumberOfOwnedRegionsForPlayer(
                    regionDefinition.getRegionType(),
                    player.getUniqueId()
            );

            if (regionDefinition.getConfiguration().getOwnerLimit() <= playerOwnedRegions) {
                ChatUtils.sendErrorMessage(player.getPlayer(), "You own too many of this type of region!");
                return;
            }


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
                         .setRegionOwned(regionDefinition.getName(), player.getUniqueId(), player.getName());
        });
    }
}
