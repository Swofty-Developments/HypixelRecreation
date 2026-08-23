package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIBestiaryIsland extends StatelessView {

    private static final int[] DISPLAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };

    private final BestiaryCategories category;

    public GUIBestiaryIsland(BestiaryCategories category) {
        this.category = category;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Bestiary ➡ {}", Text.of(category.getDisplayName()).plain()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        BestiaryData bestiaryData = new BestiaryData();

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();

            BestiaryEntry[] entries = category.getEntries();
            int total = entries.length;
            int found = 0;
            int completed = 0;

            for (BestiaryEntry entry : entries) {
                int kills = player.getBestiaryData().getAmount(entry.getMobs());
                if (kills > 0) {
                    found++;

                    BestiaryMob mob = entry.getMobs().getFirst();
                    int mobKills = player.getBestiaryData().getAmount(mob);
                    int tier = bestiaryData.getCurrentBestiaryTier(mob, mobKills);
                    if (tier == mob.getMaxBestiaryTier()) completed++;
                }
            }

            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>View all of the mobs that you've"));
            lore.add(Text.of("<7>found and killed on {}<7>.", Text.of(category.getDisplayName())));
            lore.add(Text.empty());

            // Families Found
            int foundPercent = (int) ((double) found / total * 100);
            lore.add(Text.of(foundPercent == 100
                    ? "<7>Families Found: <a>{}%"
                    : "<7>Families Found: <e>{}%", foundPercent));

            String baseBar = "─────────────────";
            int barLength = baseBar.length();
            int filled = (int) Math.round(((double) found / total) * barLength);

            lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                    baseBar.substring(0, Math.min(filled, barLength)),
                    baseBar.substring(Math.min(filled, barLength)),
                    found, total));
            lore.add(Text.empty());

            // Families Completed
            int completedPercent = (int) ((double) completed / total * 100);
            lore.add(Text.of(completedPercent == 100
                    ? "<7>Families Completed: <a>{}%"
                    : "<7>Families Completed: <e>{}%", completedPercent));

            int completedFilled = (int) Math.round(((double) completed / total) * barLength);

            lore.add(Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                    baseBar.substring(0, Math.min(completedFilled, barLength)),
                    baseBar.substring(Math.min(completedFilled, barLength)),
                    completed, total));

            return ItemStacks.head(
                    "c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56",
                    Text.of(category.getDisplayName()),
                    lore
            );
        });

        // Display all mobs
        BestiaryEntry[] bestiaryEntries = category.getEntries();
        for (int i = 0; i < DISPLAY_SLOTS.length && i < bestiaryEntries.length; i++) {
            BestiaryEntry bestiaryEntry = bestiaryEntries[i];
            BestiaryMob mob = bestiaryEntry.getMobs().getFirst();
            int slot = DISPLAY_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int kills = player.getBestiaryData().getAmount(bestiaryEntry.getMobs());

                if (kills > 0) {
                    int tier = bestiaryData.getCurrentBestiaryTier(mob, kills);
                    ArrayList<String> rendered = new ArrayList<>();
                    GUIMaterial guiMaterial = bestiaryEntry.getGuiMaterial();

                    player.getBestiaryData().getMobDisplay(rendered, kills, mob, bestiaryEntry);

                    List<Text> lore = new ArrayList<>(rendered.stream().map(Text::parse).toList());
                    lore.add(Text.of("<e>Click to view!"));

                    return ItemStacks.of(guiMaterial, 1,
                            Text.of("<a>{} {:roman}", Text.parse(bestiaryEntry.getName()), tier), lore);
                } else {
                    return ItemStacks.item(Material.GRAY_DYE, 1, """
                            <c>{}
                            <7>Kill a mob belonging to this Family to
                            <7>unlock it in your Bestiary!""", Text.parse(bestiaryEntry.getName()));
                }
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                int kills = player.getBestiaryData().getAmount(bestiaryEntry.getMobs());
                if (kills > 0) {
                    player.openView(new GUIBestiaryMob(category, bestiaryEntry));
                }
            });
        }
    }
}
