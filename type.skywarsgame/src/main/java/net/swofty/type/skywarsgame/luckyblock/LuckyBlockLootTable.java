package net.swofty.type.skywarsgame.luckyblock;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.text.Text;
import net.swofty.type.skywarsgame.luckyblock.effects.LuckyBlockEnvironmentEffect;
import net.swofty.type.skywarsgame.luckyblock.items.armor.*;
import net.swofty.type.skywarsgame.luckyblock.items.consumables.AbsorptionHearts;
import net.swofty.type.skywarsgame.luckyblock.items.consumables.Cornucopia;
import net.swofty.type.skywarsgame.luckyblock.items.consumables.OneUpMushroom;
import net.swofty.type.skywarsgame.luckyblock.items.consumables.PlusHealthItem;
import net.swofty.type.skywarsgame.luckyblock.items.consumables.SuperStar;
import net.swofty.type.skywarsgame.luckyblock.items.usables.BridgeEggs;
import net.swofty.type.skywarsgame.luckyblock.items.usables.DevilsContract;
import net.swofty.type.skywarsgame.luckyblock.items.usables.Fireball;
import net.swofty.type.skywarsgame.luckyblock.items.usables.JediForce;
import net.swofty.type.skywarsgame.luckyblock.items.usables.SuperDie;
import net.swofty.type.skywarsgame.luckyblock.items.usables.ThrowableTNT;
import net.swofty.type.skywarsgame.luckyblock.items.usables.VoidCharm;
import net.swofty.type.skywarsgame.luckyblock.items.usables.WaterBalloon;
import net.swofty.type.skywarsgame.luckyblock.items.weapons.*;

import java.util.Random;

public class LuckyBlockLootTable {
    private static final Random RANDOM = new Random();

    private static final int TICKS_PER_SECOND = 20;

    // Reward category percentages - more balanced distribution
    private static final int GOOD_ITEMS_CHANCE = 30;
    private static final int NEUTRAL_ITEMS_CHANCE = 45;  // 30 + 15
    private static final int POTIONS_CHANCE = 60;        // 45 + 15
    private static final int BAD_LUCK_CHANCE = 80;       // 60 + 20

    /**
     * Convert seconds to game ticks.
     */
    private static int secondsToTicks(int seconds) {
        return seconds * TICKS_PER_SECOND;
    }

    public enum RewardType {
        ITEMS,
        POTION,
        SPAWN_MOB,
        BAD_EFFECT,
        EXPLOSION,
        NOTHING,
        OP_RULE,
        ENVIRONMENT_EFFECT
    }

    public record LuckyBlockReward(
            RewardType type,
            ItemStack[] items,
            Potion potion,
            EntityType mobType,
            int mobCount,
            Text message,
            boolean isGood,
            LuckyBlockEnvironmentEffect environmentEffect
    ) {
        public static LuckyBlockReward items(ItemStack... items) {
            return new LuckyBlockReward(RewardType.ITEMS, items, null, null, 0,
                    Text.of("<6>Lucky!"), true, null);
        }

        public static LuckyBlockReward itemsWithMessage(Text message, ItemStack... items) {
            return new LuckyBlockReward(RewardType.ITEMS, items, null, null, 0, message, true, null);
        }

        public static LuckyBlockReward potion(Potion potion, String effectName) {
            return new LuckyBlockReward(RewardType.POTION, null, potion, null, 0,
                    Text.of("<a>You got {}!", effectName), true, null);
        }

        public static LuckyBlockReward badPotion(Potion potion, String effectName) {
            return new LuckyBlockReward(RewardType.BAD_EFFECT, null, potion, null, 0,
                    Text.of("<c>Unlucky! {}!", effectName), false, null);
        }

        public static LuckyBlockReward spawnMob(EntityType mobType, int count) {
            return new LuckyBlockReward(RewardType.SPAWN_MOB, null, null, mobType, count,
                    Text.of("<c>Unlucky! Enemies spawned!"), false, null);
        }

        public static LuckyBlockReward explosion() {
            return new LuckyBlockReward(RewardType.EXPLOSION, null, null, null, 0,
                    Text.of("<4>BOOM!"), false, null);
        }

        public static LuckyBlockReward nothing() {
            return new LuckyBlockReward(RewardType.NOTHING, null, null, null, 0,
                    Text.of("<7>Nothing happened..."), true, null);
        }

        public static LuckyBlockReward opRule() {
            return new LuckyBlockReward(RewardType.OP_RULE, null, null, null, 0,
                    Text.of("<b>OP RULE ACTIVATED!"), true, null);
        }

        public static LuckyBlockReward environmentEffect(LuckyBlockEnvironmentEffect effect) {
            return new LuckyBlockReward(RewardType.ENVIRONMENT_EFFECT, null, null, null, 0,
                    effect.getMessageText(), effect.isGood(), effect);
        }

        public static LuckyBlockReward randomGoodEnvironmentEffect() {
            return environmentEffect(LuckyBlockEnvironmentEffect.randomGood());
        }

        public static LuckyBlockReward randomBadEnvironmentEffect() {
            return environmentEffect(LuckyBlockEnvironmentEffect.randomBad());
        }
    }

