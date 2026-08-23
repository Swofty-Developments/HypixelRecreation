package net.swofty.type.skywarsgame.perk;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.skywarslobby.perk.SkywarsPerk;
import net.swofty.type.skywarslobby.perk.SkywarsPerkRegistry;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.tinylog.Logger;

import java.util.*;

public class SkywarsPerkHandler {

    public static void applyPerkEffects(SkywarsPlayer player, SkywarsGame game) {
        Set<SkywarsPerk> activePerks = getActivePerks(player, game.getGameType());

        if (activePerks.isEmpty()) {
            return;
        }

        Set<String> perkIds = new HashSet<>();
        for (SkywarsPerk perk : activePerks) {
            perkIds.add(perk.getId());
        }
        player.setActivePerks(perkIds);

        for (SkywarsPerk perk : activePerks) {
            if (perk.hasStartingItems()) {
                for (ItemStack item : perk.getStartingItems()) {
                    player.getInventory().addItemStack(item);
                }
            }
        }

        for (SkywarsPerk perk : activePerks) {
            applyStartingEffect(player, perk, game.getGameType());
        }

        Logger.debug("Applied {} perks for player {}", activePerks.size(), player.getUsername());
    }

    public static Set<SkywarsPerk> getActivePerks(SkywarsPlayer player, SkywarsGameType gameType) {
        Set<SkywarsPerk> activePerks = new HashSet<>();

        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) {
            return activePerks;
        }

        DatapointSkywarsUnlocks unlocksDP = handler.get(
                SkywarsDataHandler.Data.UNLOCKS,
                DatapointSkywarsUnlocks.class);
        DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = unlocksDP.getValue();

        String mode = gameType.getModeString();

        for (SkywarsPerk globalPerk : SkywarsPerkRegistry.getGlobalPerks()) {
            if (globalPerk.isAvailableFor(mode)) {
                activePerks.add(globalPerk);
            }
        }

        if (gameType.isInsane()) {
            Set<String> enabledPerkIds = unlocks.getEnabledInsanePerks(mode);
            for (String perkId : enabledPerkIds) {
                SkywarsPerk perk = SkywarsPerkRegistry.getPerk(perkId);
                if (perk != null && perk.isAvailableFor(mode) && !perk.isGlobal()) {
                    activePerks.add(perk);
                }
            }
        } else {
            List<String> selectedPerkIds = unlocks.getSelectedPerksForMode(mode);
            for (String perkId : selectedPerkIds) {
                if (perkId == null || perkId.isEmpty()) continue;
                SkywarsPerk perk = SkywarsPerkRegistry.getPerk(perkId);
                if (perk != null && perk.isAvailableFor(mode) && !perk.isGlobal()) {
                    activePerks.add(perk);
                }
            }
        }

        return activePerks;
    }

    private static void applyStartingEffect(SkywarsPlayer player, SkywarsPerk perk, SkywarsGameType gameType) {
        String perkId = perk.getId();

        switch (perkId) {
            case "resistance_boost" -> player.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 1, 15 * 20));
            case "fat" -> player.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 0, 20 * 20));
            case "speed_boost" -> player.addEffect(new Potion(PotionEffect.HASTE, (byte) 1, 300 * 20));
        }
    }

    public static void applyKillEffects(SkywarsPlayer killer, SkywarsPlayer victim, SkywarsGame game) {
        Set<String> activePerks = killer.getActivePerks();
        if (activePerks == null || activePerks.isEmpty()) {
            return;
        }

        boolean isSolo = game.getGameType().getTeamSize() == 1;

        for (String perkId : activePerks) {
            switch (perkId) {
                case "bulldozer" -> {
                    int duration = isSolo ? 5 : 2;
                    killer.addEffect(new Potion(PotionEffect.STRENGTH, (byte) 0, duration * 20));
                }
                case "juggernaut" -> killer.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 0, 10 * 20));
                case "savior" -> killer.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 0, 7 * 20));
                case "knowledge" -> killer.setLevel(killer.getLevel() + 3);
                case "lucky_charm" -> {
                    if (Math.random() < 0.30) {
                        killer.getInventory().addItemStack(ItemStack.of(Material.GOLDEN_APPLE, 1));
                        killer.sendMessage("<6>Lucky Charm! You found a Golden Apple!");
                    }
                }
            }
        }
    }

    public static void applyVoidKillEffects(SkywarsPlayer killer, SkywarsPlayer victim, SkywarsGame game) {
        Set<String> activePerks = killer.getActivePerks();
        if (activePerks == null || activePerks.isEmpty()) {
            return;
        }

        for (String perkId : activePerks) {
            if ("black_magic".equals(perkId)) {
                if (Math.random() < 0.30) {
                    killer.getInventory().addItemStack(ItemStack.of(Material.ENDER_PEARL, 1));
                    killer.sendMessage("<5>Black Magic! You found an Ender Pearl!");
                }
            }
        }
    }

    public static boolean hasPerk(SkywarsPlayer player, String perkId) {
        Set<String> activePerks = player.getActivePerks();
        return activePerks != null && activePerks.contains(perkId);
    }

    public static double getPerkChance(SkywarsPlayer player, String perkId) {
        if (!hasPerk(player, perkId)) {
            return 0;
        }

        return switch (perkId) {
            case "bridger" -> 0.50;
            case "mining_expertise" -> 0.20;
            case "annoy_o_mite" -> 0.10;
            case "arrow_recovery" -> 0.50;
            case "blazing_arrows" -> 0.15;
            case "environmental_expert" -> 0.50;
            case "lucky_charm" -> 0.30;
            case "frost" -> 0.40;
            case "necromancer" -> 0.16;
            case "robbery" -> 0.20;
            case "black_magic" -> 0.30;
            case "diamond_in_the_rough" -> 0.05;
            case "diamondpiercer" -> 0.20;
            default -> 0;
        };
    }
}
