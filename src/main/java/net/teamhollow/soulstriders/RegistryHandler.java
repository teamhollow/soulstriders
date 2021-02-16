package net.teamhollow.soulstriders;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.teamhollow.soulstriders.init.SSBlocks;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.init.SSItems;

public class RegistryHandler {
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        SSBlocks.register(event.getRegistry());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        SSItems.register(event.getRegistry());
        SSBlocks.registerBlockItems(event.getRegistry());
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        SSEntities.register(event.getRegistry());
    }
}