    /**
     * Generate a random reward from breaking a lucky block (legacy - uses default loot).
     */
    public static LuckyBlockReward generateReward() {
        return generateReward(LuckyBlockType.WILD);
    }

    /**
     * Generate a random reward based on the lucky block type.
     */
    public static LuckyBlockReward generateReward(LuckyBlockType type) {
        return switch (type) {
            case GUARDIAN -> generateGuardianReward();
            case WEAPONRY -> generateWeaponryReward();
            case WILD -> generateWildReward();
            case CRAZY -> generateCrazyReward();
            case INSANE -> generateInsaneReward();
            case OP_RULE -> generateOPRuleReward();
        };
    }

    /**
     * Guardian Lucky Block - Defense items (armor, health, power-ups).
     */
    private static LuckyBlockReward generateGuardianReward() {
        return switch (RANDOM.nextInt(4)) {
            case 0 -> generateArmorReward();
            case 1 -> generateHealthReward();
            case 2 -> generateDefensivePotion();
            default -> generateRareDefensiveItems();
        };
    }

    /**
     * Weaponry Lucky Block - Weapons for melee and ranged attacks.
     */
    private static LuckyBlockReward generateWeaponryReward() {
        return switch (RANDOM.nextInt(3)) {
            case 0 -> generateMeleeWeapon();
            case 1 -> generateRangedWeapon();
            default -> generateRareWeapons();
        };
    }

    /**
     * Wild Lucky Block - Mix of good and bad effects.
     */
    private static LuckyBlockReward generateWildReward() {
        int roll = RANDOM.nextInt(100);

        if (roll < GOOD_ITEMS_CHANCE) {
            return generateGoodItems();
        } else if (roll < NEUTRAL_ITEMS_CHANCE) {
            return generateNeutralItems();
        } else if (roll < POTIONS_CHANCE) {
            return generatePotion();
        } else if (roll < BAD_LUCK_CHANCE) {
            return generateBadLuck();
        } else {
            return generateRareItems();
        }
    }

    /**
     * Crazy Lucky Block - More chaotic effects than Wild.
     */
    private static LuckyBlockReward generateCrazyReward() {
        int roll = RANDOM.nextInt(100);

        if (roll < 25) {
            return generateGoodItems();
        } else if (roll < 40) {
            return generatePotion();
        } else if (roll < 60) {
            return generateBadLuck();
        } else if (roll < 80) {
            return generateCrazyEffect();
        } else {
            return generateRareItems();
        }
    }

    /**
     * Insane Lucky Block - The most unpredictable effects.
     */
    private static LuckyBlockReward generateInsaneReward() {
        int roll = RANDOM.nextInt(100);

        if (roll < 20) {
            return generateRareItems();
        } else if (roll < 40) {
            return generateBadLuck();
        } else if (roll < 60) {
            return generateCrazyEffect();
        } else if (roll < 80) {
            return generateInsaneEffect();
        } else {
            return generateLegendaryReward();
        }
    }

    /**
     * OP Rule Lucky Block - Activates a game-wide rule.
     * The actual rule activation is handled by OPRuleManager in LuckyBlock.applyReward().
     */
    private static LuckyBlockReward generateOPRuleReward() {
        return LuckyBlockReward.opRule();
    }

