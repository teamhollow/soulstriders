package net.teamhollow.soulstriders.init;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntity;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.item.SoulMothOnAStickItem;

@ObjectHolder(SoulStriders.MOD_ID)
public class SSItems {
    public static final Item SOUL_MOTH_IN_A_BOTTLE = Items.AIR;
    public static final Item SOUL_MOTH_ON_A_STICK = Items.AIR;

    public static void register(IForgeRegistry<Item> registry) {
        registry.registerAll(
            new Item(new Item.Properties().containerItem(Items.GLASS_BOTTLE).group(SoulStriders.ITEM_GROUP).maxStackSize(1))
                .setRegistryName(SoulStriders.id("soul_moth_in_a_bottle")),
            new SoulMothOnAStickItem(new Item.Properties().maxDamage(35).isImmuneToFire().group(SoulStriders.ITEM_GROUP))
                .setRegistryName(SoulStriders.id("soul_moth_on_a_stick")),
            new SpawnEggItem(SSEntities.SOUL_STRIDER, 0x4D494D, 0x8ff1D7, new Item.Properties().group(SoulStriders.ITEM_GROUP))
                .setRegistryName(SoulStriders.id(SoulStriderEntity.id + "_spawn_egg")),
            new SpawnEggItem(SSEntities.WISP, 0x456296, 0x8FF1D7, new Item.Properties().group(SoulStriders.ITEM_GROUP))
                .setRegistryName(SoulStriders.id(WispEntity.id + "_spawn_egg")),
            new SpawnEggItem(SSEntities.SOUL_MOTH, 0xF5E0FF, 0x8FF1D7, new Item.Properties().group(SoulStriders.ITEM_GROUP))
                .setRegistryName(SoulStriders.id(SoulMothEntity.id + "_spawn_egg"))
        );
    }
}
