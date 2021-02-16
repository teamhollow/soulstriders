package net.teamhollow.soulstriders;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.init.SSItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoulStriders {
    public static final String MOD_ID = "soulstriders";
    protected static final Logger LOGGER = LogManager.getLogger();
    public static final ItemGroup ITEM_GROUP = new ItemGroup(MOD_ID + ".item_group") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(SSItems.SOUL_MOTH_IN_A_BOTTLE);
        }
    };

    public void preInit() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(new RegistryHandler());
    }

    public void init() {
        SSEntities.init();
    }

    public static ResourceLocation id(String id) {
        int colonIndex = id.indexOf(':');
        if (colonIndex >= 0) {
            return new ResourceLocation(id);
        } else {
            return new ResourceLocation(MOD_ID, id);
        }
    }
}
