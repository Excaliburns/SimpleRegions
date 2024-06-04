package moe.krp.simpleregions.listeners;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.util.ConfigUtil;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.util.TimeUtils;
import moe.krp.simpleregions.helpers.Vec3D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
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

        if (Objects.equals(e.line(0), Component.text("[" + ConfigUtil.getDisplayName() + "]"))) {
            if (!user.hasPermission("SimpleRegions.create")) {
                user.sendMessage("You do not have permission to create a region sign");
                e.setCancelled(true);
                return;
            }

            final Vec3D signLocation = new Vec3D(e.getBlock().getLocation());
            if (SimpleRegions.getStorageManager().getRegionDefinitionByLocation(signLocation) != null) {
                user.sendMessage("A region already exists at this location");
                e.setCancelled(true);
                return;
            }
            final Component regionNameLine = e.line(1);
            final Component costLine = e.line(2);
            final Component timeLimitLine = e.line(3);
            if (regionNameLine == null || costLine == null || timeLimitLine == null) {
                SimpleRegions.log("Sign components were null when creating sign for " + e.getPlayer().getName());
                return;
            }

            final String regionName  = PlainTextComponentSerializer.plainText()
                                                                   .serialize(regionNameLine);
            final String costStr   = PlainTextComponentSerializer.plainText()
                                                                 .serialize(costLine);
            final String timeLimit = PlainTextComponentSerializer.plainText()
                                                                 .serialize(timeLimitLine);
            if (regionName.isEmpty() || regionName.isBlank()) {
                user.sendMessage("Please specify a region name.");
                return;
            }
            if (costStr.isEmpty() || costStr.isBlank()) {
                user.sendMessage("Please specify a region cost.");
                return;
            }
            if (timeLimit.isEmpty() || timeLimit.isBlank()) {
                user.sendMessage("Please specify a time limit, or \"Unlimited\", if it is unlimited.");
                return;
            }

            final RegionDefinition regionDef = storageManager.getRegion(regionName);
            if (regionDef == null) {
                user.sendMessage("Region " + regionName + " does not exist.");
                return;
            }
            final double cost;
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException ex) {
                user.sendMessage("Invalid cost value.");
                return;
            }
            final Duration expireDuration;
            try {
                expireDuration = TimeUtils.getDurationFromTimeString(timeLimit);
            } catch (IllegalArgumentException ex) {
                user.sendMessage(ex.getMessage());
                return;
            }

            final SignDefinition signDef = new SignDefinition(
                    cost,
                    signLocation,
                    TimeUtils.getTimeStringFromDuration(expireDuration)
            );

            final boolean success = SimpleRegions.getStorageManager().addSign(regionName, signDef);
            if (success) {
                ChatUtils.sendMessage(user, "Sign registered for region " + regionName);
                e.line(0, Component.text(regionDef.getConfiguration().getBuySignLineZero())
                                   .color(TextColor.fromHexString(regionDef.getConfiguration().getBuySignLineZeroColor())));
                e.line(1, Component.text(regionDef.getName()));
                e.line(2, Component.empty());
                if (cost == 0) {
                    e.line(3, Component.text("Free!").color(TextColor.color(0x55FF55)));
                }
                else {
                    e.line(3, Component.text(ChatColor.GOLD + "$").color(TextColor.color(0xFFAA00)).append(
                            Component.text(cost).color(TextColor.color(0xFFFF55))
                    ));
                }
            }
        }
    }
}
