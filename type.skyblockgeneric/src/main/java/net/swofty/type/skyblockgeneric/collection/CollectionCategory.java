package net.swofty.type.skyblockgeneric.collection;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionComponent;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class CollectionCategory {
    public abstract Material getDisplayIcon();

    public abstract Text getName();

    public abstract ItemCollection[] getCollections();

    public ItemCollection getCollection(ItemType type) {
        for (ItemCollection collection : getCollections()) {
            if (collection.type() == type) {
                return collection;
            }
        }
        return null;
    }

    public record ItemCollection(ItemType type, ItemCollectionReward... rewards) {
        public int getPlacementOf(ItemCollectionReward reward) {
            for (int i = 0; i < rewards.length; i++) {
                if (rewards[i].requirement == reward.requirement) {
                    return i;
                }
            }
            return -1;
        }
    }

    public record ItemCollectionReward(int requirement, Unlock... unlocks) {
        public List<String> getDisplay(List<String> lore, String itemDisplay) {
            lore.add("<7>" + itemDisplay + "Rewards:");

            Arrays.stream(unlocks).forEach(unlock -> {
                switch (unlock.type()) {
                    case RECIPE -> {
                        ((UnlockRecipe) unlock).getRecipes().forEach(recipe -> {
                            if (recipe.getResult().getDisplayName().contains("Minion")) {
                                if (lore.stream().noneMatch(line -> line.contains("Minion") && line.contains("Recipes"))) {
                                    lore.add("<9>  " + StringUtility.toNormalCase(
                                            MinionRegistry.fromItemType(recipe.getResult().getAttributeHandler()
                                                    .getPotentialType()).name()) + " Minion <7>Recipes");
                                }
                            } else {
                                lore.add("<7>  <e>" + recipe.getResult().getDisplayName() + " <7>Recipe");
                            }
                        });
                    }
                    case CUSTOM_AWARD -> {
                        lore.add("<7>  " + ((UnlockCustomAward) unlock).getAward().getDisplay());
                    }
                }
            });
            Arrays.stream(unlocks).forEach(unlock -> {
                if (Objects.requireNonNull(unlock.type()) == Unlock.UnlockType.XP) {
                    lore.add("<7>  <8>+<b>" + ((UnlockXP) unlock).xp() + " SkyBlock XP");
                }
            });

            return lore;
        }
    }

    public abstract static class Unlock {
        public abstract UnlockType type();
        public abstract ItemStack.Builder getDisplay(SkyBlockPlayer player);

        public enum UnlockType {
            RECIPE,
            XP,
            CUSTOM_AWARD
        }
    }

    public abstract static class UnlockRecipe extends Unlock {
        @Override
        public UnlockType type() {
            return UnlockType.RECIPE;
        }

        @Override
        public ItemStack.Builder getDisplay(SkyBlockPlayer player) {
            SkyBlockItem skyBlockItem = getRecipes().getFirst().getResult();
            ItemStack.Builder updatedItem = new NonPlayerItemUpdater(getRecipes().getFirst().getResult()).getUpdatedItem();

            if (skyBlockItem.hasComponent(MinionComponent.class)) {
                String material = StringUtility.toNormalCase(skyBlockItem.getAttributeHandler().getMinionType().toString());
                ItemStacks.name(updatedItem, "<9>{} Minion Recipes", material);
                return ItemStacks.lore(updatedItem, List.of(
                        Text.of("<7>Place this minion and it will start"),
                        Text.of("<7>generating and mining {}!", material),
                        Text.of("<7>Requires an open area to place"),
                        Text.of("<7>{}.", material),
                        Text.empty(),
                        Text.of("<e>Click to view recipes!")));
            }

            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(" "));
            int others = getRecipes().size() - 1;

            if (others > 0) {
                lore.add(Text.of("<8>+{} more recipes", others));
            }
            lore.add(Text.of("<e>Click to view recipe"));

            return ItemStacks.appendLore(updatedItem, lore);
        }

        public abstract SkyBlockRecipe<?> getRecipe();

        public List<SkyBlockRecipe<?>> getRecipes() {
            if (getRecipe() != null) {
                return List.of(getRecipe());
            }
            return List.of();
        }
    }

    public abstract static class UnlockXP extends Unlock {
        @Override
        public UnlockType type() {
            return UnlockType.XP;
        }

        @Override
        public ItemStack.Builder getDisplay(SkyBlockPlayer player) {
            return ItemStacks.item(Material.EXPERIENCE_BOTTLE, "<8>+<b>{} SkyBlock XP", xp());
        }

        public abstract int xp();
    }

    public abstract static class UnlockCustomAward extends Unlock {
        @Override
        public UnlockType type() {
            return UnlockType.CUSTOM_AWARD;
        }

        @Override
        public ItemStack.Builder getDisplay(SkyBlockPlayer player) {
            return ItemStacks.item(Material.PURPLE_STAINED_GLASS_PANE, 1, getAward().getDisplay(), List.of());
        }

        public abstract CustomCollectionAward getAward();
    }
}
