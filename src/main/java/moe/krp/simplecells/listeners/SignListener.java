package moe.krp.simplecells.listeners;

import moe.krp.simplecells.SimpleCells;
import moe.krp.simplecells.util.Vec3D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Objects;

public class SignListener implements Listener {

    @EventHandler
    public void onCreateSign(SignChangeEvent e) {
        final Player user = e.getPlayer();

        if (Objects.equals(e.line(0), Component.text(SimpleCells.getSignLineZero()))) {
            if (!user.hasPermission("simplecells.create")) {
                user.sendMessage("You do not have permission to create a cell sign");
                e.setCancelled(true);
                return;
            }

            final Vec3D signLocation = new Vec3D(e.getBlock().getLocation());
            if (SimpleCells.getStorageManager().getCellDefinitionByLocation(signLocation) != null) {
                user.sendMessage("A cell already exists at this location");
                e.setCancelled(true);
                return;
            }
            final Component line1 = e.line(1);
            if (line1 == null) {
                user.sendMessage("Please specify a cell name");
                return;
            }

            final String cellName = PlainTextComponentSerializer.plainText()
                                                                .serialize(line1);

            final boolean success = SimpleCells.getStorageManager().addSignLocation(signLocation, cellName, user);
            if (success) {
                user.sendMessage("Sign registered for cell " + cellName);
            }
        }
    }
}
