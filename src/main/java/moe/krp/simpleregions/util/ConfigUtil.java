package moe.krp.simpleregions.util;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.helpers.RegionTypeConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigUtil {
    private static final SimpleRegions instance = SimpleRegions.getInstance();

    public static List<String> getRegionTypes() {
        return instance.getConfig().getStringList("region-types");
    }

    public static String getDisplayName() {
        return instance.getConfig().getString("display-name");
    }

    public static String getBuyRegionGuiTitle() {
        return instance.getConfig().getString("sign-gui-title");
    }

    public static Component getChatPrefix() {
        return Component.text()
                .append(Component.text("[", TextColor.color(0xD00000)))
                .append(Component.text(getDisplayName(), TextColor.color(0xFFBA08)))
                .append(Component.text("]", TextColor.color(0xD00000)))
                .append(Component.text(" >> ", TextColor.color(0x3F88C5)))
                .build();
    }

    public static RegionTypeConfiguration getRegionTypeConfiguration(String regionType) {
        final String path = "region-type-configuration." + regionType;
        final FileConfiguration configuration = instance.getConfig();
        return RegionTypeConfiguration
                .builder()
                .buySignLineZeroColor(configuration.getString(path + ".buy-sign-line-zero-color"))
                .buySignLineZero(configuration.getString(path + ".buy-sign-line-zero"))
                .removeItemsOnExpiry(configuration.getBoolean(path + ".remove-items-on-expiry"))
                .upkeep(configuration.getBoolean(path + ".upkeep"))
                .upkeepInterval(TimeUtils.getDurationFromTimeString(configuration.getString(path + ".upkeep-interval")))
                .upkeepCost(configuration.getDouble(path + ".upkeep-cost"))
                .build();
    }
}
