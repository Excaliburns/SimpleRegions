package moe.krp.simpleregions.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class SignDefinition {
    final double cost;
    final Vec3D location;
    final String duration;
}
