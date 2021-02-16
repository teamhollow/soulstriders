package net.teamhollow.soulstriders;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SoulStriders.MOD_ID)
public class SoulStridersMod {
    public static final SoulStriders INSTANCE = DistExecutor.safeRunForDist(
        () -> SoulStridersClient::new,
        () -> SoulStriders::new
    );

    public SoulStridersMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        INSTANCE.preInit();
    }

    private void setup(FMLCommonSetupEvent event) {
        INSTANCE.init();
    }
}
