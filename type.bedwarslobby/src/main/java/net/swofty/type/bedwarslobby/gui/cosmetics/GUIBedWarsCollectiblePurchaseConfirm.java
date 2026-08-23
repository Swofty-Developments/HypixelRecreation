package net.swofty.type.bedwarslobby.gui.cosmetics;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.CollectibleDescriptionService;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUIBedWarsCollectiblePurchaseConfirm implements View<GUIBedWarsCollectiblePurchaseConfirm.State> {

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Confirm Purchase", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        Optional<CollectibleDefinition> optionalDefinition = BedWarsCollectibleCatalog.findItemById(state.collectibleId());
        if (optionalDefinition.isEmpty()) {
            layout.slot(13, ItemStacks.item(Material.BARRIER, """
                    <c>Collectible Missing
                    <7>Unable to resolve this collectible.
                    <7>Please reopen the cosmetics menu."""));
            layout.slot(15, ItemStacks.item(Material.BARRIER, "<c>Close"), (click, context) -> context.backOrClose());
            return;
        }

        CollectibleDefinition definition = optionalDefinition.get();

        layout.slot(13, (s, c) -> buildDisplayStack(definition, state.cost()));

        layout.slot(11,
            (s, c) -> ItemStacks.item(Material.LIME_TERRACOTTA, """
                    <a>Confirm Purchase
                    <7>Unlocks the <f>{} <7> using <2>Tokens<7>!

                    <7>Cost: <2>{:,} Tokens

                    <e>Click to purchase with Tokens!""", definition.name(), state.cost()),
            (click, context) -> {
                if (!(click.click() instanceof Click.Left || click.click() instanceof Click.Right)) {
                    return;
                }

                BedWarsCollectibleStateService.SelectionResult result =
                    BedWarsCollectibleStateService.purchaseAndSelect(context.player(), definition);
                context.player().sendMessage(result.success()
                    ? Text.parse(result.message())
                    : bottomLineFailureMessage(result.message()));

                if (result.success()) {
                    context.pop();
                }
            }
        );

        layout.slot(15,
            ItemStacks.item(Material.RED_TERRACOTTA, """
                    <c>Cancel
                    <7>Return to the cosmetics menu."""),
            (click, context) -> context.pop()
        );
    }

    private static ItemStack.Builder buildDisplayStack(CollectibleDefinition definition, long cost) {
        List<Text> lore = new ArrayList<>(CollectibleDescriptionService.resolveLore(definition));
        if (!lore.isEmpty()) {
            lore.add(Text.empty());
        }
        lore.add(Text.of("<7>Cost: <2>{:,} Tokens", cost));

        Text title = Text.of("<a>{} ", definition.name());

        if (definition.iconTexture() != null && !definition.iconTexture().isBlank()) {
            return ItemStacks.head(definition.iconTexture(), 1, title, lore);
        }
        Material material = definition.iconMaterial() != null ? definition.iconMaterial() : Material.BARRIER;
        return ItemStacks.item(material, 1, title, lore);
    }

    private static Text bottomLineFailureMessage(String message) {
        if (message == null || message.isBlank()) {
            return Text.of("<c>Action failed.");
        }

        String normalized = message.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.indexOf('§') >= 0) {
                return Text.legacy(line);
            }
            if (line.indexOf('<') >= 0) {
                return Text.parse(line);
            }
            return Text.of("<c>{}", line);
        }

        return Text.of("<c>Action failed.");
    }

    public record State(String collectibleId, long cost) {
    }
}
