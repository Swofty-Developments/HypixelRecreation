package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.List;

public class GUIGuildAchievements implements View<GUIGuildAchievements.GuildAchievementsState> {

    private static final int[][] PRESTIGE_TIERS = {{20}, {40}, {60}, {80}, {100}};
    private static final int[] PRESTIGE_SLOTS = {0, 1, 2, 3, 4};

    private static final int[][] EXP_KING_TIERS = {{50000}, {100000}, {150000}, {200000}, {250000}, {275000}, {300000}};
    private static final int[] EXP_KING_SLOTS = {9, 10, 11, 12, 13, 14, 15};
    private static final int[] WINNER_TIERS = {100, 200, 300, 400, 500, 750, 1000};
    private static final int[] WINNER_SLOTS = {18, 19, 20, 21, 22, 23, 24};

    private static final int[][] FAMILY_TIERS = {{5}, {15}, {30}, {40}, {50}, {60}, {70}};
    private static final int[] FAMILY_SLOTS = {27, 28, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<GuildAchievementsState> configuration() {
        return new ViewConfiguration<>("Guild Achievements", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<GuildAchievementsState> layout, GuildAchievementsState state, ViewContext ctx) {
        GuildData guild = state.guild();
        int level = guild.getLevel();

        for (int i = 0; i < PRESTIGE_TIERS.length; i++) {
            int required = PRESTIGE_TIERS[i][0];
            boolean achieved = level >= required;
            Text name = Text.of((achieved ? "<a>" : "<7>") + "Prestige {:roman}", i + 1);
            Material mat = achieved ? Material.EXPERIENCE_BOTTLE : Material.GRAY_DYE;
            Text progress = Text.of("<7>Progress: <e>{}<7>/{}", level, required)
                .appendIf(achieved, " <a>ACHIEVED");
            layout.slot(PRESTIGE_SLOTS[i], ItemStacks.item(mat, 1, name, List.of(
                Text.of("<7>Reach Guild level {}", required), Text.empty(), progress)));
        }

        long gexp = guild.getDailyGexp();
        for (int i = 0; i < EXP_KING_TIERS.length; i++) {
            int required = EXP_KING_TIERS[i][0];
            boolean achieved = gexp >= required;
            Text name = Text.of((achieved ? "<a>" : "<7>") + "Experience Kings {:roman}", i + 1);
            Material mat = achieved ? Material.CLOCK : Material.GRAY_DYE;
            Text progress = Text.of("<7>Progress: <e>{:,}<7>/{:,}", gexp, required)
                .appendIf(achieved, " <a>ACHIEVED");
            layout.slot(EXP_KING_SLOTS[i], ItemStacks.item(mat, 1, name, List.of(
                Text.of("<7>Get {:,} Guild Exp in one day", required), Text.empty(), progress)));
        }

        for (int i = 0; i < WINNER_TIERS.length; i++) {
            int required = WINNER_TIERS[i];
            boolean achieved = guild.getDailyWins() >= required;
            layout.slot(WINNER_SLOTS[i], ItemStacks.item(
                achieved ? Material.GOLD_INGOT : Material.GRAY_DYE, 1,
                Text.of((achieved ? "<a>" : "<7>") + "Winners {:roman}", i + 1),
                List.of(
                    Text.of("<7>Win {:,} games as a Guild in a", required),
                    Text.of("<7>day"),
                    Text.empty(),
                    Text.of("<7>Progress: <e>{:,}<7>/{:,}", guild.getDailyWins(), required)
                        .appendIf(achieved, " <a>ACHIEVED"))));
        }

        int memberCount = guild.getMembers().size();
        for (int i = 0; i < FAMILY_TIERS.length; i++) {
            int required = FAMILY_TIERS[i][0];
            boolean achieved = memberCount >= required;
            Text name = Text.of((achieved ? "<a>" : "<7>") + "Family {:roman}", i + 1);
            Material mat = achieved ? Material.DIAMOND : Material.GRAY_DYE;
            Text progress = Text.of("<7>Progress: <e>{}<7>/{}", memberCount, required)
                .appendIf(achieved, " <a>ACHIEVED");
            layout.slot(FAMILY_SLOTS[i], ItemStacks.item(mat, 1, name, List.of(
                Text.of("<7>Have {} guild members online", required),
                Text.of("<7>at the same time!"),
                Text.empty(),
                progress)));
        }

        layout.slot(49, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Guild"""), (click, viewCtx) -> viewCtx.navigator().pop());
    }

    public record GuildAchievementsState(GuildData guild) {
    }
}
