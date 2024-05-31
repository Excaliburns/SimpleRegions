package moe.krp.simpleregions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.util.RegionDefinition;
import moe.krp.simpleregions.util.SignDefinition;
import moe.krp.simpleregions.util.Vec3D;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.time.Duration;
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

    private final Set<RegionDefinition> allRegions;
    private final ConcurrentHashMap<RegionDefinition, UUID> locationToPlayerOwnerMap;
    private final ConcurrentHashMap<Vec3D, RegionDefinition> signLocationMap;

    public StorageManager() {
        allRegions = new HashSet<>();
        locationToPlayerOwnerMap = new ConcurrentHashMap<>();
        signLocationMap = new ConcurrentHashMap<>();
    }

    public RegionDefinition getRegion(String name) {
        return allRegions
                .stream()
                .filter(Region -> Region.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean addRegion(String name, String worldName, UUID creator, String RegionType, Region Region) {
        synchronized (allRegions) {
            allRegions.add(
                    new RegionDefinition(
                            name,
                            new Vec3D(Region.getMinimumPoint(), worldName),
                            new Vec3D(Region.getMaximumPoint(), worldName),
                            creator,
                            RegionType
                    )
            );
        }

        return true;
    }

    public void markRegionForDelete(final String name) {
        allRegions.stream()
                .filter( Region -> Region.getName().equals(name))
                .findFirst()
                .ifPresent( Region -> {
                    Region.setMarkedForDeletion(true);
                    Region.setDirty(true);
                });
    }

    public Set<String> getRegionNames() {
        return allRegions
                .stream()
                .map(RegionDefinition::getName)
                .collect(Collectors.toSet());
    }

    public RegionDefinition getRegionDefinitionByLocation(final Vec3D location) {
        return signLocationMap.get(location);
    }

    public boolean addSign(String regionName, SignDefinition signDefinition) {
        final RegionDefinition RegionDefinition = allRegions
                .stream()
                .filter(regionDef -> regionDef.getName().equalsIgnoreCase(regionName))
                .findFirst()
                .orElse(null);
        // Validation should be handled by calling method
        if (RegionDefinition == null) {
            return false;
        }

        allRegions.remove(RegionDefinition);
        RegionDefinition.setRelatedSign(signDefinition);
        RegionDefinition.setDirty(true);
        allRegions.add(RegionDefinition);
        signLocationMap.put(signDefinition.getLocation(), RegionDefinition);
        return true;
    }

    public void initInMemoryStore() {
        final File RegionsDir = new File(SimpleRegions.getInstance().getDataFolder().getAbsolutePath() + "/Regions");
        if (!RegionsDir.exists()) {
            SimpleRegions.log(Level.SEVERE, "No Region directory, if this is your first time starting with this plugin, ignore this!");
            return;
        }
        File[] RegionFiles = RegionsDir.listFiles();
        if (RegionFiles == null) {
            SimpleRegions.log(Level.SEVERE, "Failed to list files in Regions directory");
            return;
        }

        for (final File file : RegionFiles) {
            try {
                final String contents = Files.readString(file.toPath());
                final RegionDefinition Region = gson.fromJson(contents, RegionDefinition.class);
                allRegions.add(Region);
            } catch (Exception e) {
                SimpleRegions.log(Level.SEVERE, "Failed to load Region from file " + file.getName());
                SimpleRegions.log(e);
            }
        }
    }

    public void cleanUpDirtyStorage() {
        final Set<RegionDefinition> RegionsToSave = new HashSet<>();

        for (RegionDefinition Region : allRegions) {
            synchronized (allRegions) {
                if (Region.isDirty()) {
                    RegionsToSave.add(Region);
                    Region.setDirty(false);
                }
                if (Region.isMarkedForDeletion()) {
                    locationToPlayerOwnerMap.remove(Region);
                    allRegions.remove(Region);
                }
            }
        }
        saveRegions(RegionsToSave);
    }

    public void setRegionOwned(
            final String regionName,
            final UUID owner
    ) {
        final Player player = Bukkit.getPlayer(owner);
        if (player == null) {
            SimpleRegions.log(Level.INFO, "Player not found during signage update for region " + regionName + ".");
            SimpleRegions.log("Player UUID: " + owner);
            return;
        }

        final RegionDefinition region = getRegion(regionName);
        if (region == null) {
            SimpleRegions.log(Level.INFO, "Region " + regionName + " not found during signage update.");
            return;
        }

        final BlockState blockState = getSignBlockStateForRegion(region);
        if (blockState instanceof Sign signBlock) {
            signBlock.line(1, Component.text("Owned By:"));
            signBlock.line(2, Component.text(player.getName()));
            signBlock.line(3, Component.text(region.getRelatedSign().getDuration()));
            signBlock.update();
        }

        region.setOwnedBy(owner);
        region.setDirty(true);
    }

    public void tickSigns(final Duration duration) {
        allRegions.forEach( region -> {
            if (region.getOwnedBy() == null) {
                return;
            }

            final BlockState blockState = getSignBlockStateForRegion(region);
            if (blockState instanceof Sign signBlock) {
                final SignDefinition sign = region.getRelatedSign();
                sign.tickDownTime(duration);
                signBlock.line(3, Component.text(sign.getDuration()));
                signBlock.update();
                region.setDirty(true);
            }
        });
    }

    private BlockState getSignBlockStateForRegion(final RegionDefinition region) {
        final SignDefinition sign = region.getRelatedSign();
        if (sign == null) {
            return null;
        }
        final Server server = Bukkit.getServer();
        final World world = server.getWorld(region.getWorld());
        if (world == null) {
            SimpleRegions.log("World was null for the sign of region "+ region.getName() + " at " + sign.getLocation().toString());
            return null;
        }
        final Block block = world.getBlockAt(sign.getLocation().toLocation());
        return block.getState();
    }

    private void saveRegions(Set<RegionDefinition> RegionsToSave) {
        for (final RegionDefinition Region : RegionsToSave) {
            final File file = new File(SimpleRegions.getInstance().getDataFolder().getAbsolutePath() + "/Regions/" + Region.getName() + ".json");

            if (Region.isMarkedForDeletion()) {
                final boolean fileDeletion = file.delete();
                if (!fileDeletion) {
                    SimpleRegions.log(Level.INFO, "Failed to delete file for Region " + Region.getName());
                }
                continue;
            }

            try {
                final boolean parentExists = file.getParentFile().exists();
                final boolean parentCreationSuccess = parentExists || file.getParentFile().mkdirs();

                if (!parentCreationSuccess) {
                    SimpleRegions.log(Level.INFO, "Failed to create parent directory for Region " + Region.getName());
                    allRegions.remove(Region);
                    continue;
                }

                final boolean createFileSuccess = file.exists() || file.createNewFile();
                if (!createFileSuccess) {
                    SimpleRegions.log(Level.INFO, "Failed to create file for Region " + Region.getName());
                    allRegions.remove(Region);
                    continue;
                }

                Writer writer = new FileWriter(file);
                gson.toJson(Region, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                SimpleRegions.log(Level.INFO, "Failed to save Region " + Region.getName());
                SimpleRegions.log(e);
            }
            SimpleRegions.log(Level.FINER, "Saved Region " + Region.getName());
        }
    }
}
