package net.swofty.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeText;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class GUIAttributeDetails extends StatelessView {
    private final AttributeDefinition definition;

    public GUIAttributeDetails(AttributeDefinition definition) {
        this.definition = definition;
    }

    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.literal(definition.name()), InventoryType.CHEST_5_ROW);
    }

    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        DatapointHunting.HuntingData data = ((SkyBlockPlayer) ctx.player()).getHuntingData();
        int level = data.level(definition.id());
        int syphoned = data.syphoned(definition.id());
        boolean enabled = data.enabled(definition.id());
        layout.slot(4, AttributeGUIItems.attribute(definition, data, true));

        List<Text> maxLore = new ArrayList<>(AttributeGUIItems.wrapped(AttributeText.atLevel(definition, 10)));
        maxLore.add(Text.empty());
        int remaining = Math.max(0, definition.rarity().cumulativeForLevel(10) - syphoned);
        if (remaining > 0) {
            maxLore.add(Text.of("<7>Requires <b>{} <7>more Shards to max out", remaining));
            maxLore.add(Text.of("<7>(<a>{}<7>/<c>{}<7>).", syphoned, definition.rarity().cumulativeForLevel(10)));
        } else {
            maxLore.add(Text.of("<7>Max level reached!"));
        }
        layout.slot(20, ItemStacks.item(Material.EXPERIENCE_BOTTLE, 1,
                Text.of(level >= 10 ? "<a>Max Level - <6>{}" : "<c>Max Level - <6>{}", definition.name()), maxLore));

        List<Text> huntLore = new ArrayList<>();
        huntLore.add(Text.empty());
        huntLore.addAll(AttributeText.huntInfo(definition));
        layout.slot(22, ItemStacks.item(Material.LEAD, 1, Text.of("<a>How to Hunt"), huntLore));
        layout.slot(24, ItemStacks.item(Material.OAK_BUTTON, 1,
                        enabled ? Text.of("<c>Toggle") : Text.of("<a>Toggle"),
                        List.of(Text.of("<7>Currently: {}", enabled ? Text.of("<a>ON") : Text.of("<c>OFF")),
                                Text.empty(),
                                level == 0 ? Text.of("<c>Not Unlocked!") : Text.of("<e>Click to toggle!"))),
                (_, c) -> {
                    data.toggle(definition.id());
                    c.session(DefaultState.class).setState(state);
                });
        Components.back(layout, 39, ctx, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Attribute Menu"""));
        Components.close(layout, 40);
    }
}
