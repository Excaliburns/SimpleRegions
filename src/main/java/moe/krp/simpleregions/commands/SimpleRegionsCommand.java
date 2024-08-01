package moe.krp.simpleregions.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.Vec3D;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.util.ConfigUtils;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.visualization.VisualizationManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.Option;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SimpleRegionsCommand implements TabExecutor {
    private static final List<String> COMMAND_STRING_LIST = List.of(
            "create",
            "delete",
            "info",
            "visualize",
            "setOwner",
            "clearOwner",
            "setType",
            "setOriginalDuration",
            "setTimeRemaining",
            "setUpkeepTimeRemaining",
            "redefine"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            return false;
        }

        switch (args[0]) {
            case "create" -> handleCreateRegion(sender, args[1], args[2]);
            case "delete" -> handleDeleteRegion(sender, args[1]);
            case "info" -> handleRegionInfo(sender, args[1]);
            case "visualize" -> handleRegionVisualization(sender, args[1]);
            case "setOwner" -> handleRegionSetOwner(sender, args[1], args[2]);
            case "clearOwner" -> handleRegionClearOwner(sender, args[1]);
            case "setType" -> handleRegionSetType(sender, args[1], args[2]);
            case "setOriginalDuration" -> handleOriginalDuration(sender, args[1], args[2]);
            case "setUpkeepTimeRemaining" -> handleSetUpkeepTimeRemaining(sender, args[1], args[2]);
            case "setTimeRemaining" -> handleSetTimeRemaining(sender, args[1], args[2]);
            case "redefine" -> handleRedefine(sender, args[1]);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return COMMAND_STRING_LIST.stream().filter( it -> it.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equals("create")) {
            return Collections.emptyList();
        }
        else if (args.length == 3 && args[0].equals("create")) {
            return ConfigUtils.getRegionTypes()
                              .stream().filter(regionType -> regionType.startsWith(args[2]))
                              .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("delete")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("info")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                              .stream().filter(regionName -> regionName.startsWith(args[1]))
                              .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("visualize")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                              .stream().filter(regionName -> regionName.startsWith(args[1]))
                              .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("clearOwner")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("setType")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 3 && args[0].equals("setType")) {
            return ConfigUtils.getRegionTypes()
                                .stream().filter(type -> type.startsWith(args[2]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("setOwner")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                    .stream().filter(regionName -> regionName.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        else if (args.length == 3 && args[0].equals("setOwner")) {
            return SimpleRegions.getInstance().getServer().getOnlinePlayers()
                    .stream().map(Player::getName)
                    .filter(name -> name.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("setOriginalDuration")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("setUpkeepTimeRemaining")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("setTimeRemaining")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("redefine")) {
            return SimpleRegions.getStorageManager().getRegionNames()
                                .stream().filter(regionName -> regionName.startsWith(args[1]))
                                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void handleRegionInfo(final CommandSender sender, final String regionName) {
        SimpleRegions.getStorageManager().getRegionByName(regionName)
                     .ifPresentOrElse(def -> def.getFormattedChatInformation().forEach(sender::sendMessage),
                             () -> ChatUtils.sendErrorMessage(sender, "Region not found"));
    }

    private void handleDeleteRegion(
            final CommandSender sender,
            final String regionName
    ) {
        if (!sender.hasPermission("SimpleRegions.delete")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to delete regions");
            return;
        }

        SimpleRegions.getStorageManager().getRegionByName(regionName)
                .ifPresentOrElse(def -> {
                    SimpleRegions.getStorageManager().markRegionForDelete(regionName);
                    ChatUtils.sendMessage(sender, String.format("Region %s deleted", regionName));
                }, () -> ChatUtils.sendErrorMessage(sender, "Region not found"));
    }

    private void handleCreateRegion(
            final CommandSender sender,
            final String regionName,
            final String regionType
    ) {
        if (!(sender instanceof Player creator)) {
            ChatUtils.sendErrorMessage(sender, "You must be a player to use this command.");
            return;
        }

        if (!creator.hasPermission("SimpleRegions.create")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to create regions");
            return;
        }

        getPlayerWorldEditRegion(creator)
                .ifPresent(region -> SimpleRegions.getStorageManager().getRegionByName(regionName)
                                                  .ifPresentOrElse( def -> ChatUtils.sendErrorMessage(creator, "Region " + regionName + " already exists"),
                                     () -> {
                                         if (!ConfigUtils.getRegionTypes().contains(regionType)) {
                                             ChatUtils.sendErrorMessage(creator, "Invalid region type");
                                             return;
                                         }

                                         // getPlayerWorldEditRegion null-checks region.getWorld()
                                         @SuppressWarnings("DataFlowIssue")
                                         final boolean addRegion = SimpleRegions.getStorageManager().addRegion(
                                                 regionName, region.getWorld().getName(), creator.getUniqueId(), regionType, region
                                         );

                                         if (addRegion) {
                                             ChatUtils.sendMessage(creator, String.format("Region %s created", regionName));
                                         }
                                     }));
    }

    private void handleRegionVisualization(final CommandSender sender, final String regionName) {
        if (!sender.hasPermission("SimpleRegions.visualize")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to visualize regions");
            return;
        }

        SimpleRegions.getStorageManager().getRegionByName(regionName).ifPresentOrElse(
                def -> {
                    final Player player = (Player) sender;
                    // manager handles validation
                    VisualizationManager.displayVisualizationForRegion(player, def);
                }, () -> ChatUtils.sendErrorMessage(sender, "Region not found.")
        );
    }

    private void handleRegionSetType(final CommandSender sender, final String regionName, final String regionType) {
        if (!sender.hasPermission("SimpleRegions.setType")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to set region types");
            return;
        }
        if (!ConfigUtils.getRegionTypes().contains(regionType)) {
            ChatUtils.sendErrorMessage(sender, "That region type does not exist!");
            return;
        }
        if (!SimpleRegions.getStorageManager().getRegionNames()
                         .contains(regionName)) {
            ChatUtils.sendErrorMessage(sender, "That region does not exist!");
            return;
        }

        SimpleRegions.getStorageManager().setType(regionName, regionType);
        ChatUtils.sendMessage(sender, "Set type to " + regionType);
    }

    private void handleRegionClearOwner(final CommandSender sender, final String regionName) {
        if (!sender.hasPermission("SimpleRegions.clearOwner")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to clear region owners");
            return;
        }

        SimpleRegions.getStorageManager().resetOwnership(regionName);
        ChatUtils.sendMessage(sender, "Cleared owner of " + regionName);
    }

    private void handleOriginalDuration(final CommandSender sender, final String regionName, final String originalDuration) {
        if (!sender.hasPermission("SimpleRegions.setOriginalDuration")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to set the upkeep time on a region");
            return;
        }

        SimpleRegions.getStorageManager().setOriginalDuration(regionName, originalDuration, sender);
    }

    private void handleSetUpkeepTimeRemaining(final CommandSender sender, final String regionName, final String timeRemaining) {
        if (!sender.hasPermission("SimpleRegions.setUpkeepTimeRemaining")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to set the upkeep time on a region");
            return;
        }

        SimpleRegions.getStorageManager().setTimeRemaining(regionName, timeRemaining, true, sender);
    }

    private void handleSetTimeRemaining(final CommandSender sender, final String regionName, final String timeRemaining) {
        if (!sender.hasPermission("SimpleRegions.setTimeRemaining")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to set the time on a region");
            return;
        }

        SimpleRegions.getStorageManager().setTimeRemaining(regionName, timeRemaining, false, sender);
    }

    private void handleRegionSetOwner(final CommandSender sender, final String regionName, final String playerName) {
        if (!sender.hasPermission("SimpleRegions.setOwner")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to set region owners");
            return;
        }
        SimpleRegions.getStorageManager().getRegionByName(regionName).ifPresentOrElse(
                def -> {
                    final OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                    SimpleRegions.getStorageManager().setRegionOwned(regionName, player.getUniqueId(), player.getName());
                    ChatUtils.sendMessage(sender, String.format("Region %s is now owned by %s", regionName, playerName));
                }, () -> ChatUtils.sendErrorMessage(sender, "Region not found.")
        );
    }

    private void handleRedefine(final CommandSender sender, final String regionName) {
        if (!(sender instanceof Player creator)) {
            ChatUtils.sendErrorMessage(sender, "You must be a player to use this command.");
            return;
        }

        if (!creator.hasPermission("SimpleRegions.redefine")) {
            ChatUtils.sendErrorMessage(sender, "You don't have permission to redefine regions");
            return;
        }

        getPlayerWorldEditRegion(creator)
                .ifPresent(region -> {
                    //getPlayerWorldEditRegion null-checks region.getWorld()
                    //noinspection DataFlowIssue
                    SimpleRegions.getStorageManager().changeRegionBounds(
                            regionName,
                            new Vec3D(region.getMinimumPoint(), region.getWorld().getName()),
                            new Vec3D(region.getMaximumPoint(), region.getWorld().getName()),
                            sender
                    );
                });
    }

    private Optional<Region> getPlayerWorldEditRegion(final Player player) {
        final LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
        final Region region;
        try {
            region = session.getSelection(session.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            ChatUtils.sendErrorMessage(player, "Please make a WorldEdit selection!");
            return Optional.empty();
        }

        if (region.getWorld() == null) {
            ChatUtils.sendMessage(player, "Region world cannot be null");
            return Optional.empty();
        }

        return Optional.of(region);
    }
}
