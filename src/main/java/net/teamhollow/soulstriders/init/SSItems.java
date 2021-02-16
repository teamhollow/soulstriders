package net.teamhollow.soulstriders.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.item.SoulMothOnAStickItem;

public class SSItems {
    public static final Item SOUL_MOTH_IN_A_BOTTLE = register("soul_moth_in_a_bottle", new Item(new FabricItemSettings().recipeRemainder(Items.GLASS_BOTTLE).group(SoulStriders.ITEM_GROUP).maxCount(1)));
    public static final Item SOUL_MOTH_ON_A_STICK = register("soul_moth_on_a_stick", new SoulMothOnAStickItem(new FabricItemSettings().maxDamage(35).fireproof().group(SoulStriders.ITEM_GROUP)));

    public SSItems() {}

    public static Item register(String id, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(SoulStriders.MOD_ID, id), item);
    }
}
