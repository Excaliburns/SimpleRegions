package moe.krp.simpleregions.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeductUpkeepEvent extends Event implements Cancellable {
    boolean canceled;
    HandlerList handlers;

    @Setter @Getter
    boolean economyInteractHandled;

    public DeductUpkeepEvent() {
        this.canceled = false;
        this.economyInteractHandled = false;
        this.handlers = new HandlerList();
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
