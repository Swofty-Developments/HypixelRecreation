package net.swofty.type.skyblockgeneric.collection;

import lombok.Data;
import net.minestom.server.item.Material;
import net.swofty.commons.YamlFileUtils;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class CollectionLoader {
    private static final File COLLECTIONS_DIR = new File("./configuration/skyblock/collections");

    @Data
    public static class CollectionConfig {
        public String name;
        public String displayIcon;
        public @Nullable String displayColor;
        public List<CollectionEntry> collections;
    }

    @Data
    public static class CollectionEntry {
        public String itemType;
        public List<CollectionReward> rewards;
    }

    @Data
    public static class CollectionReward {
        public int amount;
        public List<Reward> rewards;
    }

    @Data
    public static class Reward {
        public String type;
        public RewardData data;
    }

    @Data
    public static class RewardData {
        // For recipe unlocks
        public String unlockedItemType;

        // XP data
        public Integer xp;

        // Custom award data
        public String customAward;
    }

    @Data
    public static class RecipeIngredient {
        public String type;
        public int amount;
    }

    public static CollectionCategory loadFromFile(String name) {
        try {
            // Ensure directory exists
            if (!YamlFileUtils.ensureDirectoryExists(COLLECTIONS_DIR)) {
                throw new IOException("Failed to create collections directory");
            }

            File collectionFile = new File(COLLECTIONS_DIR, name.toLowerCase() + ".yml");

            // Load the YAML file
            Yaml yaml = new Yaml();
            CollectionConfig config = yaml.loadAs(new FileReader(collectionFile), CollectionConfig.class);

            return new CollectionCategory() {
                @Override
                public Material getDisplayIcon() {
                    return Material.values().stream()
                            .filter(material -> material.key().value().equalsIgnoreCase(config.displayIcon))
                            .findFirst()
                            .orElse(Material.AIR);
                }

                @Override
                public Text getName() {
                    return config.displayColor == null
                            ? Text.literal(config.name)
                            : Text.of("<" + config.displayColor + ">{}", config.name);
                }

                @Override
                public ItemCollection[] getCollections() {
                    List<ItemCollection> collections = new ArrayList<>();
                    for (CollectionEntry entry : config.collections) {
                        ItemCollection collection = parseCollection(entry);
                        collections.add(collection);
                    }
                    return collections.toArray(new ItemCollection[0]);
                }
            };
        } catch (Exception e) {
            Logger.error(e, "Failed to load collection from file");
            return null;
        }
    }

    private static CollectionCategory.ItemCollection parseCollection(CollectionEntry entry) {
        ItemType type = ItemType.valueOf(entry.itemType);
        List<CollectionCategory.ItemCollectionReward> rewards = new ArrayList<>();

        for (CollectionReward reward : entry.rewards) {
            rewards.add(parseReward(reward));
        }

        return new CollectionCategory.ItemCollection(type, rewards.toArray(new CollectionCategory.ItemCollectionReward[0]));
    }

    private static CollectionCategory.ItemCollectionReward parseReward(CollectionReward reward) {
        List<CollectionCategory.Unlock> rewardTypes = new ArrayList<>();

        for (Reward r : reward.rewards) {
            switch (r.type.toUpperCase()) {
                case "RECIPE_UNLOCK" -> rewardTypes.add(parseRecipeUnlock(r.data));
                case "XP" -> rewardTypes.add(new CollectionCategory.UnlockXP() {
                    @Override
                    public int xp() {
                        return r.data.xp;
                    }
                });
                case "CUSTOM_AWARD" -> rewardTypes.add(new CollectionCategory.UnlockCustomAward() {
                    @Override
                    public CustomCollectionAward getAward() {
                        return CustomCollectionAward.valueOf(r.data.customAward);
                    }
                });
                default -> throw new IllegalArgumentException("Unknown reward type: " + r.type);
            }
        }

        return new CollectionCategory.ItemCollectionReward(reward.amount, rewardTypes.toArray(new CollectionCategory.Unlock[0]));
    }

    private static CollectionCategory.UnlockRecipe parseRecipeUnlock(RewardData data) {
        return new CollectionCategory.UnlockRecipe() {
            @Override
            public SkyBlockRecipe<?> getRecipe() {
                if (data.unlockedItemType == null || data.unlockedItemType.isEmpty()) {
                    throw new IllegalArgumentException("unlockedItemType cannot be null or empty for RECIPE_UNLOCK");
                }

                ItemType itemType = ItemType.valueOf(data.unlockedItemType);
                List<SkyBlockRecipe<?>> recipes = SkyBlockRecipe.getFromType(itemType);

                if (recipes.isEmpty()) {
                    SkyBlockItem item = new SkyBlockItem(data.unlockedItemType);
                    if (item.hasComponent(EnchantedComponent.class)) {
                        EnchantedComponent enchanted = item.getComponent(EnchantedComponent.class);
                        List<SkyBlockRecipe<?>> enchantedRecipes = enchanted.getRecipes();
                        if (!enchantedRecipes.isEmpty()) {
                            return enchantedRecipes.getFirst();
                        }
                    }

                    throw new RuntimeException("No recipes found for item: " + data.unlockedItemType +
                            ". Make sure the item has a CraftableComponent or EnchantedComponent with recipes defined.");
                }

                return recipes.getFirst();
            }

            @Override
            public List<SkyBlockRecipe<?>> getRecipes() {
                SkyBlockRecipe<?> recipe = getRecipe();
                return recipe != null ? List.of(recipe) : Collections.emptyList();
            }
        };
    }
}