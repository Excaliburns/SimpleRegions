package moe.krp.simpleregions;

import lombok.Getter;
import moe.krp.simpleregions.commands.SimpleRegionsCommand;
import moe.krp.simpleregions.config.StorageManager;
import moe.krp.simpleregions.listeners.SignListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SimpleRegions extends JavaPlugin {
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
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        storageManager.cleanUpDirtyStorage();
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
    }

    private void setUpTasks() {
        // Clean up dirty storage every 2 seconds
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                this,
                () -> storageManager.cleanUpDirtyStorage(),
                0,
                40 // 20 * 2
        );
        log("Tasks set up");
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
