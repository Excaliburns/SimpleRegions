package moe.krp.simpleregions.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class ItemUtils {
    public enum Skulls {
        PLUS_SIGN ("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"),
        MINUS_SIGN("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=");

        private String texture;

        Skulls (String texture) {
            this.texture = texture;
        }
    }

    public static ItemStack getSkull(Skulls skull) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) item.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", skull.texture));
        meta.setPlayerProfile(profile);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getSkullForPlayerUuid(UUID uuid, String name) {
        return getSkullForPlayerUuid(uuid.toString(), name);
    }
    public static ItemStack getSkullForPlayerUuid(String uuid, String name) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        final SkullMeta meta = (SkullMeta) skull.getItemMeta();
        final PlayerProfile profile = Bukkit.createProfile(UUID.fromString(uuid), null);
        final String displayName = profile.getName() != null ? profile.getName() : name != null ? name : uuid;
        meta.displayName(Component.text().color(TextColor.color(0x136F63)).content(displayName).build());
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }
}
