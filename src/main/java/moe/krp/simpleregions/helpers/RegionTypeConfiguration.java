package moe.krp.simpleregions.helpers;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Data @Builder
public class RegionTypeConfiguration {
    final String buySignLineZeroColor;
    final String buySignLineZero;
    final Boolean removeItemsOnNewOwner;
    final List<String> removeItemBlockFilter;
    final Duration upkeepInterval;
    final Double upkeepCost;
    final Integer ownerLimit;
    final Boolean allowInteractUnowned;
}
