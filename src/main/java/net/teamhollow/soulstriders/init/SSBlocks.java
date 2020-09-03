package net.teamhollow.soulstriders.init;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.block.*;

public class SSBlocks {
    public static final Block SOUL_STRIDER_BULB = register(
        "soul_strider_bulb",
        new SoulStriderBulbBlock()
    );

    public SSBlocks() {}

    public static Block register(String id, Block block, boolean registerItem) {
        Identifier identifier = new Identifier(SoulStriders.MOD_ID, id);

        Block registeredBlock = Registry.register(Registry.BLOCK, identifier, block);
        if (registerItem) SSItems.register(id, new BlockItem(registeredBlock, new Item.Settings().maxCount(64).group(SoulStriders.ITEM_GROUP)));

        return registeredBlock;
    }
    public static Block register(String id, Block block) {
        return register(id, block, true);
    }
}
