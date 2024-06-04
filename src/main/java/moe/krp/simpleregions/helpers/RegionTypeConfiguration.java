package moe.krp.simpleregions.helpers;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data @Builder
public class RegionTypeConfiguration {
    final String buySignLineZero;
    final Boolean removeItemsOnExpiry;
    final Boolean upkeep;
    final Duration upkeepInterval;
    final Double upkeepCost;
}
