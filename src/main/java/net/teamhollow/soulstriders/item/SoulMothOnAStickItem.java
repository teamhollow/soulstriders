package net.teamhollow.soulstriders.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.init.SSEntities;

import java.util.function.Consumer;

public class SoulMothOnAStickItem extends Item {
    public SoulMothOnAStickItem() {
        super(
            new Item.Settings()
                .maxDamage(35)
                .fireproof()
                .group(SoulStriders.ITEM_GROUP)
        );
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient) {
            Entity entity = user.getVehicle();
            if (user.hasVehicle() && entity instanceof ItemSteerable && entity.getType() == SSEntities.SOUL_STRIDER) {
                ItemSteerable itemSteerable = (ItemSteerable) entity;
                if (itemSteerable.consumeOnAStickItem()) {
                    itemStack.damage(1, user, (Consumer<LivingEntity>) ((p) -> p.sendToolBreakStatus(hand)));
                    if (itemStack.isEmpty()) {
                        ItemStack itemStack2 = new ItemStack(Items.FISHING_ROD);
                        itemStack2.setTag(itemStack.getTag());
                        return TypedActionResult.success(itemStack2);
                    }

                    return TypedActionResult.success(itemStack);
                }
            }

            user.incrementStat(Stats.USED.getOrCreateStat(this));
        }
        return TypedActionResult.pass(itemStack);
    }
}
