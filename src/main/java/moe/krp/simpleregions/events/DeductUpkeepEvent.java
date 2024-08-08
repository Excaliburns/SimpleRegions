package moe.krp.simpleregions.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class DeductUpkeepEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    boolean canceled;
    @Setter @Getter
    boolean economyInteractHandled;
    @Setter @Getter
    BigDecimal upkeepCost;
    @Getter
    OfflinePlayer player;

    public DeductUpkeepEvent(BigDecimal upkeepCost, OfflinePlayer player) {
        this.canceled = false;
        this.economyInteractHandled = false;
        this.upkeepCost = upkeepCost;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }
}
