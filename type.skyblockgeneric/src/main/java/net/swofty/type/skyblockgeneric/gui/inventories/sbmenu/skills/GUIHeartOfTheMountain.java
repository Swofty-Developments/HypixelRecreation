package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.skills;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUITreeSlots;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.skilltree.HotmService;
import net.swofty.type.skyblockgeneric.skilltree.SkillTreeDefinition;
import net.swofty.type.skyblockgeneric.skilltree.SkillTreeType;
import net.swofty.type.skyblockgeneric.skilltree.TreeNodeDefinition;
import net.swofty.type.skyblockgeneric.skilltree.TreePowder;
import net.swofty.type.skyblockgeneric.skilltree.TreeTierDefinition;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIHeartOfTheMountain implements StatefulView<GUIHeartOfTheMountain.State> {
    private static final String RESET_TEXTURE = "7c8489c03357d6d6abd9f4a3bd8824eb0f2841685ade95ff987ebe15b2e65fad";
    private static final String RNG_METER_DROP = "Divan's Alloy";

    private final boolean commandEntry;

    public GUIHeartOfTheMountain() {
        this(false);
    }

    public GUIHeartOfTheMountain(boolean commandEntry) {
        this.commandEntry = commandEntry;
    }

    @Override
    public State initialState() {
        return new State(-1);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Heart of the Mountain", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 45);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkillTreeDefinition definition = HotmService.definition();
        int topY = effectiveScroll(state, player, definition);

        for (int row = 0; row < 5; row++) {
            int y = topY + row;
            if (y > definition.maxY()) break;
            int tier = definition.tierForY(y);
            layout.slot(row * 9, tierItem(player, definition.tier(tier), tier));
            for (int x = 0; x < 7; x++) {
                TreeNodeDefinition node = definition.nodeAt(x, y);
                if (node == null) continue;
                int slot = row * 9 + x + 1;
                layout.slot(slot,
                        (s, c) -> nodeItem((SkyBlockPlayer) c.player(), node),
                        (click, c) -> handleNodeClick(node, click.click(), c));
            }
        }

        layout.slot(8, (s, c) -> scrollItem(s, (SkyBlockPlayer) c.player(), definition, true),
                (click, c) -> scroll(c, definition, true, click.click()));
        layout.slot(53, (s, c) -> scrollItem(s, (SkyBlockPlayer) c.player(), definition, false),
                (click, c) -> scroll(c, definition, false, click.click()));

        layout.slot(47, (s, c) -> treeSlotItem((SkyBlockPlayer) c.player()),
                (_, c) -> c.push(new GUITreeSlots(SkillTreeType.HOTM)));
        layoutBack(layout, ctx);
        layout.slot(49, (s, c) -> informationItem((SkyBlockPlayer) c.player()));
        layout.slot(50, crystalsItem());
        layout.slot(51, rngMeterItem(), (_, c) -> c.push(new GUIHotmRngMeter()));
        layout.slot(52, resetItem(), (_, c) -> reset((SkyBlockPlayer) c.player(), c));
    }

    private void layoutBack(ViewLayout<State> layout, ViewContext ctx) {
        if (commandEntry) {
            layout.slot(48, ItemStacks.item(Material.ARROW, "<a>Go Back\n<7>To Mining Skill"),
                    (_, c) -> c.replace(new GUISkillCategory(net.swofty.type.skyblockgeneric.skill.SkillCategories.MINING, 0)));
            return;
        }
        if (!Components.back(layout, 48, ctx)) Components.close(layout, 48);
    }

    private void handleNodeClick(TreeNodeDefinition node, Click click, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (click instanceof Click.Right || click instanceof Click.RightShift) {
            if (HotmService.toggleNode(player, node)) ctx.session(State.class).refresh();
            return;
        }
        if (!(click instanceof Click.Left) && !(click instanceof Click.LeftShift)) return;

        int current = HotmService.level(player, node);
        if (node.ability() && current >= node.maxLevel()) {
            if (HotmService.toggleAbility(player, node)) ctx.session(State.class).refresh();
            return;
        }
        int amount = click instanceof Click.LeftShift ? 10 : 1;
        if (HotmService.upgrade(player, node, amount) > 0) ctx.session(State.class).refresh();
    }

    private void scroll(ViewContext ctx, SkillTreeDefinition definition, boolean up, Click click) {
        State state = ctx.session(State.class).state();
        int current = effectiveScroll(state, (SkyBlockPlayer) ctx.player(), definition);
        boolean right = click instanceof Click.Right || click instanceof Click.RightShift;
        int next;
        if (right) {
            next = up ? 0 : definition.maxY();
        } else if (click instanceof Click.Left || click instanceof Click.LeftShift) {
            next = current + (up ? -1 : 1);
        } else {
            return;
        }
        ctx.session(State.class).update(_ -> new State(definition.clampScroll(next)));
    }

    private static int effectiveScroll(State state, SkyBlockPlayer player, SkillTreeDefinition definition) {
        if (state.topY() >= 0) return definition.clampScroll(state.topY());
        return definition.clampScroll(10 - HotmService.data(player).getTier());
    }

    private net.minestom.server.item.ItemStack.Builder tierItem(SkyBlockPlayer player, TreeTierDefinition tier, int tierNumber) {
        boolean unlocked = HotmService.data(player).getTier() >= tierNumber;
        List<String> lore = new ArrayList<>();
        if (unlocked) {
            lore.add("<7>You have unlocked this tier. All");
            lore.add("<7>perks and abilities on this tier are");
            lore.add("<7>available for unlocking with <5>Token of");
            lore.add("<5>the Mountain<7>.");
            lore.add("");
            lore.add("<7>Rewards");
            for (String reward : tier.rewards()) lore.add("<8>+<f>" + reward);
            lore.add("");
            lore.add("<a><l>UNLOCKED");
        } else {
            lore.add("<7>Reach <5>Heart of the Mountain " + tierNumber + "<7>");
            lore.add("<7>to unlock this tier.");
            lore.add("");
            lore.add("<c><l>LOCKED");
        }
        return ItemStacks.item(unlocked ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, 1,
                Text.of((unlocked ? "<a>" : "<c>") + "Tier {}", tierNumber), text(lore));
    }

    private net.minestom.server.item.ItemStack.Builder nodeItem(SkyBlockPlayer player, TreeNodeDefinition node) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        int current = HotmService.level(player, node);
        int displayLevel = Math.max(1, current);
        boolean available = HotmService.available(player, node);
        boolean selected = HotmService.isSelected(player, node);
        boolean enabled = HotmService.isEnabled(player, node);
        String titleColor = current >= node.maxLevel() || selected ? "<a>" : current > 0 || available ? "<e>" : "<c>";

        List<String> lore = new ArrayList<>();
        if (!node.ability()) lore.add("<7>Level " + displayLevel + "/" + node.maxLevel());
        lore.addAll(node.renderLore(displayLevel, data.getTier(), HotmService.level(player, HotmService.node("core_of_the_mountain"))));

        if (current > 0 && current < node.maxLevel()) {
            lore.add("");
            lore.add("<a><l>=====[ UPGRADE ]=====");
            if (!node.ability()) lore.add("<7>Level " + (current + 1) + "/" + node.maxLevel());
            lore.add("");
            lore.addAll(node.renderLore(current + 1, data.getTier(), HotmService.level(player, HotmService.node("core_of_the_mountain"))));
        }

        if (current < node.maxLevel()) {
            lore.add("");
            lore.add("<7>Cost");
            if (current == 0) {
                lore.add("<5><l>1 <5>Token of the Mountain");
            } else {
                TreePowder powder = node.powder(current);
                lore.add(powder.color() + StringUtility.commaify(node.cost(current)) + " " + powder.displayName());
            }
            lore.add("");
            if (!available) {
                for (String requirement : HotmService.missingRequirements(player, node)) {
                    lore.add("<c>Requires " + requirement);
                }
            } else if (!HotmService.canAffordNextLevel(player, node)) {
                if (current == 0) {
                    lore.add("<c>You don't have enough Token of the Mountain!");
                } else {
                    lore.add("<c>You don't have enough " + node.powder(current).displayName() + "!");
                }
            } else if (current == 0) {
                lore.add("<e>Click to unlock!");
            } else {
                lore.add(enabled ? "<a><l>ENABLED" : "<c><l>DISABLED");
                lore.add("");
                lore.add(enabled ? "<e>Right-click to <c>disable<e>!" : "<e>Right-click to <a>enable<e>!");
                lore.add("<e>Left-click to upgrade!");
                lore.add("<e>Shift Left-click to upgrade 10 levels!");
            }
        } else if (node.ability()) {
            lore.add("");
            lore.add(selected ? "<a><l>SELECTED" : "<e>Click to select!");
            if (selected) {
                lore.add("");
                lore.add("<e>Right-click to <c>disable<e>!");
            }
        } else {
            lore.add("");
            lore.add(enabled ? "<a><l>ENABLED" : "<c><l>DISABLED");
            lore.add("");
            lore.add(enabled ? "<e>Right-click to <c>disable<e>!" : "<e>Right-click to <a>enable<e>!");
        }

        return ItemStacks.item(node.material(current), 1, Text.of(titleColor + "{}", node.name()), text(lore));
    }

    private net.minestom.server.item.ItemStack.Builder scrollItem(State state, SkyBlockPlayer player, SkillTreeDefinition definition, boolean up) {
        int current = effectiveScroll(state, player, definition);
        if ((up && current == 0) || (!up && current == definition.maxY())) {
            return Components.asFiller(Material.BLACK_STAINED_GLASS_PANE);
        }
        String action = up ? "up" : "down";
        String destination = up ? "top tier" : "bottom tier";
        return ItemStacks.item(Material.ARROW, 1, """
                <a>Scroll {}
                <e>Left-click <7>to scroll {}!

                <e>Right-click <7>to go to the {}!""", up ? "Up" : "Down", action, destination);
    }

    private net.minestom.server.item.ItemStack.Builder treeSlotItem(SkyBlockPlayer player) {
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);
        int active = data.getActiveHotmSlot();
        return ItemStacks.item(Material.CHEST, 1, """
                <a>Heart of the Mountain Slot
                <7>Quickly swap between saved trees.

                <7>Current: <a>{}

                <c>Swapping trees has a 10m cooldown!

                <e>Click to view!""", data.getHotmNames()[active]);
    }

    private net.minestom.server.item.ItemStack.Builder informationItem(SkyBlockPlayer player) {
        DatapointHOTM.PlayerHOTMData data = HotmService.data(player);
        List<String> lore = new ArrayList<>();
        lore.add("<7>Token of the Mountain: <5>" + data.getAvailableTokens());
        lore.add("");
        lore.add("<8>Use <5>Token of the Mountain <8>to unlock");
        lore.add("<8>perks and abilities above!");
        lore.add("");
        lore.add("<7>Mithril Powder: <2>" + StringUtility.commaify(data.getMithrilPowder()));
        lore.add("  <8>(+<2>more powder<8>)");
        lore.add("<7>Gemstone Powder: <d>" + StringUtility.commaify(data.getGemstonePowder()));
        lore.add("<7>Glacite Powder: <b>" + StringUtility.commaify(data.getGlacitePowder()));
        lore.add("");
        lore.add("<7>Obtain <2>Mithril Powder <7>by mining and");
        lore.add("<7>taking part in events in the <2>Dwarven Mines<7>.");
        lore.add("<7>Obtain <d>Gemstone Powder <7>by mining");
        lore.add("<7>Gemstones and opening Treasure Chests in the <5>Crystal Hollows<7>.");
        lore.add("<7>Obtain <b>Glacite Powder <7>by mining Glacite");
        lore.add("<7>and looting Frozen Corpses in the <b>Glacite Tunnels<7>.");
        lore.add("");
        lore.add("<8>Increase your chance to gain extra");
        lore.add("<8>Powder by unlocking perks, equipping");
        lore.add("<8>the <2>Mithril Golem Pet<8>, and more!");
        return ItemStacks.head(HotmService.definition().headTexture(), 1,
                Text.of("<5>Heart of the Mountain"), text(lore));
    }

    private net.minestom.server.item.ItemStack.Builder crystalsItem() {
        return ItemStacks.item(Material.PAPER, 1, """
                <5>Crystal Hollows Crystals
                <8>Crystals are used to forge Gems
                <8>into <d>Perfect <8>Gems. They can be
                <8>found hidden within the <5>Crystal
                <8>Hollows<8>.

                <d>Your Crystal Nucleus
                  <a>Jade <c>✖ Not Found
                  <6>Amber <c>✖ Not Found
                  <5>Amethyst <c>✖ Not Found
                  <b>Sapphire <c>✖ Not Found
                  <e>Topaz <c>✖ Not Found

                <d>Your Other Crystals
                  <c>Ruby <a>✔ Found
                  <f>Opal <c>✖ Not Found
                  <9>Aquamarine <c>✖ Not Found
                  <2>Peridot <c>✖ Not Found
                  <8>Onyx <c>✖ Not Found
                  <4>Citrine <c>✖ Not Found""");
    }

    private net.minestom.server.item.ItemStack.Builder rngMeterItem() {
        return ItemStacks.item(Material.PAPER, 1, """
                <d>Crystal Nucleus RNG Meter
                <7>Your <d>Crystal Nucleus RNG Meter <7>fills
                <7>with <9>1,000 Nucleus XP <7>every time you
                <7>complete the <d>Crystal Nucleus<7>!

                <7>Selected Drop
                <6>{}

                <7>Progress: <d>1.1%
                <d><m>                         <f>  <d>11,000<5>/<d>1M

                <e>Click to view!""", RNG_METER_DROP);
    }

    private net.minestom.server.item.ItemStack.Builder resetItem() {
        return ItemStacks.head(RESET_TEXTURE, 1, """
                <c>Reset Heart of the Mountain
                <7>Resets the Perks and Abilities of
                <7>your <5>Heart of the Mountain<7>, locking
                <7>them and resetting their levels.

                <c>WARNING: This is permanent.
                <c>You can not go back after resetting!""");
    }

    private void reset(SkyBlockPlayer player, ViewContext ctx) {
        if (HotmService.resetActiveTree(player) > 0) {
            player.sendMessage(Text.of("<a>Your Heart of the Mountain perks have been reset."));
            ctx.session(State.class).refresh();
        }
    }

    public record State(int topY) {
    }

    private static List<Text> text(List<String> markup) {
        return markup.stream().map(Text::of).toList();
    }
}
