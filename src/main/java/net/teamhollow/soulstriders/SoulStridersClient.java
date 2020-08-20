package net.teamhollow.soulstriders;

import static net.teamhollow.soulstriders.SoulStriders.log;

import net.fabricmc.api.ClientModInitializer;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulStridersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        log("Initializing client");

        SSEntities.registerRenderers();

        log("Initialized client");
    }
}
