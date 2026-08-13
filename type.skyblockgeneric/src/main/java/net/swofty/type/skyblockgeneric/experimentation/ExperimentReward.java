package net.swofty.type.skyblockgeneric.experimentation;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.loottable.BossDropRarity;
import net.swofty.type.skyblockgeneric.loottable.LootAnnouncement;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterLoot;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterReward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum ExperimentReward implements RNGMeterReward {
    EXPERIENCE("<b>Enchanting XP", ItemType.EXPERIENCE_BOTTLE, 0),
    EXPERIENCE_BOTTLE("<f>Experience Bottle", ItemType.EXPERIENCE_BOTTLE, 0),
    GRAND_EXPERIENCE_BOTTLE("<a>Grand Experience Bottle", ItemType.GRAND_EXP_BOTTLE, 0),

    TITANIC_EXPERIENCE_BOTTLE("<9>Titanic Experience Bottle", ItemType.TITANIC_EXP_BOTTLE, 15_000),
    EXPERIMENT_THE_FISH("<c>Experiment the Fish", ItemType.EXPERIMENT_THE_FISH, 50_000),
    METAPHYSICAL_SERUM("<5>Metaphysical Serum", ItemType.METAPHYSICAL_SERUM, 50_000),
    SCAVENGER_V("<a>Scavenger V", EnchantmentType.SCAVENGER, 5, 150_000),
    SHARPNESS_VI("<9>Sharpness VI", EnchantmentType.SHARPNESS, 6, 150_000),
    LIFE_STEAL_IV("<f>Life Steal IV", EnchantmentType.LIFE_STEAL, 4, 150_000),
    POWER_VI("<9>Power VI", EnchantmentType.POWER, 6, 150_000),
    ENDER_SLAYER_VI("<9>Ender Slayer VI", EnchantmentType.ENDER_SLAYER, 6, 150_000),
    THUNDERBOLT_VI("<9>Thunderbolt VI", EnchantmentType.THUNDERBOLT, 6, 150_000),
    GROWTH_VI("<9>Growth VI", EnchantmentType.GROWTH, 6, 150_000),
    CHANCE_IV("<f>Chance IV", EnchantmentType.CHANCE, 4, 150_000),
    BLAST_PROTECTION_VI("<9>Blast Protection VI", EnchantmentType.BLAST_PROTECTION, 6, 150_000),
    RESPITE_III("<f>Respite III", EnchantmentType.RESPITE, 3, 150_000),
    VENOMOUS_VI("<9>Venomous VI", EnchantmentType.VENOMOUS, 6, 150_000),
    PROJECTILE_PROTECTION_VI("<9>Projectile Protection VI", EnchantmentType.PROJECTILE_PROTECTION, 6, 150_000),
    GUARDIAN_PET("<7>[Lvl 1] <6>Guardian", ItemType.GUARDIAN_PET, 150_000),
    FIRE_PROTECTION_VI("<9>Fire Protection VI", EnchantmentType.FIRE_PROTECTION, 6, 150_000),
    WOODSPLITTER_VI("<9>Woodsplitter VI", EnchantmentType.WOODSPLITTER, 6, 150_000),
    GIANT_KILLER_VI("<9>Giant Killer VI", EnchantmentType.GIANT_KILLER, 6, 150_000),
    DRAIN_IV("<f>Drain IV", EnchantmentType.DRAIN, 4, 150_000),
    PROTECTION_VI("<9>Protection VI", EnchantmentType.PROTECTION, 6, 150_000),
    TITAN_KILLER_VI("<9>Titan Killer VI", EnchantmentType.TITAN_KILLER, 6, 150_000),

    A_BEGINNERS_GUIDE_TO_PESTHUNTING("<6>A Beginner's Guide to Pesthunting", ItemType.A_BEGINNERS_GUIDE_TO_PESTHUNTING, 500_000),
    SEVERED_PINCER("<6>Severed Pincer", ItemType.SEVERED_PINCER, 500_000),
    CHANCE_V("<a>Chance V", EnchantmentType.CHANCE, 5, 500_000),
    THUNDERLORD_VII("<5>Thunderlord VII", EnchantmentType.THUNDERLORD, 7, 500_000),
    ENSNARED_SNAIL("<6>Ensnared Snail", ItemType.ENSNARED_SNAIL, 500_000),
    GIANT_KILLER_VII("<5>Giant Killer VII", EnchantmentType.GIANT_KILLER, 7, 500_000),

    GRAVITY_VI("<9>Gravity VI", EnchantmentType.GRAVITY, 6, 500_000),
    GOLDEN_BOUNTY("<6>Golden Bounty", ItemType.GOLDEN_BOUNTY, 500_000),
    SEVERED_HAND("<6>Severed Hand", ItemType.SEVERED_HAND, 500_000),
    CRITICAL_VII("<5>Critical VII", EnchantmentType.CRITICAL, 7, 500_000),
    VIBRANT_CORAL("<6>Vibrant Coral", ItemType.VIBRANT_CORAL, 500_000),
    SNIPE_IV("<f>Snipe IV", EnchantmentType.SNIPE, 4, 500_000),
    LIFE_STEAL_V("<a>Life Steal V", EnchantmentType.LIFE_STEAL, 5, 500_000),
    GOLD_BOTTLE_CAP("<6>Gold Bottle Cap", ItemType.GOLD_BOTTLE_CAP, 500_000),
    LOOTING_V("<a>Looting V", EnchantmentType.LOOTING, 5, 500_000),
    FIRST_STRIKE_V("<a>First Strike V", EnchantmentType.FIRST_STRIKE, 5, 500_000),
    FIRE_PROTECTION_VII("<5>Fire Protection VII", EnchantmentType.FIRE_PROTECTION, 7, 500_000),
    THUNDERBOLT_VII("<5>Thunderbolt VII", EnchantmentType.THUNDERBOLT, 7, 500_000),
    CUBISM_VI("<9>Cubism VI", EnchantmentType.CUBISM, 6, 500_000),
    TRIPLE_STRIKE_V("<a>Triple-Strike V", EnchantmentType.TRIPLE_STRIKE, 5, 500_000),
    CHAIN_OF_THE_END_TIMES("<6>Chain of the End Times", ItemType.CHAIN_OF_THE_END_TIMES, 500_000),
    DRAIN_V("<a>Drain V", EnchantmentType.DRAIN, 5, 500_000),
    FATEFUL_STINGER("<6>Fateful Stinger", ItemType.FATEFUL_STINGER, 500_000),
    BLAST_PROTECTION_VII("<5>Blast Protection VII", EnchantmentType.BLAST_PROTECTION, 7, 500_000),
    CLEAVE_VI("<9>Cleave VI", EnchantmentType.CLEAVE, 6, 500_000),
    OCTOPUS_TENDRIL("<6>Octopus Tendril", ItemType.OCTOPUS_TENDRIL, 500_000),
    TITAN_KILLER_VII("<5>Titan Killer VII", EnchantmentType.TITAN_KILLER, 7, 500_000),
    LUCK_VII("<5>Luck VII", EnchantmentType.LUCK, 7, 500_000),
    END_STONE_IDOL("<6>End Stone Idol", ItemType.END_STONE_IDOL, 500_000),
    EXECUTE_VI("<9>Execute VI", EnchantmentType.EXECUTE, 6, 500_000),
    POWER_VII("<5>Power VII", EnchantmentType.POWER, 7, 500_000),
    TROUBLED_BUBBLE("<6>Troubled Bubble", ItemType.TROUBLED_BUBBLE, 500_000),
    PROJECTILE_PROTECTION_VII("<5>Projectile Protection VII", EnchantmentType.PROJECTILE_PROTECTION, 7, 500_000),
    GROWTH_VII("<5>Growth VII", EnchantmentType.GROWTH, 7, 500_000),

    SHARPNESS_VII("<5>Sharpness VII", EnchantmentType.SHARPNESS, 7, 500_000),
    PROTECTION_VII("<5>Protection VII", EnchantmentType.PROTECTION, 7, 500_000),
    PROSECUTE_VI("<9>Prosecute VI", EnchantmentType.PROSECUTE, 6, 500_000),
    NADESHIKO_DYE("<d>Nadeshiko Dye", ItemType.NADESHIKO_DYE, 2_500_000);

    private final String displayName;
    private final Material material;
    private final Supplier<SkyBlockItem> itemSupplier;
    private final int meterRequirement;
    private final boolean ultraRareBook;
    private static final Map<ExperimentReward, RNGMeterLoot> LOOT = createLootRegistry();

    ExperimentReward(String displayName, ItemType itemType, int meterRequirement) {
        this(displayName, itemType.material, () -> new SkyBlockItem(itemType), meterRequirement, false);
    }

    ExperimentReward(String displayName, EnchantmentType enchantmentType, int enchantmentLevel, int meterRequirement) {
        this(displayName, ItemType.ENCHANTED_BOOK.material,
                () -> enchantedBook(enchantmentType, enchantmentLevel), meterRequirement, meterRequirement >= 500_000);
    }

    ExperimentReward(String displayName, Material material, Supplier<SkyBlockItem> itemSupplier, int meterRequirement) {
        this(displayName, material, itemSupplier, meterRequirement, false);
    }

    ExperimentReward(String displayName, Material material, Supplier<SkyBlockItem> itemSupplier,
                     int meterRequirement, boolean ultraRareBook) {
        this.displayName = displayName;
        this.material = material;
        this.itemSupplier = itemSupplier;
        this.meterRequirement = meterRequirement;
        this.ultraRareBook = ultraRareBook;
    }

    public String displayName() {
        return displayName;
    }

    public String id() {
        return name();
    }

    public double requiredXp() {
        return meterRequirement;
    }

    public Material material() {
        return material;
    }

    public int meterRequirement() {
        return meterRequirement;
    }

    public boolean isUltraRareBook() {
        return ultraRareBook;
    }

    public SkyBlockItem createItem() {
        return createItem(null);
    }

    public SkyBlockItem createItem(Rarity rarity) {
        SkyBlockItem item = itemSupplier.get();
        if (rarity != null) item.getAttributeHandler().setRarity(rarity);
        return item;
    }

    public ItemStack.Builder displayItem(SkyBlockPlayer player) {
        return PlayerItemUpdater.playerUpdate(player, createItem().getItemStack());
    }

    public void give(SkyBlockPlayer player) {
        give(player, 1);
    }

    public void give(SkyBlockPlayer player, int amount) {
        give(player, amount, null);
    }

    public void give(SkyBlockPlayer player, int amount, Rarity rarity) {
        if (amount < 1) throw new IllegalArgumentException("Reward amount must be positive");
        if (this == EXPERIENCE) {
            player.getSkills().increase(player, net.swofty.type.skyblockgeneric.skill.SkillCategories.ENCHANTING,
                    (double) amount);
            player.sendMessage("<a>Experiment reward: <3>" + amount + " Enchanting XP<a>!");
            return;
        }
        SkyBlockItem item = createItem(rarity);
        item.setAmount(amount);
        player.addAndUpdateItem(item);
        player.sendMessage("<a>Experiment reward: " + displayName + "<a>!");
        loot().announcement().announce(player, Text.of(displayName));
    }

    public boolean grantsSkillExperience() {
        return this == EXPERIENCE;
    }

    public RNGMeterLoot loot() {
        return LOOT.get(this);
    }

    private RNGMeterLoot createLoot() {
        BossDropRarity rarity;
        double chance;
        LootAnnouncement announcement;
        switch (this) {
            case EXPERIENCE, EXPERIENCE_BOTTLE, GRAND_EXPERIENCE_BOTTLE -> {
                rarity = BossDropRarity.COMMON;
                chance = 100;
                announcement = LootAnnouncement.NONE;
            }
            case TITANIC_EXPERIENCE_BOTTLE, EXPERIMENT_THE_FISH, METAPHYSICAL_SERUM -> {
                rarity = BossDropRarity.EXTRAORDINARY;
                chance = 1.1236;
                announcement = LootAnnouncement.NONE;
            }
            case SCAVENGER_V, SHARPNESS_VI, LIFE_STEAL_IV, POWER_VI, ENDER_SLAYER_VI,
                 THUNDERBOLT_VI, GROWTH_VI, CHANCE_IV, BLAST_PROTECTION_VI, RESPITE_III,
                 VENOMOUS_VI, PROJECTILE_PROTECTION_VI, FIRE_PROTECTION_VI, WOODSPLITTER_VI,
                 GIANT_KILLER_VI, DRAIN_IV, PROTECTION_VI, TITAN_KILLER_VI -> {
                rarity = BossDropRarity.RARE;
                chance = 5.618;
                announcement = LootAnnouncement.RARE;
            }
            case NADESHIKO_DYE -> {
                rarity = BossDropRarity.RNGESUS_INCARNATE;
                chance = 0.012;
                announcement = LootAnnouncement.INSANE;
            }
            case GUARDIAN_PET, A_BEGINNERS_GUIDE_TO_PESTHUNTING, SEVERED_PINCER, CHANCE_V,
                 THUNDERLORD_VII, ENSNARED_SNAIL, GIANT_KILLER_VII, GRAVITY_VI, GOLDEN_BOUNTY,
                 SEVERED_HAND, CRITICAL_VII, VIBRANT_CORAL, SNIPE_IV, LIFE_STEAL_V,
                 GOLD_BOTTLE_CAP, LOOTING_V, FIRST_STRIKE_V, FIRE_PROTECTION_VII,
                 THUNDERBOLT_VII, CUBISM_VI, TRIPLE_STRIKE_V, CHAIN_OF_THE_END_TIMES, DRAIN_V,
                 FATEFUL_STINGER, BLAST_PROTECTION_VII, CLEAVE_VI, OCTOPUS_TENDRIL,
                 TITAN_KILLER_VII, LUCK_VII, END_STONE_IDOL, EXECUTE_VI, POWER_VII,
                 TROUBLED_BUBBLE, PROJECTILE_PROTECTION_VII, GROWTH_VII, SHARPNESS_VII,
                 PROTECTION_VII, PROSECUTE_VI -> {
                rarity = BossDropRarity.RNGESUS_INCARNATE;
                chance = 0.027;
                announcement = LootAnnouncement.INSANE;
            }
            default -> throw new IllegalStateException("Missing loot definition for " + name());
        }
        return new RNGMeterLoot(Key.key("skyblock", "experimentation/" + name().toLowerCase()),
                rarity, chance, announcement);
    }

    private static Map<ExperimentReward, RNGMeterLoot> createLootRegistry() {
        Map<ExperimentReward, RNGMeterLoot> loot = new EnumMap<>(ExperimentReward.class);
        for (ExperimentReward reward : values()) loot.put(reward, reward.createLoot());
        return Map.copyOf(loot);
    }

    public static ExperimentReward fromName(String name) {
        return Arrays.stream(values()).filter(reward -> reward.name().equalsIgnoreCase(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown experimentation reward: " + name));
    }

    private static SkyBlockItem enchantedBook(EnchantmentType type, int level) {
        SkyBlockItem item = new SkyBlockItem(ItemType.ENCHANTED_BOOK);
        item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(type, level));
        return item;
    }

}
