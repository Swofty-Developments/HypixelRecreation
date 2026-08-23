package net.swofty.type.bedwarsgame.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.v2.SharedContext;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GUITeamChest implements View<GUITeamChest.State> {

    private final BedWarsMapsConfig.TeamKey teamKey;

    public GUITeamChest(BedWarsMapsConfig.TeamKey teamKey) {
        this.teamKey = teamKey;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>(Text.of("{}'s Team Chest", teamKey.getName()), InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        for (int slot = 0; slot < InventoryType.CHEST_3_ROW.getSize(); slot++) {
            layout.editable(slot);
        }
        layout.allowHotkey(true);
    }

    @Override
    public boolean onBottomClick(ClickContext<State> click, ViewContext ctx) {
        return true;
    }

    public static void open(BedWarsPlayer player, BedWarsMapsConfig.TeamKey teamKey) {
        BedWarsGame game = player.getGame();
        if (game == null) {
            return;
        }

        String gameId = player.getGameId();
        if (gameId == null) {
            return;
        }

        String contextId = "bedwars-team-chest-" + gameId + "-" + teamKey.name();
        ViewNavigator navigator = ViewNavigator.get(player);

        if (SharedContext.exists(contextId)) {
            navigator.joinShared(new GUITeamChest(teamKey), contextId);
            return;
        }

        SharedContext<State> sharedContext = SharedContext.create(contextId, new State(teamKey));

        Map<Integer, ItemStack> teamChest = game.getTeamChests().get(teamKey);
        if (teamChest != null) {
            sharedContext.setSlotItems(teamChest);
        }

        sharedContext.onSlotChange(change -> {
            Map<Integer, ItemStack> gameTeamChest = game.getTeamChests().computeIfAbsent(teamKey, k -> new ConcurrentHashMap<>());
            if (change.newItem().isAir()) {
                gameTeamChest.remove(change.slot());
            } else {
                gameTeamChest.put(change.slot(), change.newItem());
            }
        });

        navigator.pushShared(new GUITeamChest(teamKey), sharedContext);
    }

    public record State(BedWarsMapsConfig.TeamKey teamKey) {
    }
}
