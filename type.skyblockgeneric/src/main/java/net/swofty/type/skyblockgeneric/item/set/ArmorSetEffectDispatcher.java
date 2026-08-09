package net.swofty.type.skyblockgeneric.item.set;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Set;

public final class ArmorSetEffectDispatcher {
    private ArmorSetEffectDispatcher() {
    }

    public static float modifyIncomingDamage(SkyBlockPlayer player, SkyBlockMob mob, float damage) {
        Set<ItemType> wornItems = ArmorSetContext.getWornItems(player);
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            ArmorSetContext context = context(registry, player, wornItems);
            if (context.wornPieces() == 0) continue;
            for (ArmorSetEffect effect : registry.getEffects()) {
                if (effect.isActive(context)) damage = effect.modifyIncomingDamage(context, mob, damage);
            }
        }
        return damage;
    }

    public static float modifyOutgoingDamage(SkyBlockPlayer player, SkyBlockMob mob, float damage) {
        Set<ItemType> wornItems = ArmorSetContext.getWornItems(player);
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            ArmorSetContext context = context(registry, player, wornItems);
            if (context.wornPieces() == 0) continue;
            for (ArmorSetEffect effect : registry.getEffects()) {
                if (effect.isActive(context)) damage = effect.modifyOutgoingDamage(context, mob, damage);
            }
        }
        return damage;
    }

    public static int modifyManaCost(SkyBlockPlayer player, int manaCost) {
        Set<ItemType> wornItems = ArmorSetContext.getWornItems(player);
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            ArmorSetContext context = context(registry, player, wornItems);
            if (context.wornPieces() == 0) continue;
            for (ArmorSetEffect effect : registry.getEffects()) {
                if (effect.isActive(context)) manaCost = effect.modifyManaCost(context, manaCost);
            }
        }
        return manaCost;
    }

    public static void onMobKill(SkyBlockPlayer player, SkyBlockMob mob) {
        Set<ItemType> wornItems = ArmorSetContext.getWornItems(player);
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            ArmorSetContext context = context(registry, player, wornItems);
            if (context.wornPieces() == 0) continue;
            for (ArmorSetEffect effect : registry.getEffects()) {
                if (effect.isActive(context)) effect.onMobKill(context, mob);
            }
        }
    }

    private static ArmorSetContext context(ArmorSetRegistry registry, SkyBlockPlayer player, Set<ItemType> wornItems) {
        return new ArmorSetContext(registry, player, wornItems, registry.getWornPieceCount(wornItems));
    }
}
