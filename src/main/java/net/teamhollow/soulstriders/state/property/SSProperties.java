package net.teamhollow.soulstriders.state.property;

import net.minecraft.state.property.IntProperty;

public class SSProperties {
    /**
     * A property that specifies how many bulbs are in a soul strider bulb block.
     */
    public static final IntProperty BULBS = IntProperty.of("bulbs", 1, 4);
}
