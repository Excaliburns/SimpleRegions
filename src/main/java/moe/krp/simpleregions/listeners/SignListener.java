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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;

public class SignListener implements Listener {
    final private static StorageManager storageManager = SimpleRegions.getStorageManager();

    @EventHandler
    public void onBreakSign(BlockBreakEvent e) {
        if (!(e.getBlock().getState() instanceof Sign)) {
            return;
        }
        final RegionDefinition definition = storageManager.getRegionDefinitionBySignLocation(e.getBlock().getLocation());
        if (definition == null) {
            return;
        }

        storageManager.removeSign(definition.getName());
    }

    @EventHandler
    public void onPlaceSign(BlockPlaceEvent e) {
        final Player user = e.getPlayer();
        if (!(e.getBlockPlaced().getState() instanceof Sign sign)) {
            return;
        }
        if (!validateSign(sign.line(0), user)) {
            e.setCancelled(true);
            return;
        }

        registerNewSign(sign, user);
    }

    @EventHandler
    public void onCreateSign(SignChangeEvent e) {
        final Player user = e.getPlayer();
        final Sign sign = (Sign) e.getBlock().getState();
        if (!validateSign(e.line(0), user)) {
            e.setCancelled(true);
            return;
        }

        registerNewSign(sign, user);
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

    private boolean validateSign(final Component line0, final Player user) {
        if (!Objects.equals(line0, Component.text("[" + ConfigUtils.getDisplayName() + "]"))) {
            return false;
        }

        if (!user.hasPermission("SimpleRegions.create")) {
            ChatUtils.sendMessage(user, "You do not have permission to create a region sign");
            return false;
        }

        return true;
    }

    private SignDefinition registerNewSign(final Sign sign, final Player user) {
        final Vec3D signLocation = new Vec3D(sign.getLocation());
        final Component regionNameLine = sign.line(1);
        final Component costLine = sign.line(2);
        final Component timeLimitLine = sign.line(3);

        final String regionName = PlainTextComponentSerializer.plainText()
                                                              .serialize(regionNameLine);
        final String costStr = PlainTextComponentSerializer.plainText()
                                                           .serialize(costLine);
        final String timeLimit = PlainTextComponentSerializer.plainText()
                                                             .serialize(timeLimitLine);
        if (regionName.isEmpty() || regionName.isBlank()) {
            ChatUtils.sendMessage(user, "Please specify a region name.");
            return null;
        }
        if (costStr.isEmpty() || costStr.isBlank()) {
            ChatUtils.sendMessage(user, "Please specify a region cost.");
            return null;
        }
        if (timeLimit.isEmpty() || timeLimit.isBlank()) {
            ChatUtils.sendMessage(user, "Please specify a time limit, or \"Unlimited\", if it is unlimited.");
            return null;
        }

        final SignDefinition signDef = new SignDefinition();
        storageManager.getRegionByName(regionName)
                .ifPresentOrElse(regionDef -> {
                    if (regionDef.getRelatedSign() != null) {
                        ChatUtils.sendMessage(user, "A sign already exists for this region");
                        return;
                    }

                    final double cost;
                    try {
                        cost = Double.parseDouble(costStr);
                        signDef.setCost(cost);
                    } catch (NumberFormatException ex) {
                        ChatUtils.sendMessage(user, "Invalid cost value.");
                        return;
                    }

                    try {
                        TimeUtils.getDurationFromTimeString(timeLimit);
                        signDef.initDuration(timeLimit);
                    } catch (IllegalArgumentException ex) {
                        ChatUtils.sendMessage(user, "Invalid duration value.");
                        return;
                    }

                    signDef.setLocation(signLocation);
                    signDef.setRegionName(regionName);

                    if (!signDef.isValid()) {
                        ChatUtils.sendMessage(user, "Sign invalid for region " + signDef.getRegionName());
                        return;
                    }

                    storageManager.addSign(regionName, signDef);

                    ChatUtils.sendMessage(user, "Sign registered for region " + signDef.getRegionName());
                    resetWorldSign(sign, regionDef, cost);
                }, () -> ChatUtils.sendMessage(user, "Region " + regionName + " does not exist."));

        return signDef;
    }
}
