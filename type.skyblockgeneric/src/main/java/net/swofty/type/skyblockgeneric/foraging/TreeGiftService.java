package net.swofty.type.skyblockgeneric.foraging;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.swofty.commons.loot.LootEntry;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.loot.LootRoll;
import net.swofty.commons.loot.LootTable;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.hunting.AttributeId;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.loottable.PityDefinition;
import net.swofty.type.skyblockgeneric.loottable.PityService;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TreeGiftService {
    private static final PityDefinition TREE_THE_FISH_PITY =
            new PityDefinition(Key.key("skyblock", "foraging/tree_the_fish"), 1_000);

    private TreeGiftService() {
    }

    public static void award(Instance instance, String treeType, Map<UUID, Integer> contributions, int treeLogs) {
        int sizeMultiplier = Math.clamp((int) Math.round(treeLogs / 25D), 1, 20);
        int total = contributions.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<UUID, Integer> entry : contributions.entrySet()) {
            Player rawPlayer = instance.getPlayerByUuid(entry.getKey());
            if (!(rawPlayer instanceof SkyBlockPlayer player)) continue;
            double share = total == 0 ? 0 : entry.getValue() / (double) total;
            double rewardScale = share < 0.20 ? 0 : share < 0.33 ? 0.5 : 1;
            if (rewardScale == 0) {
                player.sendMessage("<c>You need to cut at least 20% of the tree to receive a Tree Gift!");
                continue;
            }
            awardPlayer(player, treeType, sizeMultiplier, rewardScale, share);
        }
    }

    private static void awardPlayer(SkyBlockPlayer player, String treeType, int multiplier,
                                    double rewardScale, double share) {
        List<Text> rewards = new ArrayList<>();
        List<Text> bonusMessages = new ArrayList<>();
        int scaled = Math.max(1, (int) Math.round(multiplier * rewardScale));
        int essence = switch (treeType) {
            case "MANGROVE" -> 3;
            case "HELIX" -> 4;
            default -> 2;
        } * scaled;
        int foragingXp = switch (treeType) {
            case "MANGROVE" -> 1250;
            case "HELIX" -> 1500;
            default -> 1000;
        } * scaled;
        increase(player, SkyBlockDataHandler.Data.FOREST_ESSENCE, essence);
        increase(player, SkyBlockDataHandler.Data.HOTF_EXPERIENCE, 10L * scaled);
        player.getSkills().increase(player, SkillCategories.FORAGING, (double) foragingXp);
        rewards.add(Text.of("<a>Forest Essence <7>x<f>{}", essence));
        rewards.add(Text.of("<a>Foraging Experience <7>x<f>{}", foragingXp));
        rewards.add(Text.of("<a>HOTF Experience <7>x<f>{}", 10 * scaled));
        if (treeType.equals("FIG")) {
            int tender = ThreadLocalRandom.current().nextInt(scaled + 1);
            if (tender > 0) player.addAndUpdateItem(ItemType.TENDER_WOOD, tender);
            rewards.add(Text.of("<a>Tender Wood <7>x<f>0-{}", scaled));
        } else if (treeType.equals("MANGROVE")) {
            int vinesap = ThreadLocalRandom.current().nextInt(scaled + 1);
            if (vinesap > 0) player.addAndUpdateItem(ItemType.VINESAP, vinesap);
            rewards.add(Text.of("<a>Vinesap <7>x<f>0-{}", scaled));
        }
        if (treeType.equals("HELIX")) {
            increase(player, SkyBlockDataHandler.Data.FOREST_WHISPERS, 225L * scaled);
            rewards.add(Text.of("<a>Forest Whispers <7>x<f>{}", 75 * scaled));
            rewards.add(Text.of("<a>Forest Whispers <7>x<f>{}", 150 * scaled));
        } else {
            int whispers = ThreadLocalRandom.current().nextInt(20 * scaled, 40 * scaled + 1);
            increase(player, SkyBlockDataHandler.Data.FOREST_WHISPERS, whispers);
            rewards.add(Text.of("<a>Forest Whispers <7>x<f>{}", whispers));
        }
        List<Bonus> bonuses = new ArrayList<>(bonuses(treeType));
        int guaranteedRewards = rewards.size();
        double treeLurker = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("C24")) / 100D;
        double signalChance = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("R7")) / 100D;
        if (signalChance > 0) bonuses.add(new Bonus(null, ItemType.SIGNAL_ENHANCER, signalChance, false));
        boolean guaranteeTreeFish = PityService.guaranteesNext(player, TREE_THE_FISH_PITY);
        TreeGiftRollContext context = new TreeGiftRollContext(scaled, treeLurker, guaranteeTreeFish);
        List<LootEntry<TreeGiftRollContext, Bonus>> entries = new ArrayList<>();
        for (int index = 0; index < bonuses.size(); index++) {
            Bonus bonus = bonuses.get(index);
            entries.add(new LootEntry<>(Key.key("skyblock", "foraging/tree_gift/bonus_" + index), bonus, bonus.chance,
                    ignored -> true,
                    List.of((rollContext, ignored, chance) -> {
                        if (bonus.item == ItemType.TREE_THE_FISH && rollContext.guaranteeTreeFish()) return 1;
                        double effective = chance * (bonus.scalesWithTreeSize() ? rollContext.multiplier() : 1);
                        return bonus.shard == null ? effective : effective * (1 + rollContext.treeLurker());
                    })));
        }
        List<Bonus> rolledBonuses = new LootTable<TreeGiftRollContext, Bonus>(Key.key("skyblock", "foraging/tree_gift"), List.of(
                new LootPool<>(Key.key("skyblock", "bonuses"), LootPool.Mode.INDEPENDENT, entries)
        )).roll(context).stream().map(LootRoll::value).toList();
        boolean obtainedTreeFish = false;
        for (Bonus bonus : rolledBonuses) {
            double chance = bonus.item == ItemType.TREE_THE_FISH && guaranteeTreeFish
                    ? 1 : Math.min(1, bonus.chance * (bonus.scalesWithTreeSize() ? scaled : 1)
                    * (bonus.shard == null ? 1 : 1 + treeLurker));
            if (bonus.shard != null) {
                AttributeDefinition definition = AttributeRegistry.findByShard(bonus.shard).orElse(null);
                if (definition == null) continue;
                player.getHuntingData().addShards(definition.id(), 1);
                Text shardText = Text.of("<color:{}>{}", definition.rarity().itemRarity().getColor(), definition.shardName());
                rewards.add(shardText);
                bonusMessages.add(shardText.append(" <7>(<a>{}<7>)", chance(bonus.chance)));
            } else if (bonus.item != null) {
                player.addAndUpdateItem(bonus.item);
                if (bonus.item == ItemType.TREE_THE_FISH) obtainedTreeFish = true;
                Text itemText = Text.of("<color:{}>{}", bonus.item.rarity.getColor(), bonus.item.getDisplayName());
                rewards.add(itemText);
                bonusMessages.add(itemText.append(" <7>(<a>{}<7>)", chance(bonus.chance)));
            }
        }
        PityService.recordAttempt(player, TREE_THE_FISH_PITY, obtainedTreeFish);
        int percent = (int) Math.round(share * 100);
        Text body = Text.of("<2><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n")
                .append("""
                        <a><l>TREE GIFT</l>
                        <f>You helped cut <a>{0}% <f>of the {1} Tree.
                        <6>+{2} rewards gained! <7>(hover)
                        """, percent, pretty(treeType), guaranteedRewards);
        if (!bonusMessages.isEmpty()) {
            body = body.append("<d><l>BONUS GIFT</l>\n")
                    .append(Text.join(Text.literal("\n"), bonusMessages))
                    .append(Text.literal("\n"));
        }
        body = body.append("<2><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        player.sendMessage(Text.of("<hover:'{0}'>{1}</hover>",
                Text.join(Text.literal("\n"), rewards), body));
    }

    private static List<Bonus> bonuses(String treeType) {
        if (treeType.equals("FIG")) return List.of(
                new Bonus("Phanpyre", null, .118), new Bonus(null, ItemType.STRETCHING_STICKS, .10),
                new Bonus("Phanflare", null, .071), new Bonus("Dreadwing", null, .012),
                new Bonus(null, ItemType.SWEEP_BOOSTER, .005), new Bonus(null, ItemType.FORAGING_WISDOM_BOOSTER, .003),
                new Bonus("Hummingbird", null, .0015), new Bonus("Chameleon", null, .0008),
                new Bonus(null, ItemType.TREE_THE_FISH, .0005));
        if (treeType.equals("MANGROVE")) return List.of(
                new Bonus("Phanpyre", null, .118), new Bonus("Phanflare", null, .071),
                new Bonus(null, ItemType.DEEP_ROOT, .04), new Bonus("Dreadwing", null, .012),
                new Bonus(null, ItemType.SWEEP_BOOSTER, .01), new Bonus(null, ItemType.FORAGING_WISDOM_BOOSTER, .005),
                new Bonus("Hummingbird", null, .0025), new Bonus("Chameleon", null, .0008),
                new Bonus(null, ItemType.TREE_THE_FISH, .0005));
        return List.of(new Bonus("Firefox", null, .15), new Bonus("Groundhog", null, .15),
                new Bonus("Drybark", null, .05), new Bonus("Puck", null, .05),
                new Bonus("Grizzly Bear", null, .01), new Bonus("Hummingbird", null, .003),
                new Bonus("Chameleon", null, .001), new Bonus(null, ItemType.TREE_THE_FISH, .0005));
    }

    private static void increase(SkyBlockPlayer player, SkyBlockDataHandler.Data key, long amount) {
        DatapointLong data = player.getSkyblockDataHandler().get(key, DatapointLong.class);
        data.setValue(data.getValue() + amount);
    }

    private static String pretty(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }

    private static String chance(double chance) {
        return "%s%%".formatted(chance * 100D % 1 == 0 ? Integer.toString((int) (chance * 100D))
                : Double.toString(chance * 100D));
    }

    private record Bonus(String shard, ItemType item, double chance, boolean scalesWithTreeSize) {
        private Bonus(String shard, ItemType item, double chance) {
            this(shard, item, chance, true);
        }
    }

    private record TreeGiftRollContext(int multiplier, double treeLurker, boolean guaranteeTreeFish) {
    }
}
