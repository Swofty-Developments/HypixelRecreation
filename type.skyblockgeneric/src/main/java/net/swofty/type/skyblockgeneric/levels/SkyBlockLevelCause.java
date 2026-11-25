package net.swofty.type.skyblockgeneric.levels;

import lombok.NonNull;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.collection.CollectionCategory;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AccessoryComponent;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.levels.causes.*;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.museum.MuseumRewards;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulExchangeLevels;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkyBlockLevelCause {
    private static final Map<String, SkyBlockLevelCauseAbstr> CAUSES = new HashMap<>();

    public static void initializeCauses() {
        // Register all Skill causes
        for (SkillCategories category : SkillCategories.values()) {
            for (int i = 1; i <= category.asCategory().getHighestLevel(); i++) {
                CAUSES.put("skill-" + category.name() + "-" + i, new SkillLevelCause(category, i));
            }
        }

        // Register all accessory causes
        for (ItemType itemTypeLinker : ItemType.values()) {
            if (new SkyBlockItem(itemTypeLinker).hasComponent(AccessoryComponent.class)) {
                CAUSES.put("accessory-" + itemTypeLinker.name(), new NewAccessoryLevelCause(itemTypeLinker));
            }
        }

        // Register all level causes up to 500
        for (int i = 0; i <= 500; i++) {
            CAUSES.put("level-" + i, new LevelCause(i));
        }

        for (int dose = 1; dose <= 3; dose++) {
            CAUSES.put("metaphysical-serum-" + dose, new MetaphysicalSerumLevelCause(dose));
        }

        // Register all collection causes
        for (ItemType itemTypeLinker : ItemType.values()) {
            CollectionCategory category = CollectionCategories.getCategory(itemTypeLinker);
            if (category == null) continue;

            for (CollectionCategory.ItemCollection collection : category.getCollections()) {
                for (CollectionCategory.ItemCollectionReward reward : collection.rewards()) {
                    CAUSES.put("collection-" + itemTypeLinker.name() + "-" + collection.getPlacementOf(reward),
                            new CollectionLevelCause(itemTypeLinker, collection.getPlacementOf(reward)));
                }
            }
        }

        // Register all Fairy Exchange rewards
        for (int i = 0; i <= FairySoulExchangeLevels.values().length; i++) {
            CAUSES.put("fairy-soul-exchange-" + i, new FairySoulExchangeLevelCause(i));
        }

        // Register all collected mob type rewards
        for (MobType mobType : MobType.values()) {
            CAUSES.put("mob-type-" + mobType.name(), new CollectedMobTypesCause(mobType.name()));
        }

        // Register all Mission rewards
        for (String missionID : MissionData.getAllMissionIDs()) {
            CAUSES.put("mission-" + missionID, new MissionLevelCause(missionID));
        }

        // Register all Museum rewards
        for (MuseumRewards reward : MuseumRewards.values()) {
            CAUSES.put("museum-" + reward.name(), new MuseumLevelCause(reward));
        }
    }

    public static Map<String, SkyBlockLevelCauseAbstr> getCauses() {
        return CAUSES;
    }

    public static Double getTotalXP() {
        double total = 0;
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            total += cause.xpReward();
        }
        return total;
    }

    public static FairySoulExchangeLevelCause getFairySoulExchangeCause(int exchangeCount) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof FairySoulExchangeLevelCause exchangeCause) {
                if (exchangeCause.getExchangeCount() == exchangeCount) {
                    return exchangeCause;
                }
            }
        }
        return null;
    }

    public static MissionLevelCause getMissionCause(String missionKey) {
        for (String key : CAUSES.keySet()) {
            if (CAUSES.get(key) instanceof MissionLevelCause missionCause) {
                if (missionCause.getMissionKey().equals(missionKey)) {
                    return missionCause;
                }
            }
        }
        return null;
    }

    public static MuseumLevelCause getMuseumCause(MuseumRewards reward) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof MuseumLevelCause museumCause) {
                if (museumCause.getReward() == reward) {
                    return museumCause;
                }
            }
        }
        return null;
    }

    public static CollectionLevelCause getCollectionCause(ItemType itemTypeLinker, int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof CollectionLevelCause collectionCause) {
                if (collectionCause.getType() == itemTypeLinker && collectionCause.getLevel() == level) {
                    return collectionCause;
                }
            }
        }
        return null;
    }

    public static int getAmountOfCauses() {
        return CAUSES.size();
    }

    public static LevelCause getLevelCause(int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof LevelCause levelCause) {
                if (levelCause.getLevel() == level) {
                    return levelCause;
                }
            }
        }
        return null;
    }

    public static MetaphysicalSerumLevelCause getMetaphysicalSerumCause(int dose) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof MetaphysicalSerumLevelCause serumCause && serumCause.getDose() == dose) {
                return serumCause;
            }
        }
        return null;
    }

    public static NewAccessoryLevelCause getAccessoryCause(ItemType itemTypeLinker) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof NewAccessoryLevelCause accessoryCause) {
                if (accessoryCause.itemTypeLinker == itemTypeLinker) {
                    return accessoryCause;
                }
            }
        }
        return null;
    }

    public static SkillLevelCause getSkillCause(SkillCategories category, int level) {
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof SkillLevelCause skillCause) {
                if (skillCause.getCategory() == category && skillCause.getLevel() == level) {
                    return skillCause;
                }
            }
        }
        return null;
    }

    public static @Nullable SkyBlockLevelCauseAbstr getCause(String key) {
        return CAUSES.get(key);
    }

    public static String getKey(@NonNull SkyBlockLevelCauseAbstr cause) {
        for (Map.Entry<String, SkyBlockLevelCauseAbstr> entry : CAUSES.entrySet()) {
            if (entry.getValue() == cause) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Cause not found " + cause.getClass().getSimpleName());
    }

    public static List<SkyBlockLevelCauseAbstr> getSkillCauses(SkillCategories category, int levelUpToInclusive) {
        List<SkyBlockLevelCauseAbstr> causes = new ArrayList<>();
        for (SkyBlockLevelCauseAbstr cause : CAUSES.values()) {
            if (cause instanceof SkillLevelCause skillCause) {
                if (skillCause.getCategory() == category && skillCause.getLevel() <= levelUpToInclusive) {
                    causes.add(skillCause);
                }
            }
        }
        return causes;
    }
}
