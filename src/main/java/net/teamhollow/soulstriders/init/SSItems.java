package net.teamhollow.soulstriders.init;

import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.OnAStickItem;
import net.minecraft.item.SignItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;

public class SSItems {
    public static final Item SOUL_MOTH_IN_A_BOTTLE = register(
        "soul_moth_in_a_bottle",
        new Item(
             new Item.Settings()
                .recipeRemainder(Items.GLASS_BOTTLE)
                .group(SoulStriders.ITEM_GROUP)
                .maxCount(1)
        )
    );
    public static final Item SOUL_MOTH_ON_A_STICK = register(
        "soul_moth_on_a_stick",
        new OnAStickItem<SoulStriderEntity>(
            new Item.Settings()
                .maxDamage(35)
                .fireproof()
                .group(SoulStriders.ITEM_GROUP),
            SSEntities.SOUL_STRIDER, 1
        )
    );

    public SSItems() {};

    public static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(SoulStriders.MOD_ID, id), item);
    }
    public static SignItem register(String id, SignBlock standingSign, WallSignBlock wallSign) {
        return (SignItem)register(id + "_sign", new SignItem(new Item.Settings().maxCount(16).group(SoulStriders.ITEM_GROUP), standingSign, wallSign));
    };
}
