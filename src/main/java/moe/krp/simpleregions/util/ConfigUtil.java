package moe.krp.simpleregions.util;

import moe.krp.simpleregions.SimpleRegions;

import java.util.List;

public class ConfigUtil {
    private static final SimpleRegions instance = SimpleRegions.getInstance();

    public static List<String> getRegionTypes() {
        return instance.getConfig().getStringList("region-types");
    }

    public static String getSignLineZero() {
        return instance.getConfig().getString("sign-line-zero");
    }
}
