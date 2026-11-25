package net.swofty.type.skyblockgeneric.skilltree;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillTreeDefinition {
    private final SkillTreeType type;
    private final String displayName;
    private final String headTexture;
    private final int maxY;
    private final List<TreeNodeDefinition> nodes;
    private final List<TreeTierDefinition> tiers;
    private final Map<String, TreeNodeDefinition> nodesById;
    private final Map<Integer, TreeNodeDefinition> nodesByPosition;

    public SkillTreeDefinition(
            SkillTreeType type,
            String displayName,
            String headTexture,
            int maxY,
            List<TreeNodeDefinition> nodes,
            List<TreeTierDefinition> tiers
    ) {
        this.type = type;
        this.displayName = displayName;
        this.headTexture = headTexture;
        this.maxY = maxY;
        this.nodes = List.copyOf(nodes);
        this.tiers = List.copyOf(tiers);
        this.nodesById = new HashMap<>();
        this.nodesByPosition = new HashMap<>();
        for (TreeNodeDefinition node : nodes) {
            if (nodesById.put(node.id(), node) != null) {
                throw new IllegalArgumentException("Duplicate skill tree node: " + node.id());
            }
            int position = position(node.x(), node.y());
            if (nodesByPosition.put(position, node) != null) {
                throw new IllegalArgumentException("Duplicate skill tree position: " + node.x() + "," + node.y());
            }
        }
    }

    public SkillTreeType type() {
        return type;
    }

    public String displayName() {
        return displayName;
    }

    public String headTexture() {
        return headTexture;
    }

    public int maxY() {
        return maxY;
    }

    public List<TreeNodeDefinition> nodes() {
        return nodes;
    }

    public TreeNodeDefinition node(String id) {
        return nodesById.get(id);
    }

    public TreeNodeDefinition nodeAt(int x, int y) {
        return nodesByPosition.get(position(x, y));
    }

    public TreeTierDefinition tier(int tier) {
        return tiers.stream().filter(definition -> definition.tier() == tier).findFirst().orElse(null);
    }

    public int tierForY(int y) {
        return maxY - y + 1;
    }

    public int clampScroll(int scroll) {
        return Math.clamp(scroll, 0, maxY);
    }

    private int position(int x, int y) {
        return y * 7 + x;
    }
}
