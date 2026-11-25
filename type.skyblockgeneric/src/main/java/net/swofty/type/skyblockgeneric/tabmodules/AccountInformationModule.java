package net.swofty.type.skyblockgeneric.tabmodules;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkillCategory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.tabwidgets.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountInformationModule extends TablistModule {

    public List<TablistEntry> getEntries(SkyBlockPlayer player) {
        TablistLocation location = TablistLocation.current();
        TablistWidgetSettings widgetSettings = TablistSettingsStore.get(player);
        ArrayList<TablistEntry> configured = new ArrayList<>();
        for (TablistWidget widget : widgetSettings.order(location)) {
            if (!widgetSettings.enabled(location, widget) || widget == TablistWidget.GENERAL_INFO) continue;
            int before = configured.size();
            appendWidget(configured, player, widget, widgetSettings.options(location, widget));
            if (configured.size() > before && widgetSettings.options(location, widget).spacing())
                configured.add(getGrayEntry());
            if (configured.size() >= 20) break;
        }
        if (!configured.isEmpty()) {
            if (configured.size() > 20) configured.subList(20, configured.size()).clear();
            return fillRestWithGray(configured);
        }

        Locale l = player.getLocale();
        ArrayList<TablistEntry> entries = new ArrayList<>(List.of(
                centered(Text.key("tablist.module.account_info"), l, TablistSkinRegistry.ORANGE)
        ));

        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        DatapointBankData.BankData bankData = dataHandler.get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();

        entries.add(new TablistEntry(Text.key("tablist.account_info.profile_label", dataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue()), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.pet_sitter"), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.bank", StringUtility.shortenNumber(bankData.getAmount()), StringUtility.shortenNumber(bankData.getBalanceLimit())), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());

        SkillCategories skillCategory = dataHandler.get(SkyBlockDataHandler.Data.LAST_EDITED_SKILL, DatapointSkillCategory.class).getValue();
        DatapointSkills.PlayerSkills skills = player.getSkills();
        ItemStatistics playerStatistics = player.getStatistics().allStatistics();
        entries.add(new TablistEntry(Text.key("tablist.account_info.skills_label", String.valueOf(skillCategory), String.valueOf(skills.getCurrentLevel(skillCategory)), String.valueOf(skills.getPercentage(skillCategory))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.speed", ItemStatistic.SPEED.getColouredSymbol(), String.valueOf(playerStatistics.getOverall(ItemStatistic.SPEED))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.strength", ItemStatistic.STRENGTH.getColouredSymbol(), ItemStatistic.STRENGTH.getSymbol().getSprite(), String.valueOf(playerStatistics.getOverall(ItemStatistic.STRENGTH))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.crit_chance", ItemStatistic.CRITICAL_CHANCE.getColouredSymbol(), ItemStatistic.CRITICAL_CHANCE.getSymbol().getSprite(), String.valueOf(playerStatistics.getOverall(ItemStatistic.CRITICAL_CHANCE))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.crit_damage", ItemStatistic.CRITICAL_DAMAGE.getColouredSymbol(), ItemStatistic.CRITICAL_DAMAGE.getSymbol().getSprite(), String.valueOf(playerStatistics.getOverall(ItemStatistic.CRITICAL_DAMAGE))), TablistSkinRegistry.GRAY));
        entries.add(new TablistEntry(Text.key("tablist.account_info.attack_speed", ItemStatistic.BONUS_ATTACK_SPEED.getColouredSymbol(), ItemStatistic.BONUS_ATTACK_SPEED.getSymbol().getSprite(), String.valueOf(playerStatistics.getOverall(ItemStatistic.BONUS_ATTACK_SPEED))), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        fillRestWithGray(entries);
        return entries;
    }

    private void appendWidget(List<TablistEntry> entries, SkyBlockPlayer player, TablistWidget widget,
                              TablistWidgetSettings.Options options) {
        SkyBlockDataHandler data = player.getSkyblockDataHandler();
        ItemStatistics stats = player.getStatistics().allStatistics();
        switch (widget) {
            case PROFILE -> {
                DatapointBankData.BankData bank = data.get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class).getValue();
                entries.add(entry("§e§lProfile: §a" + data.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue()));
                if (options.showSkyBlockLevel())
                    entries.add(entry("§f SB Level: §b" + player.getSkyBlockExperience().getLevel()));
                if (options.showBankBalance())
                    entries.add(entry("§f Bank: §6" + StringUtility.shortenNumber(bank.getAmount())));
            }
            case PET -> {
                var pet = player.getPetData().getEnabledPet();
                entries.add(entry("§e§lPet:"));
                entries.add(entry(pet == null ? "§7 None" : "§f " + pet.getDisplayName()));
                // Pet XP/item lines are intentionally omitted until their backing component exposes stable display data.
            }
            case SKILLS -> {
                entries.add(entry("§e§lSkills:"));
                DatapointSkills.PlayerSkills skills = player.getSkills();
                for (SkillCategories category : List.of(SkillCategories.FARMING, SkillCategories.MINING, SkillCategories.COMBAT, SkillCategories.FORAGING))
                    entries.add(entry("§f " + category + " " + skills.getCurrentLevel(category) + ": §a" + skills.getPercentage(category) + "%"));
            }
            case STATS -> {
                entries.add(entry("§e§lStats:"));
                for (TablistStat selected : TablistStat.values())
                    if (options.shownStats().contains(selected.id)) {
                        ItemStatistic statistic = statistic(selected);
                        if (statistic != null)
                            entries.add(entry("§f " + selected.display + ": §f" + stats.getOverall(statistic)));
                    }
            }
            default -> { /* No fabricated values for systems without a server-side provider. */ }
        }
    }

    private ItemStatistic statistic(TablistStat stat) {
        return switch (stat) {
            case HEALTH -> ItemStatistic.HEALTH;
            case DEFENSE -> ItemStatistic.DEFENSE;
            case TRUE_DEFENSE -> ItemStatistic.TRUE_DEFENSE;
            case STRENGTH -> ItemStatistic.STRENGTH;
            case CRIT_CHANCE -> ItemStatistic.CRITICAL_CHANCE;
            case CRIT_DAMAGE -> ItemStatistic.CRITICAL_DAMAGE;
            case ATTACK_SPEED -> ItemStatistic.BONUS_ATTACK_SPEED;
            case FEROCITY -> ItemStatistic.FEROCITY;
            case SWING_RANGE -> ItemStatistic.SWING_RANGE;
            case INTELLIGENCE -> ItemStatistic.INTELLIGENCE;
            case ABILITY_DAMAGE -> ItemStatistic.ABILITY_DAMAGE;
            case HEALTH_REGEN -> ItemStatistic.HEALTH_REGENERATION;
            case PULL -> ItemStatistic.PULL;
            case VITALITY -> ItemStatistic.VITALITY;
            case MENDING -> ItemStatistic.MENDING;
            case SPEED -> ItemStatistic.SPEED;
            case MAGIC_FIND -> ItemStatistic.MAGIC_FIND;
            case PET_LUCK -> ItemStatistic.PET_LUCK;
            case FISHING_SPEED -> ItemStatistic.FISHING_SPEED;
            case SEA_CREATURE_CHANCE -> ItemStatistic.SEA_CREATURE_CHANCE;
            case DOUBLE_HOOK_CHANCE -> ItemStatistic.DOUBLE_HOOK_CHANCE;
        };
    }

    private TablistEntry entry(String text) {
        return new TablistEntry(Component.text(text), TablistSkinRegistry.GRAY);
    }

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer player) {
        return getEntries((SkyBlockPlayer) player);
    }
}
