package net.teamhollow.soulstriders.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.block.SoulStriderBulbBlock;

@ObjectHolder(SoulStriders.MOD_ID)
public class SSBlocks {
    public static final Block SOUL_STRIDER_BULB = Blocks.AIR;

    public static void register(IForgeRegistry<Block> registry) {
        registry.registerAll(
            new SoulStriderBulbBlock(AbstractBlock.Properties.create(Material.ROCK, MaterialColor.STONE).tickRandomly().hardnessAndResistance(0.5F).notSolid().sound(SoundType.STONE))
                .setRegistryName(SoulStriders.id(SoulStriderBulbBlock.id))
        );
    }

    public static void registerBlockItems(IForgeRegistry<Item> registry) {
        registry.registerAll(
            new BlockItem(SOUL_STRIDER_BULB, new Item.Properties().group(ItemGroup.BREWING))
                .setRegistryName(SoulStriders.id(SoulStriderBulbBlock.id))
        );
    }
}
