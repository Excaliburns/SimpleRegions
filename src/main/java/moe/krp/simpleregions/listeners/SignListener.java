package moe.krp.simpleregions.listeners;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.util.ConfigUtils;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.util.TimeUtils;
import moe.krp.simpleregions.helpers.Vec3D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.time.Duration;
import java.util.Objects;

public class SignListener implements Listener {
    final private static StorageManager storageManager = SimpleRegions.getStorageManager();

    @EventHandler
    public void onCreateSign(SignChangeEvent e) {
        final Player user = e.getPlayer();

        if (Objects.equals(e.line(0), Component.text("[" + ConfigUtils.getDisplayName() + "]"))) {
            if (!user.hasPermission("SimpleRegions.create")) {
                ChatUtils.sendMessage(user, "You do not have permission to create a region sign");
                e.setCancelled(true);
                return;
            }

            final Vec3D signLocation = new Vec3D(e.getBlock().getLocation());
            final Component regionNameLine = e.line(1);
            final Component costLine = e.line(2);
            final Component timeLimitLine = e.line(3);
            if (regionNameLine == null || costLine == null || timeLimitLine == null) {
                final String errorMessage = "Sign components were null when creating sign for " + e.getPlayer()
                                                                                                   .getName();
                SimpleRegions.log(errorMessage);
                ChatUtils.sendMessage(user, errorMessage);
                return;
            }

            final String regionName = PlainTextComponentSerializer.plainText()
                                                                  .serialize(regionNameLine);
            final String costStr = PlainTextComponentSerializer.plainText()
                                                               .serialize(costLine);
            final String timeLimit = PlainTextComponentSerializer.plainText()
                                                                 .serialize(timeLimitLine);
            if (regionName.isEmpty() || regionName.isBlank()) {
                ChatUtils.sendMessage(user, "Please specify a region name.");
                return;
            }
            if (costStr.isEmpty() || costStr.isBlank()) {
                ChatUtils.sendMessage(user, "Please specify a region cost.");
                return;
            }
            if (timeLimit.isEmpty() || timeLimit.isBlank()) {
                ChatUtils.sendMessage(user, "Please specify a time limit, or \"Unlimited\", if it is unlimited.");
                return;
            }

            final RegionDefinition regionDef = storageManager.getRegion(regionName);
            if (regionDef == null) {
                ChatUtils.sendMessage(user, "Region " + regionName + " does not exist.");
                return;
            }
            if (regionDef.getRelatedSign() != null) {
                ChatUtils.sendMessage(user, "A sign already exists for this region");
                e.setCancelled(true);
                return;
            }
            
            final double cost;
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException ex) {
                ChatUtils.sendMessage(user, "Invalid cost value.");
                return;
            }
            final Duration expireDuration;
            try {
                expireDuration = TimeUtils.getDurationFromTimeString(timeLimit);
            } catch (IllegalArgumentException ex) {
                SimpleRegions.log(ex);
                return;
            }

            final SignDefinition signDef = new SignDefinition(cost, signLocation, TimeUtils.getTimeStringFromDuration(expireDuration));

            final boolean success = SimpleRegions.getStorageManager()
                                                 .addSign(regionName, signDef);

            if (success) {
                ChatUtils.sendMessage(user, "Sign registered for region " + regionName);
                if (e.getBlock().getState() instanceof Sign sign) {
                    resetWorldSign(sign, regionDef, cost);
                }
            }
        }
    }

    public static void resetWorldSign(final Sign e, final RegionDefinition regionDef, final double cost) {
        e.line(0, Component.text(regionDef.getConfiguration()
                                          .getBuySignLineZero())
                           .color(TextColor.fromHexString(regionDef.getConfiguration()
                                                                   .getBuySignLineZeroColor())));
        e.line(1, Component.text(regionDef.getName()));
        e.line(2, Component.empty());
        if (cost == 0) {
            e.line(3, Component.text("Free!")
                               .color(TextColor.color(0x55FF55)));
        }
        else {
            e.line(3, Component.text(ChatColor.GOLD + "$")
                               .color(TextColor.color(0xFFAA00))
                               .append(Component.text(cost)
                                                .color(TextColor.color(0xFFFF55))));
        }

        Bukkit.getScheduler().runTaskLater(SimpleRegions.getInstance(), () -> e.update(), 20L);
    }
}
