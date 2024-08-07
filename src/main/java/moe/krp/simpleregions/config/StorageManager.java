package moe.krp.simpleregions.config;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.regions.Region;
import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.events.DeductUpkeepEvent;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.SignDefinition;
import moe.krp.simpleregions.helpers.Vec3D;
import moe.krp.simpleregions.listeners.SignListener;
import moe.krp.simpleregions.util.ChatUtils;
import moe.krp.simpleregions.util.RegionUtils;
import moe.krp.simpleregions.util.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class StorageManager {
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setVersion(1.3)
            .create();

    private final ConcurrentHashMap<String, RegionDefinition> regionNameToRegionMap;
    private final ConcurrentHashMap<Vec3D, List<String>> chunkVecToRegionNameMap;
    private final ConcurrentHashMap<RegionDefinition, UUID> locationToPlayerOwnerMap;
    private final ConcurrentHashMap<Vec3D, RegionDefinition> signLocationMap;

    public StorageManager() {
        regionNameToRegionMap = new ConcurrentHashMap<>();
        chunkVecToRegionNameMap = new ConcurrentHashMap<>();
        locationToPlayerOwnerMap = new ConcurrentHashMap<>();
        signLocationMap = new ConcurrentHashMap<>();
    }

    public Optional<RegionDefinition> findRegionByPoint(final Location location) {
        return findRegionByPoint(new Vec3D(location));
    }

    public Optional<RegionDefinition> findRegionByPoint(final Vec3D point) {
        final Vec3D chunkPoint = point.toChunkVec();
        final List<String> regionNames = chunkVecToRegionNameMap.get(chunkPoint);
        if (regionNames == null) {
            return Optional.empty();
        }

        return regionNames
                .stream()
                .map(this::getRegionByName)
                .flatMap(Optional::stream)
                .filter( region -> region.getRegionIfPointWithin(point) != null)
                .findFirst();
    }

    public Optional<RegionDefinition> getRegionByName(String name) {
        final RegionDefinition def = regionNameToRegionMap.get(name);
        if (def == null || def.isMarkedForDeletion()) {
            return Optional.empty();
        }

        return Optional.of(def);
    }

    public boolean addRegion(String name, String worldName, UUID creator, String regionType, Region region) {
        final Vec3D minPoint = new Vec3D(region.getMinimumPoint(), worldName);
        final Vec3D maxPoint = new Vec3D(region.getMaximumPoint(), worldName);
        final RegionDefinition regionDefinition = new RegionDefinition(
                name,
                minPoint,
                maxPoint,
                creator,
                regionType
        );

        regionNameToRegionMap.put(name, regionDefinition);

        addRegionToChunkVecMap(regionDefinition);

        return true;
    }

    public void changeRegionBounds(
            final String name,
            final Vec3D minPoint,
            final Vec3D maxPoint,
            final CommandSender sender
    ) {
        getRegionByName(name)
                .ifPresentOrElse( region -> {
                    removeRegionFromChunkVecMap(region);
                    region.setLowerBound(minPoint);
                    region.setUpperBound(maxPoint);
                    addRegionToChunkVecMap(region);
                    region.setDirty(true);
                }, () -> {
                    if (sender == null) {
                        return;
                    }
                    ChatUtils.sendErrorMessage(sender, "That region does not exist.");
                });
    }

    public void markRegionForDelete(final String name) {
        Optional.ofNullable(regionNameToRegionMap.get(name))
                .ifPresent( def -> {
                    def.setMarkedForDeletion(true);
                    def.setDirty(true);
                });
    }

    public int getNumberOfOwnedRegionsForPlayer(final String regionType, final UUID player) {
        return regionNameToRegionMap
                .values()
                .stream()
                .filter( regionDefinition -> regionDefinition.getRegionType().equals(regionType))
                .filter( regionDefinition -> regionDefinition.getOwner() != null && regionDefinition.getOwner().equals(player))
                .collect(Collectors.toSet())
                .size();
    }

    public Set<String> getRegionNames() {
        return regionNameToRegionMap
                .entrySet()
                .stream()
                .filter((entry) -> !entry.getValue().isMarkedForDeletion())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public RegionDefinition getRegionDefinitionBySignLocation(final Location location) {
        return getRegionDefinitionBySignLocation(new Vec3D(location));
    }

    public RegionDefinition getRegionDefinitionBySignLocation(final Vec3D location) {
        return signLocationMap.get(location);
    }

    public void setType(final String regionName, final String regionType) {
        getRegionByName(regionName)
                .ifPresent( region -> region.setRegionType(regionType));
    }

    public void resetUpkeep(final String regionName) {
        getRegionByName(regionName)
                .ifPresent(region -> {
                    region.setUpkeepTimer(TimeUtils.getTimeStringFromDuration(region.getUpkeepInterval()));
                    region.setDirty(true);
                });
    }

    public void resetOwnership(final String regionName) {
        getRegionByName(regionName)
                .ifPresent( region -> {
                    region.setPreviousOwner(region.getOwner());
                    final World world = Bukkit.getServer().getWorld(region.getWorld());
                    if (world != null) {
                        if (region.getRelatedSign() != null) {
                            final Vec3D location = region.getRelatedSign().getLocation();
                            SignListener.resetWorldSign(
                                    (Sign) world.getBlockAt(location.toLocation()).getState(),
                                    SimpleRegions.getStorageManager().getRegionDefinitionBySignLocation(location),
                                    region.getRelatedSign().getCost()
                            );
                        }
                    }
                    else {
                        SimpleRegions.log("World " + region.getWorld() + " does not exist while resetting ownership of " + region.getName());
                    }

                    region.clearOwnerAndReset();
                });
    }

    public void addAllowedPlayer(final String regionName, final UUID player, final String playerName) {
        getRegionByName(regionName).ifPresent(
                region-> {
                    region.getOtherAllowedPlayers().put(player, playerName);
                    region.setDirty(true);
                }
        );
    }

    public void removeAllowedPlayer(final String regionName, final UUID player) {
        getRegionByName(regionName)
                .ifPresent( region -> {
                    region.getOtherAllowedPlayers().remove(player);
                    region.setDirty(true);
                });
    }

    public void addSign(final String regionName, final SignDefinition signDefinition) {
        getRegionByName(regionName).ifPresentOrElse(
                region -> {
                    region.setRelatedSign(signDefinition);
                    region.setDirty(true);
                    signLocationMap.put(signDefinition.getLocation(), region);
                }, () ->
                SimpleRegions.log(
                        Level.SEVERE,
                        String.format("Could not add sign for region %s, was not found in map", regionName)
                )
        );
    }

    public void removeSign(final String regionName) {
        getRegionByName(regionName)
                .ifPresentOrElse(region -> {
                            final SignDefinition signDefinition = region.getRelatedSign();
                            region.setRelatedSign(null);
                            region.setDirty(true);
                            signLocationMap.remove(signDefinition.getLocation());
                        }, () ->
                                SimpleRegions.log(
                                        Level.SEVERE,
                                        String.format("Could not remove sign for region %s, was not found in map", regionName)
                                )
                );
    }

    public void setOriginalDuration(
            final String regionName,
            final String originalDuration,
            final CommandSender sender
    ) {
        getRegionByName(regionName)
                .ifPresentOrElse( region -> {
                    final Duration parsed = TimeUtils.getDurationFromTimeString(originalDuration);
                    final SignDefinition relatedSign = region.getRelatedSign();
                    relatedSign.initDuration(TimeUtils.getTimeStringFromDuration(parsed), relatedSign.getDuration());
                    region.setRelatedSign(relatedSign);
                    region.setDirty(true);
                }, () -> {
                    final String err = "That region does not exist!";
                    if (sender == null) {
                        SimpleRegions.log(Level.SEVERE, err);
                        return;
                    }
                    ChatUtils.sendErrorMessage(sender, err);
                });
    }

    public void setTimeRemaining(
            final String regionName,
            final String timeRemaining,
            final boolean upkeep,
            final CommandSender sender
    ) {
        getRegionByName(regionName)
                .ifPresentOrElse( region -> {
                    final Duration parsed = TimeUtils.getDurationFromTimeString(timeRemaining);
                    if (upkeep) {
                        region.setUpkeepTimer(TimeUtils.getTimeStringFromDuration(parsed));
                    }
                    else {
                        final SignDefinition def = region.getRelatedSign();
                        if (def == null) {
                            final String err = "That region does not have a related sign!";
                            if (sender == null) {
                                SimpleRegions.log(Level.SEVERE, err);
                                return;
                            }
                            ChatUtils.sendErrorMessage(sender, err);
                            return;
                        }

                        def.setDuration(TimeUtils.getTimeStringFromDuration(parsed));
                    }
                    region.setDirty(true);
                }, () -> {
                    final String err = "That region does not exist!";
                    if (sender == null) {
                        SimpleRegions.log(Level.SEVERE, err);
                        return;
                    }
                    ChatUtils.sendErrorMessage(sender, err);
                });
    }

    public void initInMemoryStore() {
        final File regionsDr = new File(SimpleRegions.getInstance().getDataFolder().getAbsolutePath() + "/regions");
        if (!regionsDr.exists()) {
            SimpleRegions.log(Level.SEVERE, "No Region directory, if this is your first time starting with this plugin, ignore this!");
            return;
        }
        File[] regionFiles = regionsDr.listFiles();
        if (regionFiles == null) {
            SimpleRegions.log(Level.SEVERE, "Failed to list files in Regions directory");
            return;
        }

        for (final File file : regionFiles) {
            try {
                final String contents = Files.readString(file.toPath());
                final RegionDefinition region = gson.fromJson(contents, RegionDefinition.class);
                region.setConfiguration(region.getRegionType());
                regionNameToRegionMap.put(region.getName(), region);
                addRegionToChunkVecMap(region);
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

        for (RegionDefinition region : regionNameToRegionMap.values()) {
            if (region.isDirty()) {
                regionsToSave.add(region);
                region.setDirty(false);
            }
            if (region.isMarkedForDeletion()) {
                locationToPlayerOwnerMap.remove(region);
                regionNameToRegionMap.remove(region.getName());
                removeRegionFromChunkVecMap(region);
            }
        }
        saveRegions(regionsToSave);
    }

    public void setRegionOwned(
            final String regionName,
            final UUID owner,
            final String name
    ) {
        final PlayerProfile profile = Bukkit.createProfile(owner, name);
        final String displayName = profile.getName() != null ? profile.getName() : name != null ? name : owner.toString();


        getRegionByName(regionName)
                .ifPresentOrElse( region -> {
                    final BlockState blockState = getSignBlockStateForRegion(region);
                    if (blockState instanceof Sign signBlock) {
                        signBlock.line(0, Component.text(region.getName()).color(TextColor.color(0xFF5555)));
                        signBlock.line(1, Component.text(displayName).color(TextColor.color(0xFFAA00)));
                        updateSignDurations(region, signBlock);
                    }

                    region.setOwner(owner);
                    region.setDirty(true);

                    if (!region.getConfiguration().getRemoveItemsOnNewOwner()
                            || (region.getPreviousOwner() != null && region.getPreviousOwner().equals(owner))
                            || region.getPreviousOwner() == null
                    ) {
                        return;
                    }

                    final World world = Bukkit.getWorld(region.getWorld());
                    if (world == null) {
                        SimpleRegions.log(Level.SEVERE, "World " + region.getWorld() + "doesn't exist!");
                        return;
                    }
                    RegionUtils.removeBlocksOnExpiry(world, region);
                }, () -> SimpleRegions.log(Level.SEVERE, "Region " + regionName + " not found during owner update."));
    }

    public void tickSigns(final Duration duration) {
        regionNameToRegionMap.values().forEach( region -> {
            if (region.getOwner() == null) {
                return;
            }

            final BlockState blockState = getSignBlockStateForRegion(region);
            if (blockState instanceof Sign signBlock) {
                final SignDefinition sign = region.getRelatedSign();
                final Duration newSignDuration = sign.tickDownTime(duration);

                if (newSignDuration.isNegative() || newSignDuration.isZero()) {
                    SimpleRegions.getStorageManager().resetOwnership(region.getName());
                    return;
                }

                if (newSignDuration.toSecondsPart() == 0) {
                    updateSignDurations(region, signBlock);
                }

                if (region.getUpkeepInterval() != null) {
                    final Duration newRegionUpkeepDuration = region.tickDownTime(duration);
                    if (newRegionUpkeepDuration.isNegative() || newRegionUpkeepDuration.isZero()) {
                        final OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwner());
                        final Economy economy = SimpleRegions.getEconomy();
                        if (economy.has(owner, region.getUpkeepCost())) {
                            final DeductUpkeepEvent upkeepEvent = new DeductUpkeepEvent(
                                    BigDecimal.valueOf(region.getUpkeepCost()).setScale(2, RoundingMode.DOWN)
                            );
                            Bukkit.getPluginManager().callEvent(upkeepEvent);
                            if (!upkeepEvent.isEconomyInteractHandled() || !upkeepEvent.isCancelled()) {
                                economy.withdrawPlayer(owner, region.getUpkeepCost());
                            }
                            SimpleRegions.getStorageManager().resetUpkeep(region.getName());
                        }
                        else {
                            SimpleRegions.getStorageManager().resetOwnership(region.getName());
                        }
                    }
                }

                region.setDirty(true);
            }
        });
    }

    private void updateSignDurations(
            final RegionDefinition region,
            final Sign signBlock
    ) {
        if (region.getUpkeepInterval() != null && region.getRelatedSign().isNeverExpire()) {
            signBlock.line(2, Component.text("Next upkeep:"));
            signBlock.line(3, Component.text(region.getUpkeepTimer()));
        }
        else if (region.getUpkeepInterval() != null && !region.getRelatedSign().isNeverExpire()) {
            signBlock.line(2, Component.text("Upkeep: " + region.getUpkeepTimer()));
            signBlock.line(3, Component.text("Expiry: " + region.getRelatedSign().getDuration()));
        }
        else {
            signBlock.line(2, Component.text("Owned until:"));
            signBlock.line(3, Component.text(region.getRelatedSign().getDuration()));
        }
        signBlock.update();
    }

    private void removeRegionFromChunkVecMap(final RegionDefinition regionDefinition) {
        regionDefinition.getEnvelopingChunkVectors().forEach( vector ->
                chunkVecToRegionNameMap.compute(vector, (vectorInMap, regionNames) -> {
                    if (regionNames != null) {
                        regionNames.remove(regionDefinition.getName());
                    }
                    return regionNames;
                }));
    }

    private void addRegionToChunkVecMap(final RegionDefinition region) {
        region.getEnvelopingChunkVectors().forEach( vector ->
                chunkVecToRegionNameMap.compute(vector, (vectorInMap, regionNames) -> {
                    if (regionNames == null) {
                        regionNames = new ArrayList<>();
                    }
                    regionNames.add(region.getName());
                    return regionNames;
                }));
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
                    regionNameToRegionMap.remove(region);
                    continue;
                }

                final boolean createFileSuccess = file.exists() || file.createNewFile();
                if (!createFileSuccess) {
                    SimpleRegions.log(Level.INFO, "Failed to create file for Region " + region.getName());
                    regionNameToRegionMap.remove(region);
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
