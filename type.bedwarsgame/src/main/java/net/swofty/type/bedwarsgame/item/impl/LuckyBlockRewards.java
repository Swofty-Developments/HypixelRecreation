package net.swofty.type.bedwarsgame.item.impl;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.item.impl.lucky.*;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStacks;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class LuckyBlockRewards {
    private LuckyBlockRewards() {
    }

    public static void apply(BedWarsPlayer player, Pos openedAt, LuckyBlockTier tier) {
        List<LuckyReward> rewards = rewards(tier);
        LuckyReward reward = rewards.get(ThreadLocalRandom.current().nextInt(rewards.size()));
        player.sendMessage("<6>Lucky Block! <e>{}", reward.name());
        reward.apply(player, openedAt);
    }

    private static List<LuckyReward> rewards(LuckyBlockTier tier) {
        return switch (tier) {
            case NORMAL -> normalRewards();
            case PROMISING -> promisingRewards();
            case FORTUNATE -> fortunateRewards();
            case OFFENSIVE -> offensiveRewards();
            case MIRACLE -> miracleRewards();
        };
    }

    private static List<LuckyReward> normalRewards() {
        return List.of(
            item("Bridge Egg", () -> TypeBedWarsGameLoader.getItemHandler().getItem("bridge_egg").getItemStack()),
            item("Rainbow Wool", ItemStack.of(Material.WHITE_WOOL, 64)),
            item("Water Balloons", ItemStack.of(Material.SPLASH_POTION, 8)),
            item("Block of Emerald", ItemStack.of(Material.EMERALD_BLOCK)),
            item("Flint and Steel", named(Material.FLINT_AND_STEEL, "<a>Flint and Steel")),
            special("Bed Pointer", Material.COMPASS, "bed_pointer", 0, "<7>Find the nearest enemy bed."),
            special("Chicken Bomb", Material.EGG, "chicken_bomb", 1, "<7>Spawn a burst of distracting chickens."),
            item("Chicken Hat", LuckyEquipmentEffects.equipment("Chicken Hat", Material.LEATHER_HELMET, "chicken_hat", "<7>Looks at enemies and knocks them back.")),
            special("Explosive Chicken", Material.CHICKEN_SPAWN_EGG, "explosive_chicken", 1, "<7>Deploy an explosive chicken."),
            special("Hot Potato", Material.POTATO, "hot_potato", 1, "<7>Explodes after a short delay."),
            item("Hot Head", LuckyEquipmentEffects.equipment("Hot Head", Material.LEATHER_HELMET, "hot_head", "<7>Burns bright while protecting from fire.")),
            special("Lava Rune", Material.BLAZE_POWDER, "lava_rune", 1, "<7>Places temporary lava."),
            new RandomTeamUpgradeLuckyReward(),
            trap("Arrow Rain", "Arrow Rain"),
            trap("Damage Trap", "Damage"),
            trap("Slow Trap", "Slow"),
            new EffectLuckyReward("Instant Heal", PotionEffect.INSTANT_HEALTH, 1, 1),
            new PermanentToolLuckyReward("Permanent Axe", true),
            new PermanentToolLuckyReward("Permanent Pickaxe", false),
            mob("Skeleton", EntityType.SKELETON, 1, false),
            item("Slime Boots", LuckyEquipmentEffects.equipment("Slime Boots", Material.LEATHER_BOOTS, "slime_boots", "<7>Negates fall damage and bounces.")),
            mob("Slimes", EntityType.SLIME, 4, true),
            special("Sleepinator", Material.BLAZE_ROD, "sleepinator", 1, "<7>Blind and slow an enemy in front of you.")
        );
    }

    private static List<LuckyReward> promisingRewards() {
        return List.of(
            new ArmorSpikesLuckyReward(),
            item("Cute Pants", ItemStack.of(Material.DIAMOND_LEGGINGS)),
            item("Exodus", LuckyEquipmentEffects.equipment("Exodus", Material.GOLDEN_HELMET, "exodus", "<7>Regenerates you while worn.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, 2))),
            mob("Ghast", EntityType.GHAST, 1, true),
            item("Scythe", LuckyCombatEffects.weapon("Scythe", Material.IRON_HOE, "scythe", "<7>Deals extra damage on hit.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 2))),
            new CopiedInventoryLuckyReward(),
            item("Squid Boots", LuckyEquipmentEffects.equipment("Squid Boots", Material.LEATHER_BOOTS, "squid_boots", "<7>Leaves water under your feet.")),
            item("Disguise", named(Material.LEATHER_CHESTPLATE, "<a>Disguise")),
            special("Instant Barrier", Material.OBSIDIAN, "instant_barrier", 1, "<7>Creates a temporary obsidian wall."),
            special("Stasis", Material.CLOCK, "stasis", 1, "<7>Immobilizes nearby enemies."),
            new EffectLuckyReward("Sugar Cookie", PotionEffect.SPEED, 2, 200),
            special("Vampire Blood", Material.RED_DYE, "vampire_blood", 1, "<7>Gain regeneration and strength."),
            special("Time Warp Pearl", Material.ENDER_PEARL, "time_warp_pearl", 1, "<7>Return to this spot after 5 seconds.")
        );
    }

    private static List<LuckyReward> fortunateRewards() {
        return List.of(
            new BedCoverLuckyReward(),
            item("End Stone", ItemStack.of(Material.END_STONE, 12)),
            item("Obsidian", ItemStack.of(Material.OBSIDIAN, 4)),
            trap("Freeze Trap", "Freeze"),
            mob("Giant", EntityType.GIANT, 1, true),
            special("Grappling Hook", Material.FISHING_ROD, "grappling_hook", 3, "<7>Pull yourself forward."),
            special("Gravity Gun", Material.STICK, "gravity_gun", 5, "<7>Launch an enemy away."),
            item("Heat-Resistant Boots", LuckyEquipmentEffects.equipment("Heat-Resistant Boots", Material.LEATHER_BOOTS, "heat_boots", "<7>Grants fire resistance.")),
            item("Sharp Spoon", LuckyCombatEffects.weapon("Sharp Spoon", Material.IRON_SHOVEL, "sharp_spoon", "<7>A surprisingly sharp spoon.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 2))),
            special("Teleport Beam", Material.BEACON, "teleport_beam", 1, "<7>Teleport back to your base."),
            item("Mystery Meat", ItemStack.of(Material.COOKED_BEEF, 3)),
            item("Frog Helmet", LuckyEquipmentEffects.equipment("Frog Helmet", Material.TURTLE_HELMET, "frog_helmet", "<7>Jump high and fall safely.")),
            new RouletteBlitzLuckyReward()
        );
    }

    private static List<LuckyReward> offensiveRewards() {
        return List.of(
            item("Angel of Death's Sword", LuckyCombatEffects.weapon("Angel of Death's Sword", Material.DIAMOND_SWORD, "angel_sword", "<7>One use. Massive damage and knockback.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 4))),
            mob("Baby Zombie", EntityType.ZOMBIE, 1, true),
            item("Chicken Bow", named(Material.BOW, "<a>Chicken Bow")),
            item("Cobweb", ItemStack.of(Material.COBWEB, 5)),
            special("Dreadlord Skull", Material.WITHER_SKELETON_SKULL, "dreadlord_skull", 1, "<7>Blast an enemy in front of you."),
            trap("Poison Trap", "Poison"),
            item("Rush Pearl", named(Material.ENDER_PEARL, "<a>Rush Pearl")),
            special("Jedi Force", Material.FEATHER, "jedi_force", 3, "<7>Push enemies from range."),
            item("Knockback Slimeball", LuckyCombatEffects.weapon("Knockback Slimeball", Material.SLIME_BALL, "knockback_slimeball", "<7>One use. Huge knockback.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.KNOCKBACK, 2))),
            mob("Snowman", EntityType.SNOW_GOLEM, 1, false),
            new InstantTrapQueueLuckyReward(),
            new InstantWeaponUpgradeLuckyReward(),
            item("Sword of Justice", LuckyCombatEffects.weapon("Sword of Justice", Material.IRON_SWORD, "sword_justice", "<7>Heals you on hit with a cooldown.")
                .with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.SHARPNESS, 1)))
        );
    }

    private static List<LuckyReward> miracleRewards() {
        return List.of(
            new BlockLuckyReward("Bedrock", Block.BEDROCK),
            item("Obsidian", ItemStack.of(Material.OBSIDIAN, 6)),
            item("OP Diamond Helmet", named(Material.DIAMOND_HELMET, "<6>OP Diamond Helmet").with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, 5))),
            item("OP Iron Helmet", named(Material.IRON_HELMET, "<6>OP Iron Helmet").with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.PROTECTION, 4))),
            item("Placeable Bed", named(Material.RED_BED, "<a>Placeable Bed")),
            mob("Placeable Wither", EntityType.WITHER, 1, false),
            item("Incredible Chest", ItemStack.of(Material.CHEST)),
            item("Jerry", ItemStack.of(Material.VILLAGER_SPAWN_EGG)),
            special("Magic Toy Stick", Material.STICK, "magic_toy_stick", 3, "<7>Break nearby player-placed blocks."),
            special("Nuke Targeting Device", Material.TARGET, "nuke", 1, "<7>Call a delayed explosion where you aim."),
            item("Spicy Sword", named(Material.DIAMOND_SWORD, "<c>Spicy Sword").with(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY.with(Enchantment.FIRE_ASPECT, 1))),
            special("Super Star", Material.NETHER_STAR, "super_star", 1, "<7>Become nearly invincible."),
            special("Void Maker", Material.BARRIER, "void_maker", 3, "<7>Remove obsidian or bedrock.")
        );
    }

    private static LuckyReward item(String name, ItemStack item) {
        return new ItemLuckyReward(name, item);
    }

    private static LuckyReward item(String name, java.util.function.Supplier<ItemStack> itemSupplier) {
        return new ItemLuckyReward(name, itemSupplier);
    }

    private static LuckyReward special(String name, Material material, String action, int uses, String... lore) {
        return new SpecialItemLuckyReward(name, material, action, uses, lore);
    }

    private static LuckyReward trap(String name, String trap) {
        return new TrapLuckyReward(name, trap);
    }

    private static LuckyReward mob(String name, EntityType type, int count, boolean hostile) {
        return new MobLuckyReward(name, type, count, hostile);
    }

    private static ItemStack named(Material material, String markup) {
        return ItemStacks.named(material, markup).build();
    }
}
