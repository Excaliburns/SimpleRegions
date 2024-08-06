package moe.krp.simpleregions.util;

import moe.krp.simpleregions.SimpleRegions;
import moe.krp.simpleregions.enums.InteractionType;
import moe.krp.simpleregions.helpers.RegionDefinition;
import moe.krp.simpleregions.helpers.RegionTypeConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ConfigUtils {
    private static final SimpleRegions instance = SimpleRegions.getInstance();

    public static Predicate<? super RegionDefinition> playerAllowed(
            final Player player,
            final InteractionType type
    ) {
        return (regionDefinition) -> {
            boolean result;

            if (player.hasPermission("SimpleRegions.*")) {
                return true;
            }

            if (player.hasPermission("SimpleRegions.denyInteract")) {
                return false;
            }

            String node = "SimpleRegions." + regionDefinition.getRegionType();
            switch (type) {
                case BUY -> node += ".buy";
                case INTERACT -> node += ".interact";
                case SIGN -> node += ".sign";
                case CHEST -> node += ".chest";
                case BREAK_BLOCK -> node += ".breakBlock";
                case PLACE_BLOCK -> node += ".placeBlock";
            }
            result = player.hasPermission(node);

            if (type == InteractionType.BUY) {
                return result;
            }

            final boolean isAllowInteractionsIfUnowned = regionDefinition.getConfiguration().getAllowInteractUnowned() && regionDefinition.getOwner() == null;

            if (isAllowInteractionsIfUnowned) {
                return true;
            }

            final boolean isOwner = regionDefinition.getOwner() != null && regionDefinition.getOwner().equals(player.getUniqueId());
            final boolean isAllowedPlayer = regionDefinition.getOtherAllowedPlayers().containsKey(player.getUniqueId());

            // if they have the node, or they are the owner, or they are an allowed player
            result = result || isOwner || isAllowedPlayer;

            return result;
        };
    }

    public static List<String> getRegionTypes() {
        final ConfigurationSection section = instance.getConfig()
                                                     .getConfigurationSection("region-type-configuration");
        if (section == null) {
            return Collections.emptyList();
        }
        return section.getKeys(false)
                      .stream()
                      .toList();
    }

    public static String getDisplayName() {
        return instance.getConfig().getString("display-name");
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

        final int parsedOwnerLimit = configuration.getInt(path + ".owner-limit");
        final Integer ownerLimit = parsedOwnerLimit == 0 ? configuration.getInt("default-owner-limit") : parsedOwnerLimit;

        final String buySignLineZeroColor = Optional.ofNullable(configuration.getString(path + ".buy-sign-line-zero-color"))
                .orElse(configuration.getString("default-buy-sign-line-zero-color"));
        final String buySignLineZero = Optional.ofNullable(configuration.getString(path + ".buy-sign-line-zero"))
                                               .orElse(configuration.getString("default-buy-sign-line-zero"));

        return RegionTypeConfiguration
                .builder()
                .buySignLineZeroColor(buySignLineZeroColor)
                .buySignLineZero(buySignLineZero)
                .removeItemBlockFilter(configuration.getStringList(path + ".remove-item-block-whitelist"))
                .removeItemsOnNewOwner(configuration.getBoolean(path + ".remove-items-on-new-owner"))
                .upkeepInterval(TimeUtils.getDurationFromTimeString(configuration.getString(path + ".upkeep-interval")))
                .upkeepCost(configuration.getDouble(path + ".upkeep-cost"))
                .ownerLimit(ownerLimit)
                .allowInteractUnowned(configuration.getBoolean(path + ".allow-interact-unowned"))
                .build();
    }
}
