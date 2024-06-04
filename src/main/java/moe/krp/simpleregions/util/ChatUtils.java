package moe.krp.simpleregions.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;

public class ChatUtils {
    public static void sendMessage(final Audience audience, final String string) {
        sendMessage(audience, Component.text(string));
    }
    public static void sendMessage(final Audience audience, final Component component) {
        audience.sendMessage(
                ConfigUtil.getChatPrefix()
                          .append(component)
        );
    }
    public static void sendErrorMessage(final Audience audience, final String string) {
        sendErrorMessage(audience, Component.text(string));
    }
    public static void sendErrorMessage(final Audience audience, final Component component) {
        audience.sendMessage(
                ConfigUtil.getChatPrefix()
                          .append(Component.text(ChatColor.RED + " ERROR: "))
                          .append(component)
        );
    }
}
