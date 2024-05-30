package moe.krp.simplecells.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simplecells.SimpleCells;
import moe.krp.simplecells.util.CellDefinition;
import moe.krp.simplecells.visualization.VisualizationManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleCellsCommand implements TabExecutor {
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

                try {
                    region = session.getSelection(session.getSelectionWorld());
                } catch (IncompleteRegionException e) {
                    throw new RuntimeException(e);
                }

                if (region.getWorld() == null) {
                    sender.sendMessage("Region world cannot be null");
                    return true;
                }

                handleCreateCell(creator, args[1], region, region.getWorld().getName());
            }
            case "delete" -> handleDeleteCell(sender, args[1]);
            case "info" -> handleCellInfo(sender, args[1]);
            case "visualize" -> handleCellVisualization(sender, args[1]);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "info", "visualize");
        }

        if (args.length == 2 && args[0].equals("create")) {
            return Collections.emptyList();
        }
        else if (args.length == 2 && args[0].equals("delete")) {
            return SimpleCells.getStorageManager().getCellNames()
                    .stream().filter(cellName -> cellName.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("info")) {
            return SimpleCells.getStorageManager().getCellNames()
                              .stream().filter(cellName -> cellName.startsWith(args[1]))
                              .collect(Collectors.toList());
        }
        else if (args.length == 2 && args[0].equals("visualize")) {
            return SimpleCells.getStorageManager().getCellNames()
                              .stream().filter(cellName -> cellName.startsWith(args[1]))
                              .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private boolean handleCellInfo(final CommandSender sender, final String cellName) {
        final CellDefinition def = SimpleCells.getStorageManager().getCell(cellName);
        if (def == null) {
            sender.sendMessage("Cell not found");
            return false;
        }

        sender.sendMessage(def.toString());

        return true;
    }

    private boolean handleDeleteCell(
            final CommandSender sender,
            final String cellName
    ) {
        final CellDefinition def = SimpleCells.getStorageManager().getCell(cellName);
        if (def == null) {
            sender.sendMessage("Cell not found");
            return false;
        }

        final boolean markCellForDelete = SimpleCells.getStorageManager().markCellForDelete(cellName);
        if (markCellForDelete) {
            sender.sendMessage(String.format("Cell %s deleted", cellName));
        }

        return markCellForDelete;
    }

    private boolean handleCreateCell(
            final Player creator,
            final String cellName,
            final Region region,
            final String worldName
    ) {
        final CellDefinition def = SimpleCells.getStorageManager().getCell(cellName);
        if (def != null) {
            creator.sendMessage("Cell already exists");
            return false;
        }
        final boolean addCell = SimpleCells.getStorageManager().addCell(cellName, region, worldName, creator.getUniqueId());

        if (addCell) {
            creator.sendMessage(String.format("Cell %s created", cellName));
        }

        return addCell;
    }

    private boolean handleCellVisualization(final CommandSender sender, final String cellName) {
        final CellDefinition def = SimpleCells.getStorageManager().getCell(cellName);
        if (def == null) {
            sender.sendMessage("Cell not found");
            return false;
        }

        final Player player = (Player) sender;
        // manager handles validation
        VisualizationManager.displayVisualizationForCell(player, def);

        return true;
    }
}
