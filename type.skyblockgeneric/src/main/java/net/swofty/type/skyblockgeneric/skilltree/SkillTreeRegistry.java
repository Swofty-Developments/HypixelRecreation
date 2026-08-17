package net.swofty.type.skyblockgeneric.skilltree;

import java.util.EnumMap;
import java.util.Map;

public final class SkillTreeRegistry {
    private static final Map<SkillTreeType, SkillTreeDefinition> DEFINITIONS = new EnumMap<>(SkillTreeType.class);

    static {
        register(HotmSkillTree.create());
    }

    private SkillTreeRegistry() {
    }

    public static void register(SkillTreeDefinition definition) {
        if (DEFINITIONS.putIfAbsent(definition.type(), definition) != null) {
            throw new IllegalArgumentException("Skill tree already registered: " + definition.type());
        }
    }

    public static SkillTreeDefinition get(SkillTreeType type) {
        SkillTreeDefinition definition = DEFINITIONS.get(type);
        if (definition == null) {
            throw new IllegalArgumentException("No skill tree registered for " + type);
        }
        return definition;
    }
}
