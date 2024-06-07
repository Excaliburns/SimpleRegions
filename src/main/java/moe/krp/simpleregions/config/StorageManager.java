package moe.krp.simpleregions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import moe.krp.simpleregions.listeners.SignListener;
import moe.krp.simpleregions.util.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
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
                .filter(region -> region.getName().equals(name))
                .filter(region -> !region.isMarkedForDeletion())
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
                .filter( region -> region.getName().equals(name))
                .findFirst()
                .ifPresent( region -> {
                    region.setMarkedForDeletion(true);
                    region.setDirty(true);
                });
    }

    public Set<String> getRegionNames() {
        return allRegions
                .stream()
                .filter(region -> !region.isMarkedForDeletion())
                .map(RegionDefinition::getName)
                .collect(Collectors.toSet());
    }

    public RegionDefinition getRegionDefinitionBySignLocation(final Vec3D location) {
        return signLocationMap.get(location);
    }

    public void resetOwnership(final String regionName) {
        final RegionDefinition regionDefinition = getRegion(regionName);
        final Vec3D location = regionDefinition.getRelatedSign().getLocation();
        final World world = Bukkit.getServer().getWorld(location.getWorld());
        if (world != null) {
            SignListener.resetWorldSign(
                    (Sign) world.getBlockAt(location.toLocation()).getState(),
                    SimpleRegions.getStorageManager().getRegionDefinitionBySignLocation(location),
                    regionDefinition.getRelatedSign().getCost()
            );
        }
        else {
            SimpleRegions.log("World " + location.getWorld() + " does not exist while resetting ownership of " + regionDefinition.getName());
        }

        regionDefinition.clearOwnerAndReset();
    }

    public void addAllowedPlayer(final String regionName, final UUID player) {
        final RegionDefinition region = getRegion(regionName);
        if (region == null) {
            return;
        }
        region.getOtherAllowedPlayers().add(player);
        region.setDirty(true);
    }

    public void removeAllowedPlayer(final String regionName, final UUID player) {
        final RegionDefinition region = getRegion(regionName);
        if (region == null) {
            return;
        }
        region.getOtherAllowedPlayers().remove(player);
        region.setDirty(true);
    }

    public boolean removeSign(String regionName) {
        final RegionDefinition regionDefinition = getRegion(regionName);
        if (regionDefinition == null) {
            return false;
        }
        final SignDefinition signDefinition = regionDefinition.getRelatedSign();
        regionDefinition.setRelatedSign(null);
        regionDefinition.setDirty(true);
        signLocationMap.remove(signDefinition.getLocation());
        return true;
    }

    public boolean addSign(String regionName, SignDefinition signDefinition) {
        final RegionDefinition regionDefinition = getRegion(regionName);
        // Validation should be handled by calling method
        if (regionDefinition == null) {
            return false;
        }

        regionDefinition.setRelatedSign(signDefinition);
        regionDefinition.setDirty(true);
        signLocationMap.put(signDefinition.getLocation(), regionDefinition);
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
                final RegionDefinition region = gson.fromJson(contents, RegionDefinition.class);
                region.setConfiguration(ConfigUtils.getRegionTypeConfiguration(region.getRegionType()));
                allRegions.add(region);
                if (region.getRelatedSign() != null) {
                    signLocationMap.put(region.getRelatedSign().getLocation(), region);
                }
            } catch (Exception e) {
                SimpleRegions.log(Level.SEVERE, "Failed to load Region from file " + file.getName());
                SimpleRegions.log(e);
            }
        }
    }

    public void cleanUpDirtyStorage() {
        final Set<RegionDefinition> regionsToSave = new HashSet<>();

        for (RegionDefinition region : allRegions) {
            synchronized (allRegions) {
                if (region.isDirty()) {
                    regionsToSave.add(region);
                    region.setDirty(false);
                }
                if (region.isMarkedForDeletion()) {
                    locationToPlayerOwnerMap.remove(region);
                    allRegions.remove(region);
                }
            }
        }
        saveRegions(regionsToSave);
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
            signBlock.line(0, Component.text(region.getName()).color(TextColor.color(0xFF5555)));
            signBlock.line(1, Component.text(player.getName()).color(TextColor.color(0xFFAA00)));
            signBlock.line(2, Component.text("Owned until:"));
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
                final Duration newDuration = sign.tickDownTime(duration);

                if (newDuration.isNegative() || newDuration.isZero()) {
                    SimpleRegions.getStorageManager().resetOwnership(region.getName());
                    return;
                }

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

    private void saveRegions(Set<RegionDefinition> regionsToSave) {
        for (final RegionDefinition region : regionsToSave) {
            final File file = new File(SimpleRegions.getInstance().getDataFolder().getAbsolutePath() + "/regions/" + region.getName() + ".json");

            if (region.isMarkedForDeletion()) {
                final boolean fileDeletion = file.delete();
                if (!fileDeletion) {
                    SimpleRegions.log(Level.INFO, "Failed to delete file for Region " + region.getName());
                }
                continue;
            }

            try {
                final boolean parentExists = file.getParentFile().exists();
                final boolean parentCreationSuccess = parentExists || file.getParentFile().mkdirs();

                if (!parentCreationSuccess) {
                    SimpleRegions.log(Level.INFO, "Failed to create parent directory for Region " + region.getName());
                    allRegions.remove(region);
                    continue;
                }

                final boolean createFileSuccess = file.exists() || file.createNewFile();
                if (!createFileSuccess) {
                    SimpleRegions.log(Level.INFO, "Failed to create file for Region " + region.getName());
                    allRegions.remove(region);
                    continue;
                }

                Writer writer = new FileWriter(file);
                gson.toJson(region, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                SimpleRegions.log(Level.INFO, "Failed to save Region " + region.getName());
                SimpleRegions.log(e);
            }
            SimpleRegions.log(Level.FINER, "Saved Region " + region.getName());
        }
    }
}
