package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.leaderboard.MapLeaderboardTracked;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

import java.util.*;

public class DatapointCollection extends SkyBlockDatapoint<DatapointCollection.PlayerCollection>
        implements MapLeaderboardTracked {

    private static final String LEADERBOARD_PREFIX = "skyblock:collection";

    public DatapointCollection(String key, DatapointCollection.PlayerCollection value) {
        super(key, value, new Serializer<>() {
            @Override
            public String serialize(DatapointCollection.PlayerCollection value) {
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                for (Map.Entry<ItemType, Integer> entry : value.getItems().entrySet()) {
                    sb.append("\"").append(entry.getKey().toString()).append("\":").append(entry.getValue()).append(",");
                }
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("}");
                return sb.toString();
            }

            @Override
            public DatapointCollection.PlayerCollection deserialize(String json) {
                JSONObject jsonObject = new JSONObject(json);
                Map<ItemType, Integer> items = new HashMap<>();
                for (String key : jsonObject.keySet()) {
                    items.put(ItemType.valueOf(key), jsonObject.getInt(key));
                }
                return new PlayerCollection(items);
            }

            @Override
            public PlayerCollection clone(PlayerCollection value) {
                PlayerCollection toReturn = new PlayerCollection(new HashMap<>());

                for (Map.Entry<ItemType, Integer> entry : value.getItems().entrySet()) {
                    toReturn.getItems().put(entry.getKey(), entry.getValue());
                }

                return toReturn;
            }
        });
    }

    public DatapointCollection(String key) {
        this(key, new PlayerCollection(new HashMap<>()));
    }

    // ============ MapLeaderboardTracked Implementation ============

    @Override
    public String getLeaderboardPrefix() {
        return LEADERBOARD_PREFIX;
    }

    @Override
    public Map<String, Double> getAllLeaderboardScores() {
        Map<String, Double> scores = new HashMap<>();
        if (value == null || value.getItems() == null) {
            return scores;
        }

        for (Map.Entry<ItemType, Integer> entry : value.getItems().entrySet()) {
            // Only include items that are part of a collection category
            if (CollectionCategories.getCategory(entry.getKey()) != null) {
                scores.put(entry.getKey().name(), entry.getValue().doubleValue());
            }
        }
        return scores;
    }

    @Override
    public Map<String, Double> getChangedScores(Object oldValue, Object newValue) {
        // If either value is null, return all scores
        if (!(oldValue instanceof PlayerCollection oldCollection) ||
            !(newValue instanceof PlayerCollection newCollection)) {
            return getAllLeaderboardScores();
        }

        Map<String, Double> changed = new HashMap<>();
        Map<ItemType, Map.Entry<Integer, Integer>> diffs =
            PlayerCollection.getDifferentValues(oldCollection, newCollection);

        for (Map.Entry<ItemType, Map.Entry<Integer, Integer>> diff : diffs.entrySet()) {
            // Only include items that are part of a collection category
            if (CollectionCategories.getCategory(diff.getKey()) != null) {
                changed.put(diff.getKey().name(), diff.getValue().getValue().doubleValue());
            }
        }
        return changed;
    }

    // ============ End MapLeaderboardTracked Implementation ============

    @Getter
    @AllArgsConstructor
    public static class PlayerCollection {
        private Map<ItemType, Integer> items;

        public CollectionCategory.ItemCollectionReward getReward(CollectionCategory.ItemCollection collection) {
            int collected = get(collection.type());

            for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
                if (collected < reward.requirement()) {
                    return reward;
                }
            }

            return null;
        }

        public void increase(ItemType type) {
            items.put(type, get(type) + 1);
        }

        public void increase(ItemType type, int amount) {
            items.put(type, get(type) + amount);
        }

        public void set(ItemType type, int amount) {
            items.put(type, amount);
        }

        public Integer get(ItemType type) {
            return items.getOrDefault(type, 0);
        }

        public boolean unlocked(ItemType type) {
            return get(type) > 0;
        }

        public List<String> getDisplay(List<String> lore) {
            int allCollections = CollectionCategories.getCategories().stream().mapToInt(category -> category.getCollections().length).sum();
            int unlockedCollections = (int) items.keySet().stream().filter(this::unlocked).count();

            String unlockedPercentage = String.format("%.2f", (unlockedCollections / (double) allCollections) * 100);
            lore.add("<7>Collections Unlocked: <e>" + unlockedPercentage + "<6>%");

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();
            int completedLength = (int) ((unlockedCollections / (double) allCollections) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            lore.add("<2><m>" + completedLoadingBar + "<7>" + uncompletedLoadingBar + "<r> <e>" + unlockedCollections + "<6>/<e>" + allCollections);

            return lore;
        }

        public List<String> getDisplay(List<String> lore, CollectionCategory.ItemCollection collection) {
            CollectionCategory.ItemCollectionReward reward = getReward(collection);
            int collected = get(collection.type());
            int required = reward == null ? 0 : reward.requirement();

            if (reward == null) {
                lore.add("<c>Maxed out!");
                return lore;
            }

            String collectedPercentage = String.format("%.2f", (collected / (double) required) * 100);
            lore.add(Text.of("<7>Progress to {} {:roman}: <e>{}<6>%",
                    collection.type().getDisplayName(), collection.getPlacementOf(reward) + 1,
                    collectedPercentage).serialize());

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();
            int completedLength = (int) ((collected / (double) required) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            lore.add("<2><m>" + completedLoadingBar + "<7>" + uncompletedLoadingBar + "<r> <e>" + collected + "<6>/<e>" + required);

            if (reward.unlocks().length > 0) {
                lore.add(" ");
                reward.getDisplay(lore,
                        collection.type().getDisplayName() + " "
                                + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward) + 1) + " ");
            }

            return lore;
        }

        public List<String> getDisplay(List<String> lore, CollectionCategory.ItemCollection collection,
                                       CollectionCategory.ItemCollectionReward reward) {
            int collected = get(collection.type());

            String collectedPercentage = String.format("%.2f", Math.min(((collected / (double) reward.requirement()) * 100), 100));
            lore.add(Text.of("<7>Progress to {} {:roman}: <e>{}<6>%",
                    collection.type().getDisplayName(), collection.getPlacementOf(reward) + 1,
                    collectedPercentage).serialize());

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();

            int completedLength = (int) ((collected / (double) reward.requirement()) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            lore.add(Text.of("<2><m>{}<7>{}<r> <e>{:,}<6>/<e>{:short}",
                    completedLoadingBar, uncompletedLoadingBar, collected, reward.requirement()).serialize());

            if (reward.unlocks().length > 0) {
                lore.add(" ");
                reward.getDisplay(lore,
                        collection.type().getDisplayName() + " "
                                + StringUtility.getAsRomanNumeral(collection.getPlacementOf(reward) + 1) + " ");
            }

            return lore;
        }

        public List<String> getDisplay(List<String> lore, CollectionCategory category) {
            int allCollections = category.getCollections().length;
            int unlockedCollections = (int) items.keySet().stream().filter(
                    type -> Arrays.stream(category.getCollections()).anyMatch(collection -> collection.type() == type)
            ).filter(this::unlocked).count();

            String unlockedPercentage = String.format("%.2f", (unlockedCollections / (double) allCollections) * 100);
            lore.add("<7>Collections Unlocked: <e>" + unlockedPercentage + "<6>%");

            String baseLoadingBar = "─────────────────";
            int maxBarLength = baseLoadingBar.length();
            int completedLength = (int) ((unlockedCollections / (double) allCollections) * maxBarLength);

            String completedLoadingBar = baseLoadingBar.substring(0, Math.min(completedLength, maxBarLength));
            String uncompletedLoadingBar = baseLoadingBar.substring(Math.min(completedLoadingBar.length(), maxBarLength));

            lore.add("<2><m>" + completedLoadingBar + "<7>" + uncompletedLoadingBar + "<r> <e>" + unlockedCollections + "<6>/<e>" + allCollections);

            return lore;
        }

        public static Map<ItemType, Map.Entry<Integer, Integer>> getDifferentValues(
                DatapointCollection.PlayerCollection oldCollection, DatapointCollection.PlayerCollection newCollection) {
            Map<ItemType, Map.Entry<Integer, Integer>> toReturn = new HashMap<>();

            for (ItemType type : ItemType.values()) {
                int oldValue = oldCollection.get(type);
                int newValue = newCollection.get(type);

                if (oldValue != newValue) {
                    toReturn.put(type, new AbstractMap.SimpleEntry<>(oldValue, newValue));
                }
            }

            return toReturn;
        }
    }
}
