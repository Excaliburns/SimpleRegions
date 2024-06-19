package moe.krp.simpleregions.helpers;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data @Builder
public class RegionTypeConfiguration {
    final String buySignLineZeroColor;
    final String buySignLineZero;
    final Boolean removeItemsOnNewOwner;
    final Duration upkeepInterval;
    final Double upkeepCost;
    final Integer ownerLimit;
    final Boolean allowInteractUnowned;
}
