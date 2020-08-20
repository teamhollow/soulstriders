package net.teamhollow.soulstriders.entity.soul_strider;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulStriderEntity extends StriderEntity {
    public static final String id = "soul_strider";
    public static final EntityType.Builder<SoulStriderEntity> builder = EntityType.Builder
        .create(SoulStriderEntity::new, SpawnGroup.CREATURE)
        .setDimensions(0.9F, 1.7F)
        .maxTrackingRange(10);
    public static final int[] spawnEggColors = { 10236982, 5065037 };

	public SoulStriderEntity(EntityType<? extends StriderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public StriderEntity createChild(PassiveEntity passiveEntity) {
        return SSEntities.SOUL_STRIDER.create(this.world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return Ingredient.ofItems(Items.SOUL_SOIL).test(stack);
    }
}
