package moe.krp.simplecells.util;


import lombok.Getter;
import lombok.Setter;

public class CellLocation {
    @Getter @Setter
    private Vec3D lowerBound;
    @Getter @Setter
    private Vec3D upperBound;
}
