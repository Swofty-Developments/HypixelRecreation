package net.swofty.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public enum CustomLevelAward {
    ACCESS_TO_COMMUNITY_SHOP(ItemStacks.head("4e495b103ddd47701449ad7a34d908e8d2f08e0bd9476653d433c3bfc7c1b055", Text.empty(), List.of()),
            "<b>Access to Community Shop"),
    ACCESS_TO_GARDEN(ItemStacks.head("4778b434a258f7991825cabc965a56403c4d772e9628ce60164927e94b79d17", Text.empty(), List.of()),
            "<2>Access to the Garden"),
    ACCESS_TO_WARDROBE(ItemStack.builder(Material.LEATHER_CHESTPLATE),
            "<a>Access to Wardrobe"),
    AUTO_PICKUP_BLOCK_AND_MOB_DROPS(ItemStack.builder(Material.DIAMOND_SWORD),
            "<a>Auto-Pickup Block and Mob Drops"),
    ACCESS_TO_BAZAAR(ItemStacks.head("c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7", Text.empty(), List.of()),
            "<6>Access to Bazaar"),
    ACCESS_TO_WIZARD_PORTAL(ItemStacks.head("838564e28aba98301dbda5fafd86d1da4e2eaeef12ea94dcf440b883e559311c", Text.empty(), List.of()),
            "<d>Access to Wizard Portal"),
    DAILY_COINS_TRADE_LIMIT_OF_1B(ItemStacks.head("740d6e362bc7eee4f911dbd0446307e7458d1050d09aee538ebcb0273cf75742", Text.empty(), List.of()),
            "<a>Daily Coins Trading Limit of 1B"),
    DAILY_COINS_TRADE_LIMIT_OF_10B(ItemStacks.head("c43f12c8369f9c3888a45aaf6d7761578402b4241958f7d4ae4eceb56a867d2a", Text.empty(), List.of()),
            "<a>Daily Coins Trading Limit of 10B")
    ;
    private static final Map<Integer, List<CustomLevelAward>> CACHE = new HashMap<>();

    private final ItemStack.Builder item;
    @Getter(lombok.AccessLevel.NONE)
    private final String display;

    CustomLevelAward(ItemStack.Builder item, String display) {
        this.item = item;
        this.display = display;
    }

    public Text getDisplay() {
        return Text.of(display);
    }

    public static int getTotalLevelAwards() {
        return CACHE.values().stream().mapToInt(List::size).sum();
    }

    public static Map<CustomLevelAward, Integer> getAwards() {
        Map<CustomLevelAward, Integer> awards = new HashMap<>();
        for (Map.Entry<Integer, List<CustomLevelAward>> entry : CACHE.entrySet()) {
            for (CustomLevelAward award : entry.getValue()) {
                awards.put(award, entry.getKey());
            }
        }
        return awards;
    }

    public static Integer getLevel(CustomLevelAward award) {
        for (Map.Entry<Integer, List<CustomLevelAward>> entry : CACHE.entrySet()) {
            if (entry.getValue().contains(award)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map.Entry<Integer, List<CustomLevelAward>> getNextReward(int level) {
        int nextLevel = level + 1;
        int amountToSearch = 500;
        for (int i = nextLevel; i <= amountToSearch; i++) {
            if (CACHE.containsKey(i)) {
                return Map.entry(i, CACHE.get(i));
            }
        }
        return null;
    }

    public static void addToCache(int level, CustomLevelAward award) {
        if (!CACHE.containsKey(level)) {
            CACHE.put(level, new ArrayList<>(List.of(award)));
        } else {
            CACHE.get(level).add(award);
        }
    }

    public static List<CustomLevelAward> getFromLevel(int level) {
        // Include current level and any previous levels, so we can get all awards up to the current level
        List<CustomLevelAward> awards = new ArrayList<>();
        for (int i = 1; i <= level; i++) {
            if (CACHE.containsKey(i)) {
                awards.addAll(CACHE.get(i));
            }
        }
        return awards;
    }
}
