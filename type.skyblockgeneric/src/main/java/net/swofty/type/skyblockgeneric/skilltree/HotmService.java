package net.swofty.type.skyblockgeneric.skilltree;

import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class HotmService {
    private static final SkillTreeDefinition DEFINITION = SkillTreeRegistry.get(SkillTreeType.HOTM);

    private HotmService() {
    }

    public static DatapointHOTM.PlayerHOTMData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
    }

    public static SkillTreeState activeTree(SkyBlockPlayer player) {
        int slot = LoadoutManager.activeSlot(player, SkillTreeType.HOTM);
        return data(player).getTreeState(slot);
    }

    public static int activeTreeSlot(SkyBlockPlayer player) {
        return LoadoutManager.activeSlot(player, SkillTreeType.HOTM);
    }

    public static TreeNodeDefinition node(String nodeId) {
        return DEFINITION.node(nodeId);
    }

    public static int level(SkyBlockPlayer player, TreeNodeDefinition node) {
        return activeTree(player).level(node.id());
    }

    public static boolean isSelected(SkyBlockPlayer player, TreeNodeDefinition node) {
        return node.ability() && node.id().equals(activeTree(player).getSelectedAbility());
    }

    public static boolean isEnabled(SkyBlockPlayer player, TreeNodeDefinition node) {
        SkillTreeState state = activeTree(player);
        return state.level(node.id()) > 0 && state.isEnabled(node.id());
    }

    public static boolean available(SkyBlockPlayer player, TreeNodeDefinition node) {
        if (data(player).getTier() < DEFINITION.tierForY(node.y())) return false;
        SkillTreeState state = activeTree(player);
        for (String requirement : node.requirements()) {
            if (state.level(requirement) <= 0) return false;
        }
        return true;
    }

    public static List<String> missingRequirements(SkyBlockPlayer player, TreeNodeDefinition node) {
        List<String> missing = new ArrayList<>();
        int requiredTier = DEFINITION.tierForY(node.y());
        if (data(player).getTier() < requiredTier) missing.add("Heart of the Mountain " + requiredTier);

        SkillTreeState state = activeTree(player);
        for (String requirement : node.requirements()) {
            if (state.level(requirement) <= 0) {
                TreeNodeDefinition requiredNode = DEFINITION.node(requirement);
                missing.add(requiredNode == null ? requirement : requiredNode.name());
            }
        }
        return missing;
    }

    public static long cost(SkyBlockPlayer player, TreeNodeDefinition node) {
        int level = level(player, node);
        return level == 0 ? 0 : node.cost(level);
    }

    public static TreePowder costPowder(SkyBlockPlayer player, TreeNodeDefinition node) {
        return node.powder(level(player, node));
    }

    public static boolean canAffordNextLevel(SkyBlockPlayer player, TreeNodeDefinition node) {
        int level = level(player, node);
        if (level == 0) return data(player).getAvailableTokens() > 0;
        return data(player).getPowder(node.powder(level)) >= node.cost(level);
    }

    public static int upgrade(SkyBlockPlayer player, TreeNodeDefinition node, int requestedLevels) {
        if (requestedLevels <= 0) return 0;
        DatapointHOTM.PlayerHOTMData data = data(player);
        SkillTreeState state = activeTree(player);
        int changed = 0;

        while (changed < requestedLevels) {
            int current = state.level(node.id());
            if (current >= node.maxLevel()) break;
            if (current == 0) {
                if (!available(player, node) || !data.spendToken()) break;
            } else {
                long cost = node.cost(current);
                TreePowder powder = node.powder(current);
                if (cost <= 0 || !data.spendPowder(powder, cost)) break;
            }
            state.setLevel(node.id(), current + 1);
            changed++;
        }

        if (changed > 0) save(player, data);
        return changed;
    }

    public static boolean toggleAbility(SkyBlockPlayer player, TreeNodeDefinition node) {
        if (!node.ability() || level(player, node) <= 0) return false;
        DatapointHOTM.PlayerHOTMData data = data(player);
        SkillTreeState state = activeTree(player);
        state.setSelectedAbility(node.id().equals(state.getSelectedAbility()) ? null : node.id());
        save(player, data);
        return true;
    }

    public static boolean toggleNode(SkyBlockPlayer player, TreeNodeDefinition node) {
        if (level(player, node) <= 0) return false;
        if (node.ability()) return toggleAbility(player, node);
        DatapointHOTM.PlayerHOTMData data = data(player);
        SkillTreeState state = activeTree(player);
        state.setEnabled(node.id(), !state.isEnabled(node.id()));
        save(player, data);
        return true;
    }

    public static int resetActiveTree(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = data(player);
        SkillTreeState state = activeTree(player);
        int reset = 0;

        for (TreeNodeDefinition node : DEFINITION.nodes()) {
            if (node.id().equals("core_of_the_mountain")) continue;
            int level = state.level(node.id());
            if (level <= 0) continue;
            data.setTokensSpent(Math.max(0, data.getTokensSpent() - 1));
            for (int upgradeLevel = 1; upgradeLevel < level; upgradeLevel++) {
                data.addPowder(node.powder(upgradeLevel), node.cost(upgradeLevel));
            }
            state.setLevel(node.id(), 0);
            state.setEnabled(node.id(), true);
            reset++;
        }
        state.setSelectedAbility(null);
        if (reset > 0) save(player, data);
        return reset;
    }

    public static SkillTreeDefinition definition() {
        return DEFINITION;
    }

    private static void save(SkyBlockPlayer player, DatapointHOTM.PlayerHOTMData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).setValue(data);
    }
}
