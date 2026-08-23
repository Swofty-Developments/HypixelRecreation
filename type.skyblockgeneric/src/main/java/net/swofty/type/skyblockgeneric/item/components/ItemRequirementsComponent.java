package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@Getter
public class ItemRequirementsComponent extends SkyBlockItemComponent {
    private final List<Requirement> requirements;

    public ItemRequirementsComponent(List<Requirement> requirements) {
        this.requirements = List.copyOf(requirements);
    }

    public boolean canUse(SkyBlockPlayer player) {
        return requirements.stream().allMatch(requirement -> requirement.isMet(player));
    }

    public boolean ensureCanUse(SkyBlockPlayer player) {
        Requirement unmet = requirements.stream()
                .filter(requirement -> !requirement.isMet(player))
                .findFirst()
                .orElse(null);
        if (unmet == null) return true;
        player.sendMessage("<c>You do not meet this item's {} requirement!", unmet.display());
        return false;
    }

    public record Requirement(Type type, String category, int level, String description) {
        public boolean isMet(SkyBlockPlayer player) {
            if (type != Type.SKILL) return true;
            try {
                SkillCategories skill = SkillCategories.valueOf(category);
                return player.getSkills().getCurrentLevel(skill) >= level;
            } catch (IllegalArgumentException ignored) {
                return true;
            }
        }

        public String display() {
            return switch (type) {
                case SKILL -> category.substring(0, 1) + category.substring(1).toLowerCase() + " " + level;
                case DUNGEON_SKILL -> category + " level " + level;
                case DUNGEON_TIER -> category + " floor " + level + " completion";
                case PENDING -> description;
            };
        }
    }

    public enum Type {
        SKILL,
        DUNGEON_SKILL,
        DUNGEON_TIER,
        PENDING
    }
}
