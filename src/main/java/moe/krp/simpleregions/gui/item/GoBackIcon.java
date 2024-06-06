package moe.krp.simpleregions.gui.item;

import mc.obliviate.inventory.Gui;
import mc.obliviate.inventory.Icon;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GoBackIcon extends Icon {
    public GoBackIcon(Consumer<InventoryClickEvent> onBack) {
        super(Material.BARRIER);
        setName(ChatColor.GRAY +
                ">> " +
                ChatColor.BOLD + ChatColor.RED +
                "BACK"
        );
        this.onClick(onBack);
    }
}
