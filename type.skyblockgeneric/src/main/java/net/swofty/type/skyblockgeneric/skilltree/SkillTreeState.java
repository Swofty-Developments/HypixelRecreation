package net.swofty.type.skyblockgeneric.skilltree;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class SkillTreeState {
    private Map<String, Integer> levels = new HashMap<>();
    private Set<String> disabledNodes = new HashSet<>();
    private String selectedAbility;

    public int level(String nodeId) {
        return Math.max(0, levels.getOrDefault(nodeId, 0));
    }

    public void setLevel(String nodeId, int level) {
        if (level <= 0) {
            levels.remove(nodeId);
        } else {
            levels.put(nodeId, level);
        }
    }

    public boolean isEmpty() {
        return levels.isEmpty() && disabledNodes.isEmpty() && selectedAbility == null;
    }

    public boolean isEnabled(String nodeId) {
        return !disabledNodes.contains(nodeId);
    }

    public void setEnabled(String nodeId, boolean enabled) {
        if (enabled) {
            disabledNodes.remove(nodeId);
        } else {
            disabledNodes.add(nodeId);
        }
    }
}
