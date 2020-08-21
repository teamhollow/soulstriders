package net.teamhollow.soulstriders;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.init.SSEntities;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoulStriders implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "soulstriders";
    public static final String MOD_NAME = "Soul Striders";

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
        new Identifier(MOD_ID, "item_group"),
        () -> new ItemStack(Items.STRIDER_SPAWN_EGG)
    );

    @Override
    public void onInitialize() {
        log("Initializing");

        new SSEntities();
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }
    public static void log(String message) {
        log(Level.INFO, message);
    }

	public static Identifier texture(String path) {
		return new Identifier(MOD_ID, "textures/" + path + ".png");
	}
}
