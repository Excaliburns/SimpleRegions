package moe.krp.simpleregions.util;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionTypeConfiguration;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigUtil {
    private static final SimpleRegions instance = SimpleRegions.getInstance();

    public static List<String> getRegionTypes() {
        return instance.getConfig().getStringList("region-types");
    }

    public static String getSignLineZero() {
        return instance.getConfig().getString("sign-line-zero");
    }

    public static String getBuyRegionGuiTitle() {
        return instance.getConfig().getString("sign-gui-title");
    }

    public static Component getChatPrefix() {
        //noinspection DataFlowIssue
        return Component.text(instance.getConfig().getString("chat-prefix"));
    }

    public static RegionTypeConfiguration getRegionTypeConfiguration(String regionType) {
        final String path = "region-type-configuration." + regionType;
        final FileConfiguration configuration = instance.getConfig();
        return RegionTypeConfiguration
                .builder()
                .buySignLineZero(configuration.getString(path + ".buy-sign-line-zero"))
                .removeItemsOnExpiry(configuration.getBoolean(path + ".remove-items-on-expiry"))
                .upkeep(configuration.getBoolean(path + ".upkeep"))
                .upkeepInterval(TimeUtils.getDurationFromTimeString(configuration.getString(path + ".upkeep-interval")))
                .upkeepCost(configuration.getDouble(path + ".upkeep-cost"))
                .build();
    }
}
