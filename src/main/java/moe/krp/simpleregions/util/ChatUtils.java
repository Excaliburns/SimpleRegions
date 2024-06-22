package moe.krp.simpleregions.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChatUtils {
    public static void sendMessage(final Audience audience, final String string) {
        sendMessage(audience, Component.text(string));
    }
    public static void sendMessage(final Audience audience, final Component component) {
        audience.sendMessage(
                ConfigUtils.getChatPrefix()
                           .append(component).color(TextColor.color(0xDADADA))
        );
    }
    public static void sendErrorMessage(final Audience audience, final String string) {
        sendErrorMessage(audience, Component.text(string));
    }
    public static void sendErrorMessage(final Audience audience, final Component component) {
        audience.sendMessage(
                ConfigUtils.getChatPrefix()
                           .append(Component.text("ERROR: ").color(TextColor.color(0xFF5555)))
                           .append(component).color(TextColor.color(0xFF5555))
        );
    }
}
