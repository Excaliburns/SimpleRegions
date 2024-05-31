package moe.krp.simpleregions.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.ConfigUtil;
import moe.krp.simpleregions.util.RegionDefinition;
import moe.krp.simpleregions.visualization.VisualizationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleRegionsCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            return false;
        }

        switch (args[0]) {
            case "create" -> {
                final Player creator = (Player) sender;
                final LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt((Player) sender));
                final Region region;

                if (args.length < 3) {
                    sender.sendMessage("Usage: /simpleregions create <regionName> <regionType>");
                    return true;
                }

                try {
                    region = session.getSelection(session.getSelectionWorld());
                } catch (IncompleteRegionException e) {
                    throw new RuntimeException(e);
                }

                if (region.getWorld() == null) {
                    sender.sendMessage("Region world cannot be null");
                    return true;
                }

                return handleCreateRegion(creator, args[1], region, args[2], region.getWorld().getName());
            }
            case "delete" -> handleDeleteRegion(sender, args[1]);
            case "info" -> handleRegionInfo(sender, args[1]);
            case "visualize" -> handleRegionVisualization(sender, args[1]);
            case "setOwner" -> handleRegionSetOwner(sender, args[1], args[2]);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "info", "visualize", "setOwner");
        }

        if (args.length == 2 && args[0].equals("create")) {
            return Collections.emptyList();
        }
        else if (args.length == 3 && args[0].equals("create")) {
            return ConfigUtil.getRegionTypes()
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

        return Collections.emptyList();
    }

    private boolean handleRegionInfo(final CommandSender sender, final String regionName) {
        final RegionDefinition def = SimpleRegions.getStorageManager().getRegion(regionName);
        if (def == null) {
            sender.sendMessage("region not found");
            return false;
        }

        sender.sendMessage(def.toString());

        return true;
    }

    private boolean handleDeleteRegion(
            final CommandSender sender,
            final String regionName
    ) {
        final RegionDefinition def = SimpleRegions.getStorageManager().getRegion(regionName);
        if (def == null) {
            sender.sendMessage("region not found");
            return false;
        }

        SimpleRegions.getStorageManager().markRegionForDelete(regionName);
        sender.sendMessage(String.format("region %s deleted", regionName));

        return true;
    }

    private boolean handleCreateRegion(
            final Player creator,
            final String regionName,
            final Region region,
            final String regionType,
            final String worldName
    ) {
        final RegionDefinition def = SimpleRegions.getStorageManager().getRegion(regionName);
        if (def != null) {
            creator.sendMessage("Region already exists");
            return false;
        }

        if (!ConfigUtil.getRegionTypes().contains(regionType)) {
            creator.sendMessage("Invalid region type");
            return false;
        }

        final boolean addRegion = SimpleRegions.getStorageManager().addRegion(
                regionName, worldName, creator.getUniqueId(), regionType, region
        );

        if (addRegion) {
            creator.sendMessage(String.format("region %s created", regionName));
        }

        return addRegion;
    }

    private boolean handleRegionVisualization(final CommandSender sender, final String regionName) {
        final RegionDefinition def = SimpleRegions.getStorageManager().getRegion(regionName);
        if (def == null) {
            sender.sendMessage("region not found");
            return false;
        }

        final Player player = (Player) sender;
        // manager handles validation
        VisualizationManager.displayVisualizationForregion(player, def);

        return true;
    }

    private boolean handleRegionSetOwner(final CommandSender sender, final String regionName, final String playerName) {
        final RegionDefinition def = SimpleRegions.getStorageManager().getRegion(regionName);
        if (def == null) {
            sender.sendMessage("Region not found");
            return false;
        }

        final Player player = SimpleRegions.getInstance().getServer().getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Player not found");
            return false;
        }

        SimpleRegions.getStorageManager().setRegionOwned(regionName, player.getUniqueId());

        sender.sendMessage(String.format("Region %s is now owned by %s", regionName, playerName));

        return true;
    }
}
