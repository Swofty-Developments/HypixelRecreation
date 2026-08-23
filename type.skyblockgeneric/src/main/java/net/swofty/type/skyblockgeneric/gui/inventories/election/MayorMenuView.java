package net.swofty.type.skyblockgeneric.gui.inventories.election;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.elections.ElectionData;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;

import java.util.ArrayList;
import java.util.List;

public class MayorMenuView extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
        if (mayor == null) {
            return ViewConfiguration.translatable("gui_election.mayor.title_fallback", InventoryType.CHEST_4_ROW);
        }
        return ViewConfiguration.withText(
                (s, ctx) -> Text.key("gui_election.mayor.title", mayor.getDisplayName()),
                InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);

        SkyBlockMayor mayor = ElectionManager.getCurrentMayor();
        if (mayor == null) return;

        ElectionData data = ElectionManager.getElectionData();
        String mayorColor = data.getCurrentMayorColor();
        List<SkyBlockMayor.Perk> activePerks = data.getCurrentMayorPerkEnums();

        layout.slot(11, (s, c) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.key("gui_election.mayor.perks_label"));
            lore.add(Text.empty());
            lore.add(Text.of("<8><m>--------------------------"));

            for (SkyBlockMayor.Perk perk : activePerks) {
                lore.add(ElectionData.colored(mayorColor, perk.getDisplayName()));
                lore.addAll(Text.of("<wrap:50>{}</wrap>", perk.getDescription()).lines());
                lore.add(Text.empty());
            }

            lore.add(Text.of("<8><m>--------------------------"));
            lore.add(Text.empty());
            lore.add(Text.key("gui_election.mayor.perks_footer_1"));
            lore.add(Text.key("gui_election.mayor.perks_footer_2"));
            lore.add(Text.key("gui_election.mayor.perks_footer_3"));

            return ItemStacks.head(
                    new PlayerSkin(mayor.getTexture(), mayor.getSignature()),
                    ElectionData.colored(mayorColor, "Mayor " + mayor.getDisplayName()),
                    lore
            );
        });

        layout.slot(15, (s, c) -> {
            ElectionData.ElectionResult lastResult = data.getLastElectionResult();
            if (lastResult == null) {
                return ItemStacks.item(Material.JUKEBOX, 1, Text.key("gui_election.mayor.results_title"), List.of(
                        Text.key("gui_election.mayor.results_no_data_1"),
                        Text.key("gui_election.mayor.results_no_data_2")
                ));
            }

            List<Text> resultLore = new ArrayList<>();
            resultLore.add(Text.key("gui_election.mayor.results_year", lastResult.getYear()));
            resultLore.add(Text.empty());

            List<ElectionData.CandidateResult> results = lastResult.getCandidateResults();
            for (int i = 0; i < results.size(); i++) {
                ElectionData.CandidateResult cr = results.get(i);
                TextColor clr = ElectionData.colorForIndex(i);
                SkyBlockMayor m = null;
                try { m = SkyBlockMayor.valueOf(cr.getMayorName()); } catch (IllegalArgumentException ignored) {}
                String name = m != null ? m.getDisplayName() : cr.getMayorName();
                resultLore.add(Text.of("<color:{0}>{1}", clr, String.format("%.1f%%", cr.getPercentage()))
                        .append("<8> ○ ")
                        .append("<color:{0}>{1} votes", clr, String.format("%,d", cr.getVotes()))
                        .append("<8> | ")
                        .append("<color:{0}>{1}", clr, name));
            }

            resultLore.add(Text.empty());
            resultLore.add(Text.key("gui_election.mayor.results_footer_1"));
            resultLore.add(Text.key("gui_election.mayor.results_footer_2", mayor.getDisplayName()));
            resultLore.add(Text.key("gui_election.mayor.results_footer_3"));

            return ItemStacks.item(Material.JUKEBOX, 1, Text.key("gui_election.mayor.results_title"), resultLore);
        });
    }
}
