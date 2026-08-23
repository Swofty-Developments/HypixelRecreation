package net.swofty.type.bedwarslobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.bedwars.BedWarsDreamRotation;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.RejoinGameProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.lobby.GameQueueValidator;
import net.swofty.type.lobby.LobbyOrchestratorConnector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GUIPlayDreamBedWars extends StatelessView {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);
    private static final String LUCKY_HEAD = "50d8f863e9b42653e642711ee8b854dd8f9463ef4bfcde7db9776daadb532b";

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Play Bed Wars", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 40);

        layout.slot(4,
            (_, _) -> ItemStacks.item(Material.CLOCK, """
                <b>Dream Calendar

                <7>See what <b>Dream Modes <7>will enter
                <7>rotation next!

                <e>Click to open!"""),
            (_, viewCtx) -> viewCtx.push(new GUIDreamCalendar())
        );

        BedWarsDreamRotation.DreamMode dreamMode = BedWarsDreamRotation.current(LocalDate.now()).mode();
        List<BedWarsGameType> queueTypes = queueTypes(dreamMode);
        int[] slots = queueTypes.size() == 1 ? new int[]{22} : new int[]{21, 23};
        for (int i = 0; i < queueTypes.size(); i++) {
            BedWarsGameType type = queueTypes.get(i);
            layout.slot(slots[i], (_, _) -> dreamItem(dreamMode, type), (_, viewCtx) -> queue(viewCtx, type));
        }

        layout.slot(44,
            (_, _) -> ItemStacks.item(Material.ENDER_PEARL, """
                <c>Click here to rejoin!
                <7>Click here to rejoin your Bed Wars
                <7>game if you have been disconnected
                <7>from it."""),
            (_, viewCtx) -> rejoin(viewCtx)
        );
    }

    private List<BedWarsGameType> queueTypes(BedWarsDreamRotation.DreamMode mode) {
        List<BedWarsGameType> types = new ArrayList<>();
        types.add(mode.doublesType());
        if (mode.foursType() != null && mode.foursType() != mode.doublesType()) {
            types.add(mode.foursType());
        }
        return types;
    }

    private ItemStack.Builder dreamItem(BedWarsDreamRotation.DreamMode dreamMode, BedWarsGameType type) {
        String queueName = queueDisplay(type);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>Play a game of Bed Wars {}", type.getDisplayName()));
        lore.add(Text.of("<7>{}.", queueName));
        lore.add(Text.empty());
        for (String line : type.getDescription()) {
            lore.add(Text.literal(line));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<c>Overall stats, achievements and"));
        lore.add(Text.of("<c>quests will NOT be earned in this"));
        lore.add(Text.of("<c>mode!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Bed Wars Dreams is a variety of"));
        lore.add(Text.of("<7>rotating game modes."));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to play!"));

        Text name = Text.of("<a>{} {}", type.getDisplayName(), queueName);
        if (type.getDisplayName().contains("Lucky")) {
            return ItemStacks.head(LUCKY_HEAD, amount(type), name, lore);
        }
        Material icon = Material.fromKey(type.getIconName().toLowerCase());
        if (icon == null) icon = Material.RED_BED;
        return ItemStacks.item(icon, amount(type), name, lore);
    }

    private void queue(ViewContext ctx, BedWarsGameType type) {
        var player = ctx.player();
        player.closeInventory();
        if (!GameQueueValidator.canPlayerQueue(player, new GameQueueValidator.QueueRequirements(
            "Bed Wars",
            type.getQueueModeDisplayName(),
            type.getTeamSize()
        ))) {
            return;
        }
        new LobbyOrchestratorConnector(player).sendToGame(ServerType.BEDWARS_GAME, type.toString());
    }

    private void rejoin(ViewContext ctx) {
        var player = ctx.player();
        player.closeInventory();

        RejoinGameProtocol.RejoinGameRequest request =
            new RejoinGameProtocol.RejoinGameRequest(player.getUuid());

        ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
            if (!(response instanceof RejoinGameProtocol.RejoinGameResponse resp)) {
                player.sendMessage("<c>Failed to check for active games. Please try again.");
                return;
            }

            if (!resp.hasActiveGame() || resp.server() == null) {
                player.sendMessage("<c>You don't have an active game to rejoin!");
                return;
            }

            player.sendMessage("<a>Rejoining your game...");
            ChooseGameProtocol.ChooseGameMessage chooseMsg =
                new ChooseGameProtocol.ChooseGameMessage(player.getUuid(), resp.server(), resp.gameId());
            ORCHESTRATOR.handleRequest(chooseMsg);

            player.asProxyPlayer().transferToWithIndication(resp.server().uuid());
            player.getAchievementHandler().completeAchievement("bedwars.rejoining_the_dream");
        }).exceptionally(throwable -> {
            player.sendMessage("<c>Failed to rejoin: {}", throwable.getMessage());
            return null;
        });
    }

    private String queueDisplay(BedWarsGameType type) {
        if (type == BedWarsGameType.CASTLE) return "40v40";
        if (type.getTeamSize() == 4 && type.getTeams() == 4) return "4v4v4v4";
        if (type.getTeamSize() == 2) return "Doubles";
        if (type.getTeamSize() == 1) return "Solo";
        return type.getDisplayName();
    }

    private int amount(BedWarsGameType type) {
        return Math.max(1, type.getTeamSize());
    }

}
