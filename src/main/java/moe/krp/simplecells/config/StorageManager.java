package moe.krp.simplecells.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simplecells.SimpleCells;
import moe.krp.simplecells.util.CellDefinition;
import moe.krp.simplecells.util.Vec3D;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StorageManager {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final Set<CellDefinition> allCells;
    private final ConcurrentHashMap<CellDefinition, UUID> locationToPlayerOwnerMap;
    private final ConcurrentHashMap<Vec3D, CellDefinition> signLocationMap;

    public StorageManager() {
        allCells = new HashSet<>();
        locationToPlayerOwnerMap = new ConcurrentHashMap<>();
        signLocationMap = new ConcurrentHashMap<>();
    }

    public CellDefinition getCell(String name) {
        return allCells
                .stream()
                .filter(cell -> cell.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean addCell(String name, Region region, String worldName, UUID creator) {
        synchronized (allCells) {
            allCells.add(
                    new CellDefinition(
                            name,
                            new Vec3D(region.getMinimumPoint(), worldName),
                            new Vec3D(region.getMaximumPoint(), worldName),
                            creator
                    )
            );
        }

        return true;
    }

    public boolean markCellForDelete(final String name) {
        final CellDefinition cell = getCell(name);
        if (cell == null) {
            return false;
        }

        cell.setMarkedForDeletion(true);
        cell.setDirty(true);
        return true;
    }

    public Set<String> getCellNames() {
        return allCells
                .stream()
                .map(CellDefinition::getName)
                .collect(Collectors.toSet());
    }

    public boolean addSignLocation(final Vec3D location, final String cellName, final Player signCreator) {
        final CellDefinition cellDefinition = allCells
                .stream()
                .filter(cellDef -> cellDef.getName().equalsIgnoreCase(cellName))
                .findFirst()
                .orElse(null);
        if (cellDefinition == null) {
            signCreator.sendMessage("There was no Cell with that name. Please create a matching cell definition first.");
            return false;
        }
        allCells.remove(cellDefinition);
        cellDefinition.setSignLocation(location);
        cellDefinition.setDirty(true);
        allCells.add(cellDefinition);
        signLocationMap.put(location, cellDefinition);
        return true;
    }

    public CellDefinition getCellDefinitionByLocation(final Vec3D location) {
        return signLocationMap.get(location);
    }

    public void initInMemoryStore() {
        final File cellsDir = new File(SimpleCells.getInstance().getDataFolder().getAbsolutePath() + "/cells");
        if (!cellsDir.exists()) {
            SimpleCells.log(Level.SEVERE, "No cell directory, if this is your first time starting with this plugin, ignore this!");
            return;
        }
        File[] cellFiles = cellsDir.listFiles();
        if (cellFiles == null) {
            SimpleCells.log(Level.SEVERE, "Failed to list files in cells directory");
            return;
        }

        for (final File file : cellFiles) {
            try {
                final String contents = Files.readString(file.toPath());
                final CellDefinition cell = gson.fromJson(contents, CellDefinition.class);
                allCells.add(cell);
            } catch (Exception e) {
                SimpleCells.log(Level.SEVERE, "Failed to load cell from file " + file.getName());
                SimpleCells.log(e);
            }
        }
    }

    public void cleanUpDirtyStorage() {
        final Set<CellDefinition> cellsToSave = new HashSet<>();

        for (CellDefinition cell : allCells) {
            synchronized (allCells) {
                if (cell.isDirty()) {
                    cellsToSave.add(cell);
                    cell.setDirty(false);
                }
                if (cell.isMarkedForDeletion()) {
                    locationToPlayerOwnerMap.remove(cell);
                    allCells.remove(cell);
                }
            }
        }
        saveCells(cellsToSave);
    }

    private void saveCells(Set<CellDefinition> cellsToSave) {
        for (final CellDefinition cell : cellsToSave) {
            final File file = new File(SimpleCells.getInstance().getDataFolder().getAbsolutePath() + "/cells/" + cell.getName() + ".json");

            if (cell.isMarkedForDeletion()) {
                final boolean fileDeletion = file.delete();
                if (!fileDeletion) {
                    SimpleCells.log(Level.INFO, "Failed to delete file for cell " + cell.getName());
                }
                continue;
            }

            try {
                final boolean parentExists = file.getParentFile().exists();
                final boolean parentCreationSuccess = parentExists || file.getParentFile().mkdirs();

                if (!parentCreationSuccess) {
                    SimpleCells.log(Level.INFO, "Failed to create parent directory for cell " + cell.getName());
                    allCells.remove(cell);
                    continue;
                }

                final boolean createFileSuccess = file.createNewFile();
                if (!createFileSuccess) {
                    SimpleCells.log(Level.INFO, "Failed to create file for cell " + cell.getName());
                    allCells.remove(cell);
                    continue;
                }

                Writer writer = new FileWriter(file);
                gson.toJson(cell, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                SimpleCells.log(Level.INFO, "Failed to save cell " + cell.getName());
                SimpleCells.log(e);
            }
            SimpleCells.log(Level.FINER, "Saved cell " + cell.getName());
        }
    }
}
