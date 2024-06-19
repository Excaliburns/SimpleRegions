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
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

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

        ChatUtils.sendMessage(e.getPlayer(), "Region ownership reset for region " + definition.getName());
        storageManager.resetOwnership(definition.getName());
        storageManager.removeSign(definition.getName());
    }

    @EventHandler
    public void onPlaceSign(BlockPlaceEvent e) {
        final Player user = e.getPlayer();
        if (!(e.getBlockPlaced().getState() instanceof Sign sign)) {
            return;
        }
        if (!validateSign(sign.line(0), user)) {
            return;
        }

        registerNewSign(new Vec3D(e.getBlock().getLocation()), sign.lines(), user);
    }

    @EventHandler
    public void onCreateSign(SignChangeEvent e) {
        final Player user = e.getPlayer();
        if (!validateSign(e.line(0), user)) {
            return;
        }

        registerNewSign(new Vec3D(e.getBlock().getLocation()), e.lines(), user);
    }

    public static void resetWorldSign(final Vec3D signLocation, final RegionDefinition regionDefinition, final double cost) {
        final World world = Bukkit.getServer()
                                  .getWorld(signLocation.getWorld());
        if (world == null) {
            return;
        }
        final Sign sign = (Sign) world.getBlockAt(signLocation.toLocation()).getState();

        resetWorldSign(
                sign,
                regionDefinition,
                cost
        );
    }

    public static void resetWorldSign(final Sign sign, final RegionDefinition regionDef, final double cost) {
        try {
            sign.line(0, Component.text(regionDef.getConfiguration().getBuySignLineZero())
                                        .color(TextColor.fromHexString(regionDef.getConfiguration().getBuySignLineZeroColor())));
        } catch (NullPointerException e) {
            SimpleRegions.log(Level.SEVERE, "Did you set a buy-sign-line-zero for your Region type?");
            SimpleRegions.log(e);
        }
        sign.line(1, Component.text(regionDef.getName()));
        sign.line(2, Component.empty());
        if (cost == 0) {
            sign.line(3, Component.text("Free!")
                               .color(TextColor.color(0x55FF55)));
        }
        else {
            sign.line(3, Component.text(ChatColor.GOLD + "$")
                               .color(TextColor.color(0xFFAA00))
                               .append(Component.text(new DecimalFormat("#0.00").format(cost))
                                                .color(TextColor.color(0xFFFF55))));
        }

        Bukkit.getScheduler().runTaskLater(SimpleRegions.getInstance(), () -> sign.update(), 20L);
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

    private SignDefinition registerNewSign(
            final Vec3D signLocation,
            final List<Component> lines,
            final Player user
    ) {
        final Component regionNameLine = lines.get(1);
        final Component costLine = lines.get(2);
        final Component timeLimitLine = lines.get(3);

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
            ChatUtils.sendMessage(user, "Please specify a time limit, or \"infinite\", if it is unlimited.");
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

                    if (!timeLimit.equalsIgnoreCase("infinite")) {
                        try {
                            TimeUtils.getDurationFromTimeString(timeLimit);
                            signDef.initDuration(timeLimit);
                        } catch (IllegalArgumentException ex) {
                            ChatUtils.sendMessage(user, "Invalid duration value.");
                            return;
                        }
                    }
                    else {
                        signDef.initInfiniteDuration();
                    }

                    signDef.setLocation(signLocation);
                    signDef.setRegionName(regionName);

                    if (!signDef.isValid()) {
                        ChatUtils.sendMessage(user, "Sign invalid for region " + signDef.getRegionName());
                        return;
                    }

                    storageManager.addSign(regionName, signDef);

                    ChatUtils.sendMessage(user, "Sign registered for region " + signDef.getRegionName());
                    resetWorldSign(signLocation, regionDef, cost);
                }, () -> ChatUtils.sendMessage(user, "Region " + regionName + " does not exist."));

        return signDef;
    }
}
