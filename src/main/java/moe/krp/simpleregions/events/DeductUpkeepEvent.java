package moe.krp.simpleregions.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class DeductUpkeepEvent extends Event implements Cancellable {
    boolean canceled;
    HandlerList handlers;

    @Setter @Getter
    boolean economyInteractHandled;

    @Setter @Getter
    BigDecimal upkeepCost;

    public DeductUpkeepEvent(BigDecimal upkeepCost) {
        this.canceled = false;
        this.economyInteractHandled = false;
        this.handlers = new HandlerList();
        this.upkeepCost = upkeepCost;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
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
