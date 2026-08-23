package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.event.custom.BestiaryUpdateEvent;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary.BestiaryEntry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class DatapointBestiary extends SkyBlockDatapoint<DatapointBestiary.PlayerBestiary> {

    public DatapointBestiary(String key, PlayerBestiary value) {
        super(key, value, new Serializer<>() {
            @Override
            public String serialize(PlayerBestiary value) {
                JSONObject jsonObject = new JSONObject(value.mobs);
                return jsonObject.toString();
            }

            @Override
            public PlayerBestiary deserialize(String json) {
                JSONObject jsonObject = new JSONObject(json);
                Map<String, Integer> mobs = new HashMap<>();

                for (String key : jsonObject.keySet()) {
                    mobs.put(key, jsonObject.getInt(key));
                }

                return new PlayerBestiary(mobs);
            }

            @Override
            public PlayerBestiary clone(PlayerBestiary value) {
                return new PlayerBestiary(value.mobs == null ? new HashMap<>() : new HashMap<>(value.mobs));
            }
        });
    }

    public DatapointBestiary(String key) {
        this(key, new PlayerBestiary());
    }

    @NoArgsConstructor
    @Getter
    public static class PlayerBestiary {
        private Map<String, Integer> mobs = new HashMap<>();
        @Setter
        private SkyBlockPlayer attachedPlayer = null;

        BestiaryData bestiaryData = new BestiaryData();

        public PlayerBestiary(Map<String, Integer> mobs) {
            this.mobs = mobs;
        }

        public void setRaw(BestiaryMob mob, int value) {
            int oldValue = getAmount(mob);
            mobs.put(mob.getMobID(), value);

            if (attachedPlayer != null) {
                HypixelEventHandler.callCustomEvent(new BestiaryUpdateEvent(
                        attachedPlayer,
                        mob,
                        oldValue,
                        value
                ));
            }
        }

        public void set(BestiaryMob mob, int amount) {
            setRaw(mob, amount);
        }

        public void increase(BestiaryMob mob, Integer amount) {
            setRaw(mob, getAmount(mob) + amount);
        }

        public void decrease(BestiaryMob mob, Integer amount) {
            setRaw(mob, getAmount(mob) - amount);
        }

        public Integer getAmount(BestiaryMob mob) {
            return mobs.getOrDefault(mob.getMobID(), 0);
        }

        public Integer getAmount(List<BestiaryMob> mobs) {
            int kills = 0;
            for (BestiaryMob mob : mobs) {
                kills += getAmount(mob);
            }
            return kills;
        }

        public List<String> getMobDisplay(List<String> lore, int kills, BestiaryMob mob, BestiaryEntry bestiaryEntry) {

            int bracket = mob.getBestiaryBracket();
            int tier = bestiaryData.getCurrentBestiaryTier(mob, kills);
            double currentProgress = bestiaryData.getKillsToNextTier(mob, kills);
            double currentRequirement = bestiaryData.getTotalKillsForNextTier(bracket, tier + 1);
            double totalRequirement = bestiaryData.getTotalKillsForMaxTier(mob);
            DatapointDeaths.PlayerDeaths playerDeaths = attachedPlayer.getDeathData();
            int deaths = 0;

            for (BestiaryMob bestiaryMob : bestiaryEntry.getMobs()) {
                deaths += playerDeaths.getAmount(bestiaryMob.getMobID());
            }

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();

            List<MobType> mobtypes = mob.getMobTypes();

            if (mobtypes.size() == 1) {
                lore.add("<7>Mob Type: " + mobtypes.getFirst().getFullDisplayName());
                lore.add("");
            } else if (mobtypes.size() > 1) {
                lore.add("<7>Mob Types: " + mobtypes.stream()
                        .map(MobType::getFullDisplayName)
                        .collect(Collectors.joining("<7>, ")));
                lore.add("");
            }

            lore.add("<7>" + bestiaryEntry.getDescription());
            lore.add("");
            lore.add("<7>Kills: <a>" + kills);
            lore.add("<7>Deaths: <a>" + deaths);
            lore.add("");

            if (tier > 0) {
                bestiaryData.getTotalBonuses(lore, bestiaryEntry.getName(), tier);
                lore.add("");
            }

            // Current tier progress
            if (tier < mob.getMaxBestiaryTier()) {
                int unlockedPercentage = (int) (currentProgress / currentRequirement * 100);
                lore.add(Text.of("<7>Progress to Tier {:roman} <b>{}%", tier + 1, unlockedPercentage).serialize());

                int completedLength = (int) Math.round((currentProgress / currentRequirement) * maxBarLength);

                String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
                String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

                lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                        completedLoadingBar, uncompletedLoadingBar, currentProgress, currentRequirement).serialize());
                lore.add("");
            }

            // Total kill progress*
            int totalUnlockedPercentage = (int) (kills / totalRequirement * 100);
            if (tier < mob.getMaxBestiaryTier()) {
                lore.add("<7>Overall Progress: <b>" + totalUnlockedPercentage + "%");
            } else {
                lore.add("<7>Overall Progress: <b>" + totalUnlockedPercentage + "% <7>(<c><l>MAX!</l></c>)");
            }

            int totalCompletedLength = (int) Math.round((kills / totalRequirement) * maxBarLength);
            String totalCompletedBar = baseLoadingBar.substring(0, Math.min(totalCompletedLength, maxBarLength));
            String totalUncompletedBar = baseLoadingBar.substring(Math.min(totalCompletedBar.length(), maxBarLength));

            lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                    totalCompletedBar, totalUncompletedBar, kills, totalRequirement).serialize());

            if (mob.getMaxBestiaryTier() > tier) {
                lore.add(Text.of("<8>Capped at Tier {:roman}", mob.getMaxBestiaryTier()).serialize());
            }

            lore.add("");

            if (tier < mob.getMaxBestiaryTier()) {
                bestiaryData.getNextBonuses(lore, bestiaryEntry.getName(), tier + 1);
                lore.add("");
            }

            return lore;
        }

        public List<String> getTotalDisplay(List<String> lore) {

            List<BestiaryEntry> allEntries = new ArrayList<>();
            double totalFamilies = 0;
            double familiesFound = 0;
            double familiesCompleted = 0;

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();

            for (BestiaryCategories category : BestiaryCategories.values()) {
                allEntries.addAll(Arrays.asList(category.getEntries()));
            }

            totalFamilies = allEntries.size();

            for (BestiaryEntry entry : allEntries) {
                int kills = getAmount(entry.getMobs());
                if (kills > 0) familiesFound++;
                if (kills >= bestiaryData.getTotalKillsForMaxTier(entry.getMobs().getFirst())) familiesCompleted++;
            }

            lore.add("<7>The Bestiary is a compendium of");
            lore.add("<7>mobs in SkyBlock. It contains detailed");
            lore.add("<7>information on loot drops, your mob");
            lore.add("<7>stats, and more!");
            lore.add("");
            lore.add("<7>Kill mobs within <a>Families <7>to progress");
            lore.add("<7>and earn <a>rewards<7>, including <b>✯ Magic");
            lore.add("<b>Find <7>bonuses towards mobs in the");
            lore.add("<7>Family.");
            lore.add("");

            // Families found
            int unlockedPercentage = (int) (familiesFound / totalFamilies * 100);
            if (familiesFound != totalFamilies) {
                lore.add("<7>Families Found: <e>" + unlockedPercentage + "%");
            } else {
                lore.add("<7>Families Found: <e>" + unlockedPercentage + "% <7>(<c><l>MAX!</l></c>)");
            }

            int completedLength = (int) Math.round((familiesFound / totalFamilies) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                    completedLoadingBar, uncompletedLoadingBar, familiesFound, totalFamilies).serialize());
            lore.add("");

            // Families completed
            int totalUnlockedPercentage = (int) (familiesCompleted / totalFamilies * 100);
            if (familiesCompleted != totalFamilies) {
                lore.add("<7>Families Completed: <e>" + totalUnlockedPercentage + "%");
            } else {
                lore.add("<7>Families Completed: <e>" + totalUnlockedPercentage + "% <7>(<c><l>MAX!</l></c>)");
            }

            int totalCompletedLength = (int) Math.round((familiesCompleted / totalFamilies) * maxBarLength);
            String totalCompletedBar = baseLoadingBar.substring(0, Math.min(totalCompletedLength, maxBarLength));
            String totalUncompletedBar = baseLoadingBar.substring(Math.min(totalCompletedBar.length(), maxBarLength));

            lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                    totalCompletedBar, totalUncompletedBar, familiesCompleted, totalFamilies).serialize());

            return lore;
        }
    }
}
