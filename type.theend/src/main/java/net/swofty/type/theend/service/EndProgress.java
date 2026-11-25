package net.swofty.type.theend.service;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobRegistry;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class EndProgress {
    private static final List<ItemType> ENDER_ARMOR = List.of(
            ItemType.END_HELMET,
            ItemType.END_CHESTPLATE,
            ItemType.END_LEGGINGS,
            ItemType.END_BOOTS
    );

    private EndProgress() {
    }

    public static int endermanKills(SkyBlockPlayer player) {
        return List.of("ENDERMAN_42", "ENDERMAN_45", "ENDERMAN_50").stream()
                .map(MobRegistry::getMobById)
                .filter(mob -> mob != null)
                .mapToInt(mob -> player.getBestiaryData().getAmount(mob))
                .sum();
    }

    public static boolean hasKilledFiveEndermen(SkyBlockPlayer player) {
        return endermanKills(player) >= 5;
    }

    public static int enderArmorPieces(SkyBlockPlayer player) {
        int pieces = 0;
        for (ItemType itemType : ENDER_ARMOR) {
            pieces += player.getAmountInInventory(itemType);
            for (SkyBlockItem armor : player.getArmor()) {
                if (armor.getAttributeHandler().getPotentialType() == itemType) {
                    pieces++;
                }
            }
        }
        return pieces;
    }

    public static boolean hasEightEnderArmorPieces(SkyBlockPlayer player) {
        return enderArmorPieces(player) >= 8;
    }

    public static boolean hasSpoken(SkyBlockPlayer player, DatapointToggles.Toggles.ToggleType toggle) {
        return player.getToggles().get(toggle);
    }

    public static void markSpoken(SkyBlockPlayer player, DatapointToggles.Toggles.ToggleType toggle) {
        player.getToggles().set(toggle, true);
    }
}
