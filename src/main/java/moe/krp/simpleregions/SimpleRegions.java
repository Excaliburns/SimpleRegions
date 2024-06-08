package moe.krp.simpleregions;

import lombok.Getter;
import mc.obliviate.inventory.InventoryAPI;
import moe.krp.simpleregions.commands.SimpleRegionsCommand;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.listeners.PlayerActionListener;
import moe.krp.simpleregions.listeners.RegionListeners;
import moe.krp.simpleregions.listeners.SignListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleRegions extends JavaPlugin {
    @Getter
    private static Economy economy;

    static Logger logger;
    @Getter
    private static SimpleRegions instance;

    @Getter
    private static StorageManager storageManager;

    FileConfiguration configuration = getConfig();

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        saveDefaultConfig();
        setUpTasks();
        loadInMemoryStores();
        registerCommands();
        registerListeners();
        setupEconomy();
        new InventoryAPI(this).init();
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        log("Cleaning up dirty storage.");
        storageManager.cleanUpDirtyStorage();
        log("SimpleRegions has been disabled.");
    }

    private void loadInMemoryStores() {
        storageManager = new StorageManager();
        storageManager.initInMemoryStore();
        log("In-memory stores loaded");
    }

    private void registerCommands() {
        getCommand("SimpleRegions").setExecutor(new SimpleRegionsCommand());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new SignListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerActionListener(), this);
        getServer().getPluginManager().registerEvents(new RegionListeners(), this);
    }

    private void setUpTasks() {
        // Clean up dirty storage every 2 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                () -> storageManager.cleanUpDirtyStorage(),
                0,
                40 // 20 * 2
        );
        Bukkit.getScheduler().runTaskTimer(
                this,
                () -> storageManager.tickSigns(Duration.ofSeconds(1)),
                0,
                20
        );

        log("Tasks set up");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static void log(String s) {
        log(Level.INFO, s);
    }

    public static void log(Exception e) {
        logger.log(Level.SEVERE, "[SimpleRegions] An error occurred ", e);
    }

    public static void log(Level level, Object msg) {
        logger.log(level, "[SimpleRegions] " + msg.toString());
    }

    public void addItemToConfig(final String item) {
        final List<String> items = configuration.getStringList("items");
        items.add(item);
        configuration.set("items", items);
        saveConfig();
    }

    public void removeItemFromConfig(final String item) {
        final List<String> items = configuration.getStringList("items");
        items.remove(item);
        configuration.set("items", items);
        saveConfig();
    }
}
