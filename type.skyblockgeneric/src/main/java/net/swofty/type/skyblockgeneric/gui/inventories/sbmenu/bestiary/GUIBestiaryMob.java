package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.BestiaryDropRarity;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIBestiaryMob extends StatelessView {

    private static final Map<Integer, int[]> SLOTS = new HashMap<>(Map.of(
            0, new int[]{},
            1, new int[]{22},
            2, new int[]{21, 23},
            3, new int[]{20, 22, 24},
            4, new int[]{19, 21, 23, 25},
            5, new int[]{20, 21, 22, 23, 24},
            6, new int[]{21, 22, 23, 30, 31, 32},
            7, new int[]{19, 20, 21, 22, 23, 24, 25}
    ));

    private static final int[] DISPLAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };

    private final BestiaryCategories category;
    private final BestiaryEntry bestiaryEntry;

    public GUIBestiaryMob(BestiaryCategories category, BestiaryEntry bestiaryEntry) {
        this.category = category;
        this.bestiaryEntry = bestiaryEntry;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("{} ➡ {}",
                Text.of(category.getDisplayName()).plain(),
                Text.parse(bestiaryEntry.getName()).plain()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        BestiaryData bestiaryData = new BestiaryData();

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            ArrayList<String> rendered = new ArrayList<>();
            BestiaryMob mob = bestiaryEntry.getMobs().getFirst();
            GUIMaterial guiMaterial = bestiaryEntry.getGuiMaterial();
            int kills = player.getBestiaryData().getAmount(bestiaryEntry.getMobs());
            int tier = bestiaryData.getCurrentBestiaryTier(mob, kills);

            player.getBestiaryData().getMobDisplay(rendered, kills, mob, bestiaryEntry);

            return ItemStacks.of(guiMaterial, 1,
                    Text.of("<a>{} {:roman}", Text.parse(bestiaryEntry.getName()), tier),
                    rendered.stream().map(Text::parse).toList());
        });

        List<BestiaryMob> bestiaryMobs = bestiaryEntry.getMobs();
        int mobCount = bestiaryMobs.size();
        int[] chosenSlots = SLOTS.getOrDefault(mobCount, DISPLAY_SLOTS);

        for (int i = 0; i < bestiaryMobs.size() && i < chosenSlots.length; i++) {
            BestiaryMob mob = bestiaryMobs.get(i);
            GUIMaterial guiMaterial = mob.getGuiMaterial();
            int slot = chosenSlots[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();
                int kills = player.getBestiaryData().getAmount(mob);
                int deaths = player.getDeathData().getAmount(mob.getMobID());
                OtherLoot otherLoot = mob.getOtherLoot();

                List<SkyBlockLootTable.LootRecord> lootRecords = mob.getLootTable().getLootTable();
                Map<BestiaryDropRarity, List<SkyBlockLootTable.LootRecord>> lootByRarity =
                        new EnumMap<>(BestiaryDropRarity.class);
                for (SkyBlockLootTable.LootRecord lootRecord : lootRecords) {
                    lootByRarity.computeIfAbsent(lootRecord.getRarity(), ignored -> new ArrayList<>()).add(lootRecord);
                }

                List<MobType> mobtypes = mob.getMobTypes();

                if (mobtypes.size() == 1) {
                    lore.add(Text.of("<7>Mob Type: {}", mobtypes.getFirst().getFullDisplayName()));
                    lore.add(Text.empty());
                } else if (mobtypes.size() > 1) {
                    List<Text> displayNames = new ArrayList<>();
                    for (MobType mobType : mobtypes) {
                        displayNames.add(mobType.getFullDisplayName());
                    }

                    lore.add(Text.of("<7>Mob Types: {}", Text.join(Text.of("<7>, "), displayNames)));
                    lore.add(Text.empty());
                }

                lore.add(Text.of("<7>Mob Stats:"));
                lore.add(Text.of("<7>Health: <c>{}<glyph:stat_health>",
                        Math.round(mob.getBaseStatistics().getOverall(ItemStatistic.HEALTH).floatValue())));
                lore.add(Text.of("<7>Damage: <c>{}<glyph:stat_ability_damage>",
                        Math.round(mob.getBaseStatistics().getOverall(ItemStatistic.DAMAGE).floatValue())));
                lore.add(Text.of("<7>Coins per Kill: <6>{}", otherLoot.getCoinAmount()));
                lore.add(Text.of("<7>{} Exp: <3>{}", mob.getSkillCategory().asCategory().getName(), otherLoot.getSkillXPAmount()));
                lore.add(Text.of("<7>XP Orbs: <3>{}", otherLoot.getXpOrbAmount()));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Kills: <a>{}", kills));
                lore.add(Text.of("<7>Deaths: <a>{}", deaths));
                lore.add(Text.empty());

                for (BestiaryDropRarity rarity : BestiaryDropRarity.values()) {
                    List<SkyBlockLootTable.LootRecord> rarityLoot = lootByRarity.getOrDefault(rarity, List.of());
                    if (rarityLoot.isEmpty()) continue;
                    lore.add(rarity.displayName());
                    for (SkyBlockLootTable.LootRecord lootRecord : rarityLoot) {
                        lore.add(rarity.showsChance()
                                ? Text.of(" <8>■ <f>{} <8>(<a>{}%<8>)",
                                lootRecord.getItemType().getDisplayName(), lootRecord.getChancePercent())
                                : Text.of(" <8>■ <f>{}", lootRecord.getItemType().getDisplayName()));
                    }
                    lore.add(Text.empty());
                }

                if (!lore.isEmpty()) lore.removeLast();

                return ItemStacks.of(guiMaterial, 1,
                        Text.of("<8>[<7>Lv{}<8>] <f>{}", mob.getLevel(), mob.getDisplayName()), lore);
            });
        }
    }
}