    // === Guardian-specific rewards ===

    private static LuckyBlockReward generateArmorReward() {
        return switch (RANDOM.nextInt(16)) {
            case 0 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>El Dorado Armor!"),
                    new ElDoradoHelmet().createItemStack(),
                    new ElDoradoChestplate().createItemStack(),
                    new ElDoradoLeggings().createItemStack(),
                    new ElDoradoBoots().createItemStack()
            );
            case 1 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<d>Disco Armor!"),
                    new DiscoHelmet().createItemStack(),
                    new DiscoChestplate().createItemStack(),
                    new DiscoLeggings().createItemStack(),
                    new DiscoBoots().createItemStack()
            );
            case 2 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<5>Exodus!"),
                    new Exodus().createItemStack()
            );
            case 3 -> LuckyBlockReward.items(new FrogHelmet().createItemStack());
            case 4 -> LuckyBlockReward.items(new GreaserJacket().createItemStack());
            case 5 -> LuckyBlockReward.items(new CutePants().createItemStack());
            case 6 -> LuckyBlockReward.items(new ManitouBoots().createItemStack());
            case 7 -> LuckyBlockReward.items(new SplatcraftBoots().createItemStack());
            case 8 -> LuckyBlockReward.items(new SpeedsterBoots().createItemStack());
            case 9 -> LuckyBlockReward.items(new HotHead().createItemStack());
            case 10 -> LuckyBlockReward.items(new StingedLeggings().createItemStack());
            case 11 -> LuckyBlockReward.items(new ChillyPants().createItemStack());
            case 12 -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_HELMET), ItemStack.of(Material.DIAMOND_CHESTPLATE));
            case 13 -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_LEGGINGS), ItemStack.of(Material.DIAMOND_BOOTS));
            case 14 -> LuckyBlockReward.items(ItemStack.of(Material.IRON_HELMET), ItemStack.of(Material.IRON_CHESTPLATE));
            default -> LuckyBlockReward.items(ItemStack.of(Material.IRON_LEGGINGS), ItemStack.of(Material.IRON_BOOTS));
        };
    }

    private static LuckyBlockReward generateHealthReward() {
        return switch (RANDOM.nextInt(7)) {
            case 0 -> LuckyBlockReward.items(new AbsorptionHearts().createItemStack());
            case 1 -> LuckyBlockReward.items(new PlusHealthItem().createItemStack());
            case 2 -> LuckyBlockReward.items(new OneUpMushroom().createItemStack());
            case 3 -> LuckyBlockReward.items(new SuperStar().createItemStack());
            case 4 -> LuckyBlockReward.items(ItemStack.of(Material.GOLDEN_APPLE, 2 + RANDOM.nextInt(3)));
            case 5 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.REGENERATION, (byte) 1, secondsToTicks(30)),
                    "Regeneration II"
            );
            default -> LuckyBlockReward.items(new Cornucopia().createItemStack());
        };
    }

    private static LuckyBlockReward generateDefensivePotion() {
        return switch (RANDOM.nextInt(4)) {
            case 0 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.RESISTANCE, (byte) 1, secondsToTicks(45)),
                    "Resistance II"
            );
            case 1 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, secondsToTicks(60)),
                    "Fire Resistance"
            );
            case 2 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.HEALTH_BOOST, (byte) 2, secondsToTicks(120)),
                    "+6 Hearts"
            );
            default -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.RESISTANCE, (byte) 4, secondsToTicks(15)),
                    "Super Star!"
            );
        };
    }

    private static LuckyBlockReward generateRareDefensiveItems() {
        return switch (RANDOM.nextInt(4)) {
            case 0 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<f>Full Iron Armor!"),
                    ItemStack.of(Material.IRON_HELMET),
                    ItemStack.of(Material.IRON_CHESTPLATE),
                    ItemStack.of(Material.IRON_LEGGINGS),
                    ItemStack.of(Material.IRON_BOOTS)
            );
            case 1 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<b>Diamond Armor Set!"),
                    ItemStack.of(Material.DIAMOND_HELMET),
                    ItemStack.of(Material.DIAMOND_CHESTPLATE),
                    ItemStack.of(Material.DIAMOND_LEGGINGS),
                    ItemStack.of(Material.DIAMOND_BOOTS)
            );
            case 2 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>Totem of Undying!"),
                    ItemStack.of(Material.TOTEM_OF_UNDYING)
            );
            default -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<5>Netherite Armor!"),
                    ItemStack.of(Material.NETHERITE_HELMET),
                    ItemStack.of(Material.NETHERITE_CHESTPLATE),
                    ItemStack.of(Material.NETHERITE_LEGGINGS),
                    ItemStack.of(Material.NETHERITE_BOOTS)
            );
        };
    }

    // === Weaponry-specific rewards ===

    private static LuckyBlockReward generateMeleeWeapon() {
        return switch (RANDOM.nextInt(14)) {
            case 0 -> LuckyBlockReward.items(new Excalibur().createItemStack());
            case 1 -> LuckyBlockReward.items(new SwordOfElDorado().createItemStack());
            case 2 -> LuckyBlockReward.items(new AxeOfPerun().createItemStack());
            case 3 -> LuckyBlockReward.items(new Anduril().createItemStack());
            case 4 -> LuckyBlockReward.items(new SwordOfJustice().createItemStack());
            case 5 -> LuckyBlockReward.items(new TheStick().createItemStack());
            case 6 -> LuckyBlockReward.items(new OnePoundFish().createItemStack());
            case 7 -> LuckyBlockReward.items(new TheSpoon().createItemStack());
            case 8 -> LuckyBlockReward.items(new HappyLittleTree().createItemStack());
            case 9 -> LuckyBlockReward.items(new KnockbackSlimeball().createItemStack());
            case 10 -> LuckyBlockReward.items(new CarrotCorrupter().createItemStack());
            case 11 -> LuckyBlockReward.items(new VomitBagel().createItemStack());
            case 12 -> LuckyBlockReward.items(new StickOfTruth().createItemStack());
            default -> LuckyBlockReward.items(new IronSwordWeapon().createItemStack());
        };
    }

    private static LuckyBlockReward generateRangedWeapon() {
        return switch (RANDOM.nextInt(10)) {
            case 0 -> LuckyBlockReward.items(new BowAndArrows().createItemStack());
            case 1 -> LuckyBlockReward.items(new PunchBow().createItemStack());
            case 2 -> LuckyBlockReward.items(new Invisibow().createItemStack());
            case 3 -> LuckyBlockReward.items(new ExplosiveBow().createItemStack());
            case 4 -> LuckyBlockReward.items(new Railgun().createItemStack());
            case 5 -> LuckyBlockReward.items(new Shotgun().createItemStack());
            case 6 -> LuckyBlockReward.items(new WitherBlastRod().createItemStack());
            case 7 -> LuckyBlockReward.items(new PigletBazooka().createItemStack());
            case 8 -> LuckyBlockReward.items(ItemStack.of(Material.ENDER_PEARL, 3 + RANDOM.nextInt(3)));
            default -> LuckyBlockReward.items(new MagicToyStick().createItemStack());
        };
    }

    private static LuckyBlockReward generateRareWeapons() {
        return switch (RANDOM.nextInt(4)) {
            case 0 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<4>Netherite Sword!"),
                    ItemStack.of(Material.NETHERITE_SWORD)
            );
            case 1 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<b>Trident!"),
                    ItemStack.of(Material.TRIDENT)
            );
            case 2 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<4>Netherite Axe!"),
                    ItemStack.of(Material.NETHERITE_AXE)
            );
            default -> LuckyBlockReward.items(new SelfAttackingSword().createItemStack());
        };
    }

    // === Crazy/Insane effects ===

    private static LuckyBlockReward generateCrazyEffect() {
        return switch (RANDOM.nextInt(12)) {
            case 0 -> LuckyBlockReward.spawnMob(EntityType.WOLF, 3);
            case 1 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.RANDOM_TELEPORT);
            case 2 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.LAUNCH_PAD);
            case 3 -> LuckyBlockReward.items(new BridgeEggs().createItemStack());
            case 4 -> LuckyBlockReward.items(new Fireball().createItemStack());
            case 5 -> LuckyBlockReward.items(new ThrowableTNT().createItemStack());
            case 6 -> LuckyBlockReward.items(new WaterBalloon().createItemStack());
            case 7 -> LuckyBlockReward.items(new JediForce().createItemStack());
            case 8 -> LuckyBlockReward.items(new SuperDie().createItemStack());
            case 9 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.COBWEB_TRAP);
            case 10 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.SWAPPING_PLACE);
            default -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.PROTECTIVE_WALL);
        };
    }

    private static LuckyBlockReward generateInsaneEffect() {
        return switch (RANDOM.nextInt(14)) {
            case 0 -> LuckyBlockReward.spawnMob(EntityType.SLIME, 1);
            case 1 -> LuckyBlockReward.spawnMob(EntityType.WITHER_SKELETON, 2);
            case 2 -> LuckyBlockReward.explosion();
            case 3 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.LIGHTNING_STRIKE);
            case 4 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.TNT_SURROUND);
            case 5 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.LAVA_PIT);
            case 6 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.ARROW_RAIN);
            case 7 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.ANVIL_RAIN);
            case 8 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.INSTA_HOLE);
            case 9 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.KABOOM);
            case 10 -> LuckyBlockReward.items(new DevilsContract().createItemStack());
            case 11 -> LuckyBlockReward.items(new VoidCharm().createItemStack());
            case 12 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.CURSE);
            default -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.BLESSING);
        };
    }

    private static LuckyBlockReward generateLegendaryReward() {
        return switch (RANDOM.nextInt(4)) {
            case 0 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<5>Full Netherite!"),
                    ItemStack.of(Material.NETHERITE_HELMET),
                    ItemStack.of(Material.NETHERITE_CHESTPLATE),
                    ItemStack.of(Material.NETHERITE_LEGGINGS),
                    ItemStack.of(Material.NETHERITE_BOOTS),
                    ItemStack.of(Material.NETHERITE_SWORD)
            );
            case 1 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<d>Elytra!"),
                    ItemStack.of(Material.ELYTRA),
                    ItemStack.of(Material.FIREWORK_ROCKET, 16)
            );
            case 2 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>Golden Apples x32!"),
                    ItemStack.of(Material.GOLDEN_APPLE, 32)
            );
            default -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>Enchanted Golden Apples!"),
                    ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE, 3)
            );
        };
    }

    private static LuckyBlockReward generateGoodItems() {
        return switch (RANDOM.nextInt(12)) {
            case 0 -> LuckyBlockReward.items(ItemStack.of(Material.IRON_SWORD));
            case 1 -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_SWORD));
            case 2 -> LuckyBlockReward.items(ItemStack.of(Material.IRON_AXE));
            case 3 -> LuckyBlockReward.items(ItemStack.of(Material.BOW), ItemStack.of(Material.ARROW, 16));
            case 4 -> LuckyBlockReward.items(ItemStack.of(Material.IRON_HELMET), ItemStack.of(Material.IRON_CHESTPLATE));
            case 5 -> LuckyBlockReward.items(ItemStack.of(Material.IRON_LEGGINGS), ItemStack.of(Material.IRON_BOOTS));
            case 6 -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_HELMET));
            case 7 -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_CHESTPLATE));
            case 8 -> LuckyBlockReward.items(ItemStack.of(Material.GOLDEN_APPLE, 1 + RANDOM.nextInt(3)));
            case 9 -> LuckyBlockReward.items(ItemStack.of(Material.ENDER_PEARL, 2 + RANDOM.nextInt(3)));
            case 10 -> LuckyBlockReward.items(ItemStack.of(Material.ARROW, 16 + RANDOM.nextInt(16)));
            default -> LuckyBlockReward.items(ItemStack.of(Material.DIAMOND_AXE));
        };
    }

    private static LuckyBlockReward generateNeutralItems() {
        return switch (RANDOM.nextInt(8)) {
            case 0 -> LuckyBlockReward.items(ItemStack.of(Material.OAK_PLANKS, 32 + RANDOM.nextInt(33)));
            case 1 -> LuckyBlockReward.items(ItemStack.of(Material.FISHING_ROD));
            case 2 -> LuckyBlockReward.items(ItemStack.of(Material.EGG, 8 + RANDOM.nextInt(8)));
            case 3 -> LuckyBlockReward.items(ItemStack.of(Material.SNOWBALL, 16));
            case 4 -> LuckyBlockReward.items(ItemStack.of(Material.COBWEB, 3 + RANDOM.nextInt(3)));
            case 5 -> LuckyBlockReward.items(ItemStack.of(Material.SHIELD));
            case 6 -> LuckyBlockReward.items(ItemStack.of(Material.CROSSBOW), ItemStack.of(Material.ARROW, 8));
            default -> LuckyBlockReward.items(ItemStack.of(Material.FLINT_AND_STEEL));
        };
    }

    private static LuckyBlockReward generatePotion() {
        return switch (RANDOM.nextInt(6)) {
            case 0 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.SPEED, (byte) (1 + RANDOM.nextInt(2)), secondsToTicks(30 + RANDOM.nextInt(31))),
                    "Speed II");
            case 1 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.STRENGTH, (byte) 1, secondsToTicks(20 + RANDOM.nextInt(21))),
                    "Strength II");
            case 2 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.JUMP_BOOST, (byte) 2, secondsToTicks(30 + RANDOM.nextInt(31))),
                    "Jump Boost III");
            case 3 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.INVISIBILITY, (byte) 0, secondsToTicks(20 + RANDOM.nextInt(20))),
                    "Invisibility");
            case 4 -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.REGENERATION, (byte) 1, secondsToTicks(15 + RANDOM.nextInt(15))),
                    "Regeneration II");
            default -> LuckyBlockReward.potion(
                    new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, secondsToTicks(45 + RANDOM.nextInt(45))),
                    "Fire Resistance");
        };
    }

    private static LuckyBlockReward generateBadLuck() {
        return switch (RANDOM.nextInt(8)) {
            case 0 -> LuckyBlockReward.spawnMob(EntityType.ZOMBIE, 1 + RANDOM.nextInt(2));
            case 1 -> LuckyBlockReward.spawnMob(EntityType.SKELETON, 1 + RANDOM.nextInt(2));
            case 2 -> LuckyBlockReward.spawnMob(EntityType.SILVERFISH, 3 + RANDOM.nextInt(3));
            case 3 -> LuckyBlockReward.badPotion(
                    new Potion(PotionEffect.POISON, (byte) 0, secondsToTicks(5 + RANDOM.nextInt(6))),
                    "Poison");
            case 4 -> LuckyBlockReward.badPotion(
                    new Potion(PotionEffect.WITHER, (byte) 0, secondsToTicks(3 + RANDOM.nextInt(4))),
                    "Wither");
            case 5 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.COBWEB_TRAP);
            case 6 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.LIGHTNING_STRIKE);
            default -> LuckyBlockReward.explosion();
        };
    }

    private static LuckyBlockReward generateRareItems() {
        return switch (RANDOM.nextInt(8)) {
            case 0 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<f>Full Iron Armor!"),
                    ItemStack.of(Material.IRON_HELMET),
                    ItemStack.of(Material.IRON_CHESTPLATE),
                    ItemStack.of(Material.IRON_LEGGINGS),
                    ItemStack.of(Material.IRON_BOOTS)
            );
            case 1 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<b>Full Diamond Armor!"),
                    ItemStack.of(Material.DIAMOND_HELMET),
                    ItemStack.of(Material.DIAMOND_CHESTPLATE),
                    ItemStack.of(Material.DIAMOND_LEGGINGS),
                    ItemStack.of(Material.DIAMOND_BOOTS)
            );
            case 2 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<5>Netherite Sword!"),
                    ItemStack.of(Material.NETHERITE_SWORD)
            );
            case 3 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>Enchanted Golden Apple!"),
                    ItemStack.of(Material.ENCHANTED_GOLDEN_APPLE)
            );
            case 4 -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<6>Totem of Undying!"),
                    ItemStack.of(Material.TOTEM_OF_UNDYING)
            );
            case 5 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.BRIDGE_BUILD);
            case 6 -> LuckyBlockReward.environmentEffect(LuckyBlockEnvironmentEffect.BLESSING);
            default -> LuckyBlockReward.itemsWithMessage(
                    Text.of("<d>Elytra!"),
                    ItemStack.of(Material.ELYTRA),
                    ItemStack.of(Material.FIREWORK_ROCKET, 8)
            );
        };
    }
}
