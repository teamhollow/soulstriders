package net.teamhollow.soulstriders.init;

import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.item.Item;
import net.minecraft.item.SignItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.teamhollow.soulstriders.SoulStriders;

public class SSItems {
    public static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(SoulStriders.MOD_ID, id), item);
    }
    public static SignItem register(String id, SignBlock standingSign, WallSignBlock wallSign) {
        return (SignItem)register(id + "_sign", new SignItem(new Item.Settings().maxCount(16).group(SoulStriders.ITEM_GROUP), standingSign, wallSign));
    };
}
