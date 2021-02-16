package net.teamhollow.soulstriders.item;

import net.minecraft.item.Item;
import net.minecraft.item.OnAStickItem;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulMothOnAStickItem extends OnAStickItem<SoulStriderEntity> {
    public SoulMothOnAStickItem(Item.Properties settings) {
        super(settings, SSEntities.SOUL_STRIDER, 1);
    }
}
