package moe.krp.simpleregions.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class PreUpkeepCostCheckEvent extends Event {
    @Getter
    static final HandlerList handlerList = new HandlerList();

    @Getter @Setter
    BigDecimal cost;
    @Getter @Setter
    boolean hasEnough;
    @Getter
    OfflinePlayer player;

    public PreUpkeepCostCheckEvent(
            final BigDecimal initialCost,
            final OfflinePlayer player,
            final boolean hasEnough
    ) {
        this.cost = initialCost;
        this.player = player;
        this.hasEnough = hasEnough;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
