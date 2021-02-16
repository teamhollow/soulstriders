package net.teamhollow.soulstriders.state.property;

import net.minecraft.state.IntegerProperty;

public class SSProperties {
    /**
     * A property that specifies how many bulbs are in a soul strider bulb block.
     */
    public static final IntegerProperty BULBS = IntegerProperty.create("bulbs", 1, 4);
}
