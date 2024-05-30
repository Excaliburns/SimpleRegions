package moe.krp.simplecells.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simplecells.SimpleCells;
import moe.krp.simplecells.util.CellDefinition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CellCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            return false;
        }
        final LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt((Player) sender));
        final Region region;

        try {
            region = session.getSelection(session.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            throw new RuntimeException(e);
        }

        switch (args[0]) {
            case "create" -> SimpleCells.getStorageManager()
                                        .addCell(args[1], region);
            case "delete" -> SimpleCells.getStorageManager()
                                        .markCellForDelete(args[1]);
            case "info" -> {
                CellDefinition def = SimpleCells.getStorageManager()
                                                .getCell(args[1]);
                if (def == null) {
                    sender.sendMessage("Cell not found");
                    return true;
                }

                sender.sendMessage(def.toString());
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("create", "delete", "info");
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

        return Collections.emptyList();
    }

    private boolean handleCreateCell(
            final CommandSender sender,
            final String cellName,
            final
    ) {

    }
}
