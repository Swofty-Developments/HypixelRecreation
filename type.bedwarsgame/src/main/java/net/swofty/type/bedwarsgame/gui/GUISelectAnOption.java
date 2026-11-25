package net.swofty.type.bedwarsgame.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsTeam;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class GUISelectAnOption extends StatelessView {

    private static final int[] RESOURCE_SLOTS = {10, 12, 14, 16};
    private static final int[] TEAM_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19};

    private final String title;
    private final Function<ViewContext, List<Option>> optionSupplier;

    public GUISelectAnOption() {
        this("Select an option:", _ -> buildResourceOptions("I'm collecting"));
    }

    private GUISelectAnOption(String title, Function<ViewContext, List<Option>> optionSupplier) {
        this.title = title;
        this.optionSupplier = optionSupplier;
    }

    public static GUISelectAnOption forResourceCommunication(String prefix) {
        return new GUISelectAnOption("Select an option:", _ -> buildResourceOptions(prefix));
    }

    public static GUISelectAnOption forTeamCommunication(String prefix) {
        return new GUISelectAnOption("Select a team:", ctx -> buildTeamOptions(ctx, prefix));
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withString((_, _) -> title, InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        List<Option> options = optionSupplier.apply(ctx);
        if (options.isEmpty()) {
            layout.slot(13, ItemStacks.item(Material.BARRIER, """
                <c>No options available
                <7>There are no valid options
                <7>for this selection."""));
        } else {
            for (Option option : options) {
                layout.slot(option.slot(), buildOptionItem(option), (click, _) -> {
                    if (!(click.player() instanceof BedWarsPlayer player)) {
                        return;
                    }

                    GUIQuickCommunications.sendTeamQuickMessage(player, option.message());
                    GUIQuickCommunications.playClickSound(player);
                    player.closeInventory();
                });
            }
        }

        Components.back(layout, 31, ctx);
    }

    private static ItemStack.Builder buildOptionItem(Option option) {
        return ItemStacks.item(option.icon(), 1, option.displayName(), List.of(
            Text.of("<7>Click to send the message: \"{}<7>\"", option.message()),
            Text.of("<7>to your teammates!"),
            Text.empty(),
            Text.of("<e>Click to send!")
        ));
    }

    private static List<Option> buildResourceOptions(String prefix) {
        List<Option> options = new ArrayList<>(RESOURCE_SLOTS.length);
        options.add(resourceOption(RESOURCE_SLOTS[0], prefix, "<b><l>DIAMOND", Material.DIAMOND));
        options.add(resourceOption(RESOURCE_SLOTS[1], prefix, "<f><l>IRON", Material.IRON_INGOT));
        options.add(resourceOption(RESOURCE_SLOTS[2], prefix, "<6><l>GOLD", Material.GOLD_INGOT));
        options.add(resourceOption(RESOURCE_SLOTS[3], prefix, "<2><l>EMERALD", Material.EMERALD));
        return options;
    }

    private static Option resourceOption(int slot, String prefix, String resource, Material icon) {
        Text message = Text.of("<a>{} " + resource, prefix);
        return new Option(slot, message, icon, message);
    }

    private static List<Option> buildTeamOptions(ViewContext ctx, String prefix) {
        if (!(ctx.player() instanceof BedWarsPlayer player)) {
            return List.of();
        }

        BedWarsGame game = player.getGame();
        TeamKey ownTeam = GUIQuickCommunications.resolveTeamKey(player);
        if (game == null || ownTeam == null) {
            return List.of();
        }

        List<TeamKey> teams = game.getTeams().stream()
            .filter(BedWarsTeam::hasPlayers)
            .map(BedWarsTeam::getTeamKey)
            .filter(team -> team != ownTeam)
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toList();

        List<Option> options = new ArrayList<>();
        int optionCount = Math.min(TEAM_SLOTS.length, teams.size());
        for (int i = 0; i < optionCount; i++) {
            TeamKey team = teams.get(i);
            Text message = Text.of("<a>{} <l>{}", prefix,
                Text.of("<color:{}>{}", team.chatColor(), team.getName().toUpperCase(Locale.ROOT)));
            options.add(new Option(
                TEAM_SLOTS[i],
                message,
                team.bedMaterial(),
                message
            ));
        }

        return options;
    }

    private record Option(int slot, Text displayName, Material icon, Text message) {
    }
}
