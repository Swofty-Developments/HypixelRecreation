package net.swofty.type.skyblockgeneric.skilltree;

import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class TreeNodeDefinition {
    private final String id;
    private final String name;
    private final int x;
    private final int y;
    private final int maxLevel;
    private final boolean ability;
    private final TreePowder powder;
    private final Function<Integer, TreePowder> powderProvider;
    private final Function<Integer, Long> costProvider;
    private final List<String> lore;
    private final List<String> requirements;
    private final Map<String, NodeValue> values;
    private final LoreProvider loreProvider;

    private TreeNodeDefinition(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.x = builder.x;
        this.y = builder.y;
        this.maxLevel = builder.maxLevel;
        this.ability = builder.ability;
        this.powder = builder.powder;
        this.powderProvider = builder.powderProvider;
        this.costProvider = builder.costProvider;
        this.lore = List.copyOf(builder.lore);
        this.requirements = List.copyOf(builder.requirements);
        this.values = Map.copyOf(builder.values);
        this.loreProvider = builder.loreProvider;
    }

    public static Builder builder(String id, String name, int x, int y, int maxLevel) {
        return new Builder(id, name, x, y, maxLevel);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public boolean ability() {
        return ability;
    }

    public TreePowder powder(int level) {
        return powderProvider.apply(level);
    }

    public long cost(int level) {
        return costProvider.apply(level);
    }

    public List<String> requirements() {
        return requirements;
    }

    public List<String> renderLore(int level, int hotmTier, int coreLevel) {
        NodeRenderContext context = new NodeRenderContext(level, hotmTier, coreLevel);
        List<String> lines = loreProvider == null ? lore : loreProvider.lines(context);
        List<String> rendered = new ArrayList<>(lines.size());
        for (String line : lines) {
            String value = line;
            for (Map.Entry<String, NodeValue> entry : values.entrySet()) {
                value = value.replace("{" + entry.getKey() + "}", entry.getValue().resolve(context));
            }
            rendered.add(value);
        }
        return rendered;
    }

    public Material material(int level) {
        if (ability) return level > 0 ? Material.EMERALD_BLOCK : Material.COAL_BLOCK;
        if (level >= maxLevel) return Material.DIAMOND;
        if (level > 0) return Material.EMERALD;
        return Material.COAL;
    }

    @FunctionalInterface
    public interface NodeValue {
        String resolve(NodeRenderContext context);
    }

    @FunctionalInterface
    public interface LoreProvider {
        List<String> lines(NodeRenderContext context);
    }

    public record NodeRenderContext(int level, int hotmTier, int coreLevel) {
    }

    public static final class Builder {
        private final String id;
        private final String name;
        private final int x;
        private final int y;
        private final int maxLevel;
        private boolean ability;
        private TreePowder powder = TreePowder.MITHRIL;
        private Function<Integer, TreePowder> powderProvider;
        private Function<Integer, Long> costProvider = _ -> 0L;
        private List<String> lore = List.of();
        private List<String> requirements = List.of();
        private Map<String, NodeValue> values = new HashMap<>();
        private LoreProvider loreProvider;

        private Builder(String id, String name, int x, int y, int maxLevel) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.maxLevel = maxLevel;
            this.powderProvider = _ -> powder;
        }

        public Builder ability() {
            this.ability = true;
            return this;
        }

        public Builder powder(TreePowder powder) {
            this.powder = powder;
            this.powderProvider = _ -> powder;
            return this;
        }

        public Builder powder(Function<Integer, TreePowder> provider) {
            this.powderProvider = provider;
            return this;
        }

        public Builder cost(double exponent) {
            this.costProvider = level -> Math.round(Math.pow(level + 2, exponent));
            return this;
        }

        public Builder costs(long... costs) {
            this.costProvider = level -> level >= 0 && level < costs.length ? costs[level] : 0L;
            return this;
        }

        public Builder lore(String... lore) {
            this.lore = List.of(lore);
            return this;
        }

        public Builder require(String... requirements) {
            this.requirements = List.of(requirements);
            return this;
        }

        public Builder value(String key, NodeValue value) {
            this.values.put(key, value);
            return this;
        }

        public Builder customLore(LoreProvider provider) {
            this.loreProvider = provider;
            return this;
        }

        public TreeNodeDefinition build() {
            return new TreeNodeDefinition(this);
        }
    }
}
