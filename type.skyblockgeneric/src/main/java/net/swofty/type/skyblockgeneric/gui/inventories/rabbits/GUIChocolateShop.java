package net.swofty.type.skyblockgeneric.gui.inventories.rabbits;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIChocolateShop implements StatefulView<GUIChocolateShop.State> {
    // Texture IDs (extracted from skull texture URLs)
    private static final String SUPREME_CHOCOLATE_BAR_TEXTURE = "254b7f3f2a6f0d1c2c054678128ec2322619aefff0f450c390d6a41b5950302e";
    private static final String EGGLOCATOR_TEXTURE = "14a7ff9a10fdae14446499ef3bc1df13b7888d6cd2e311ccab51b8352c6093b4";
    private static final String NIBBLE_CHOCOLATE_STICK_TEXTURE = "888188d62908af6e114f73a109e15ac7f1faded39abd6a2054034ec5cc70c727";
    private static final String SMOOTH_CHOCOLATE_BAR_TEXTURE = "a9372efd2ca1a6c6dfc066f1ec83f9456575c3850a0e7d01109c4f1af300ba8";
    private static final String RICH_CHOCOLATE_CHUNK_TEXTURE = "6f942717364c0fecf7ad11bac8cd98dd7ad4dbd72e3d3ce2b57eb48713824ff";
    private static final String GANACHE_CHOCOLATE_SLAB_TEXTURE = "f89512331edfdc27cb7d4e80f3e0db460d05caf66c7c1c42e0e712130a9b690";
    private static final String PRESTIGE_CHOCOLATE_REALM_TEXTURE = "af19ceeabf2ecb020610b8aabc9299264fa670048c010c9699ce687fc9bf351e";
    private static final String DARK_CACAO_TRUFFLE_TEXTURE = "db9db373cadbec1912a9ab386d31ceb3e0cd4d6a64f222426588a3b2eb31ed29";
    private static final String CHOCOLATE_DYE_TEXTURE = "a15e7208539306f65d68df9be6c3124c48027e307739fc8dc35526febd643c21";
    private static final String BARN_SKIN_TEXTURE = "af90da40c557af4ac01d39b6733e204c74ae9fee8c2bc40be1fd4f28f837d52";
    private static final String CHOCOLATE_SYRINGE_TEXTURE = "7dcb67a72c01f3ca75da846f957ffed6417f0c45ad814fb3e340c317cf316718";
    private static final String CHOCO_RABBIT_MINION_TEXTURE = "9a815398e7da89b1bc08f646cafc8e7b813da0be0eec0cce6d3eff5207801026";
    private static final String ZORROS_CAPE_TEXTURE = "81f7226a927558d069a6ae343b4e089fbd60fc6037190097c7713208e988faae";
    private static final String FISH_CHOCOLAT_TEXTURE = "422b0e5faa97ca109cd45f1fba2d84ca2b9b601de50b47f4add2d835aa360f78";
    private static final String HOT_CHOCOLATE_MIXIN_TEXTURE = "4fde9c68bc5a89f01a5e5203eecc5367d494d55a47c81e6b1d689a0c4488b6e";

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        // Slot 10: Supreme Chocolate Bar
        layout.slot(10, (s, c) -> ItemStacks.head(SUPREME_CHOCOLATE_BAR_TEXTURE, """
                <a>Supreme Chocolate Bar
                <7>Bring <6>3,000 <7>of these to <5>Carrolyn <7>in
                <5>Scarleton <7>on the <c>Crimson Isle <7>to
                <7>permanently gain <c>+5❤ Health <7>and
                <6>+12☘ Cocoa Beans Fortune<7>.

                <a><l>UNCOMMON
</l>
                <7>Cost
                <6>{} Chocolate

                <7>Annual Stock <8>Year 471
                <6>500 <7>remaining

                <e>Click to trade!
                <e>Right-click for more trading options!""",
                ChocolateFactoryHelper.formatChocolate(2_000_000L)),
                (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 2_000_000L, "<a>Supreme Chocolate Bar", 0, c));

        // Slot 11: Egglocator
        layout.slot(11, (s, c) -> ItemStacks.head(EGGLOCATOR_TEXTURE, """
                <f>Egglocator
                <7>Uses the magic of <a>Hoppity <7>to
                <7>uncover hidden <a>Chocolate Rabbit
                <a>Eggs<7>.

                <6>Ability: Egglocator  <e><l>RIGHT CLICK</l>
                <7>Points towards the nearest unclaimed
                <a>Chocolate Rabbit Egg<7>!
                <8>Cooldown: <a>5s

                <7>Only works during <d>Hoppity's Hunt<7>.

                <f><l>COMMON
</l>
                <7>Cost
                <6>{} Chocolate

                <7>Annual Stock <8>Year 471
                <6>1 <7>remaining

                <e>Click to trade!""",
                ChocolateFactoryHelper.formatChocolate(7_500_000L)),
                (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 7_500_000L, "<f>Egglocator", 0, c));

        // Slot 12: Nibble Chocolate Stick
        layout.slot(12, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 1) {
                return createLockedItem("<c>Chocolate Factory II.");
            }
            return ItemStacks.head(NIBBLE_CHOCOLATE_STICK_TEXTURE, """
                    <f>Nibble Chocolate Stick
                    <7>Grants <a>+2% <7>chance to find a
                    <a>Chocolate Rabbit <7>that you haven't
                    <7>found yet and grants <6>+10 Chocolate
                    <7>per second.

                    <8><o>A delightful treat from the Factory.</o>
                    <8><o>Its crisp taste sparks joy with</o>
                    <8><o>every bite.
</o>
                    <f><l>COMMON ACCESSORY
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(250_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 250_000_000L, "<f>Nibble Chocolate Stick", 1, c));

        // Slot 13: Smooth Chocolate Bar
        layout.slot(13, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 1) {
                return createLockedItem("<c>Chocolate Factory II.");
            }
            return ItemStacks.head(SMOOTH_CHOCOLATE_BAR_TEXTURE, """
                    <a>Smooth Chocolate Bar
                    <7>Grants <a>+4% <7>chance to find a
                    <a>Chocolate Rabbit <7>that you haven't
                    <7>found yet and grants <6>+20 Chocolate
                    <7>per second.

                    <8><o>Crafted in the Factory, its</o>
                    <8><o>smoothness melts hearts and tastes</o>
                    <8><o>like a sweet escape.
</o>
                    <a><l>UNCOMMON ACCESSORY
</l>
                    <7>Cost
                    <6>{} Chocolate
                    <f>Nibble Chocolate Stick

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(1_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 1_000_000_000L, "<a>Smooth Chocolate Bar", 1, c));

        // Slot 14: Rich Chocolate Chunk
        layout.slot(14, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 2) {
                return createLockedItem("<c>Chocolate Factory III.");
            }
            return ItemStacks.head(RICH_CHOCOLATE_CHUNK_TEXTURE, """
                    <9>Rich Chocolate Chunk
                    <7>Grants <a>+6% <7>chance to find a
                    <a>Chocolate Rabbit <7>that you haven't
                    <7>found yet and grants <6>+30 Chocolate
                    <7>per second.

                    <8><o>From the Factory's secret</o>
                    <8><o>reserves, its rich flavor is a deep</o>
                    <8><o>dive into indulgence.
</o>
                    <9><l>RARE ACCESSORY
</l>
                    <7>Cost
                    <6>{} Chocolate
                    <a>Smooth Chocolate Bar

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(2_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 2_000_000_000L, "<9>Rich Chocolate Chunk", 2, c));

        // Slot 15: Ganache Chocolate Slab
        layout.slot(15, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 2) {
                return createLockedItem("<c>Chocolate Factory III.");
            }
            return ItemStacks.head(GANACHE_CHOCOLATE_SLAB_TEXTURE, """
                    <5>Ganache Chocolate Slab
                    <7>Grants <a>+8% <7>chance to find a
                    <a>Chocolate Rabbit <7>that you haven't
                    <7>found yet and grants <6>+40 Chocolate
                    <7>per second.

                    <8><o>A Factory masterpiece - its divine</o>
                    <8><o>taste transcends reality, offering a</o>
                    <8><o>heavenly escape.
</o>
                    <5><l>EPIC ACCESSORY
</l>
                    <7>Cost
                    <6>{} Chocolate
                    <9>Rich Chocolate Chunk

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(3_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 3_000_000_000L, "<5>Ganache Chocolate Slab", 2, c));

        // Slot 16: Prestige Chocolate Realm
        layout.slot(16, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 3) {
                return createLockedItem("<c>Chocolate Factory IV.");
            }
            return ItemStacks.head(PRESTIGE_CHOCOLATE_REALM_TEXTURE, """
                    <6>Prestige Chocolate Realm
                    <7>Grants <a>+10% <7>chance to find a
                    <a>Chocolate Rabbit <7>that you haven't
                    <7>found yet and grants <6>+50 Chocolate
                    <7>per second.

                    <8><o>The Factory's pinnacle creation - its</o>
                    <8><o>epic taste shatters expectations,</o>
                    <8><o>offering a taste of utopia.
</o>
                    <6><l>LEGENDARY ACCESSORY
</l>
                    <7>Cost
                    <6>{} Chocolate
                    <5>Ganache Chocolate Slab

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(4_500_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 4_500_000_000L, "<6>Prestige Chocolate Realm", 3, c));

        // Slot 19: Dark Cacao Truffle
        layout.slot(19, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 3) {
                return createLockedItem("<c>Chocolate Factory IV.");
            }
            return ItemStacks.head(DARK_CACAO_TRUFFLE_TEXTURE, """
                    <9>Dark Cacao Truffle
                    <7>Consume to boost your <6>☘ Global
                    <6>Fortune <7>for <a>60m<7>.

                    <7>Keep this item in your inventory to
                    <7>increase the bonus up to <6>+30<6>☘
                    <6>Global Fortune<7>, at which point the
                    <7>item <c><o>evolves<7>!
</o>
                    <7>Current Bonus: <6>+0<6>☘ Global Fortune

                    <9><l>RARE
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>2 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(2_500_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 2_500_000_000L, "<9>Dark Cacao Truffle", 3, c));

        // Slot 20: Chocolate Dye
        layout.slot(20, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 5) {
                return createLockedItem("<c>Chocolate Factory VI.");
            }
            return ItemStacks.head(CHOCOLATE_DYE_TEXTURE, """
                    <6>Chocolate Dye
                    <8>Combinable in Anvil

                    <7>Changes the color of an armor piece
                    <7>to <6>#7B3F00<7>!

                    <5><l>EPIC DYE
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(40_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 40_000_000_000L, "<6>Chocolate Dye", 5, c));

        // Slot 21: Chocolate Factory Barn Skin
        layout.slot(21, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 4) {
                return createLockedItem("<c>Chocolate Factory V.");
            }
            return ItemStacks.head(BARN_SKIN_TEXTURE, """
                    <6>Chocolate Factory Barn Skin
                    <7>Consume this item to unlock the
                    <6>Chocolate Factory Barn Skin <7>on <a>The
                    <a>Garden<7>!

                    <e>Click to consume!

                    <6><l>LEGENDARY COSMETIC
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(7_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 7_000_000_000L, "<6>Chocolate Factory Barn Skin", 4, c));

        // Slot 22: Chocolate Syringe
        layout.slot(22, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 4) {
                return createLockedItem("<c>Chocolate Factory V.");
            }
            return ItemStacks.head(CHOCOLATE_SYRINGE_TEXTURE, """
                    <d>Chocolate Syringe
                    <7>Use at <b>Kat <7>to upgrade <e>Rabbit Pets <7>to
                    <d>Mythic <7>rarity.

                    <d><l>MYTHIC
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(10_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 10_000_000_000L, "<d>Chocolate Syringe", 4, c));

        // Slot 23: Choco Rabbit Minion Skin
        layout.slot(23, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 4) {
                return createLockedItem("<c>Chocolate Factory V.");
            }
            return ItemStacks.head(CHOCO_RABBIT_MINION_TEXTURE, """
                    <5>Choco Rabbit Minion Skin
                    <7>This Minion skin changes your
                    <7>minion's appearance to a <e>Choco
                    <e>Rabbit<7>.

                    <7>You can place this item in any minion
                    <7>of your choice!

                    <5><l>EPIC COSMETIC
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <7>Annual Stock <8>Year 471
                    <6>2 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(2_500_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 2_500_000_000L, "<5>Choco Rabbit Minion Skin", 4, c));

        // Slot 24: Zorro's Cape
        layout.slot(24, (s, c) -> ItemStacks.head(ZORROS_CAPE_TEXTURE, """
                <6>Zorro's Cape
                <7>Strength: <c>+10
                <7>Ferocity: <c>+2
                <7>Farming Fortune: <6>+10
                <7>Farming Wisdom: <3>+1

                <7>The stats of this Cape <a>double\s
                <7>during <e>Jacob's Farming Contest<7>.
                <7>Additionally, you have a <a>20% <7>chance
                <7>to obtain an extra medal from
                <7>contests.

                <8><o>Not all Rabbits wear capes.
</o>
                <8>This item can be reforged!
                <4>❣ <c>Requires <d>Zorro <c>in Hoppity's Collection<c>.
                <6><l>LEGENDARY CLOAK
</l>
                <7>Cost
                <6>{} Chocolate

                <c>Not unlocked!""",
                ChocolateFactoryHelper.formatChocolate(20_000_000_000L)));

        // Slot 25: Fish Chocolat à la Vapeur
        layout.slot(25, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 5) {
                return createLockedItem("<c>Chocolate Factory VI.");
            }
            return ItemStacks.head(FISH_CHOCOLAT_TEXTURE, """
                    <5>Fish Chocolat à la Vapeur
                    <7>Give this dish to <a>Hoppity <7>to obtain his
                    <a>Abiphone Contact<7>.

                    <8><o>Savory fish with a chocolate twist.</o>
                    <8><o>Mwah! C'est magnifique, no?
</o>
                    <5><l>EPIC
</l>
                    <7>Cost
                    <6>{} Chocolate
                    <c>Rabbit the Fish

                    <7>Annual Stock <8>Year 471
                    <6>1 <7>remaining

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(50_000_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 50_000_000_000L, "<5>Fish Chocolat à la Vapeur", 5, c));

        // Slot 28: Hot Chocolate Mixin
        layout.slot(28, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            if (prestige < 5) {
                return ItemStacks.head(HOT_CHOCOLATE_MIXIN_TEXTURE, """
                        <9>Hot Chocolate Mixin
                        <8>Brewing Ingredient

                        <7>Mixins provide a buff that can be
                        <7>added to <c>God Potions <7>in a brewing
                        <7>stand and lasts for the full duration.

                        <7>Gain <d>+15♣ Pet Luck <7>and <6>+0.05x
                        <6>Chocolate <7>per second.

                        <7>Duration: <a>36h 0m

                        <7>The duration of Mixins can be stacked!

                        <e>Right-click to consume!
                        <8>(Requires active Booster Cookie)

                        <4>❣ <c>Requires <c>Chocolate Factory VI<c>.
                        <9><l>RARE
</l>
                        <7>Cost
                        <6>{} Chocolate

                        <c>Not unlocked!""",
                        ChocolateFactoryHelper.formatChocolate(1_500_000_000L));
            }
            return ItemStacks.head(HOT_CHOCOLATE_MIXIN_TEXTURE, """
                    <9>Hot Chocolate Mixin
                    <8>Brewing Ingredient

                    <7>Mixins provide a buff that can be
                    <7>added to <c>God Potions <7>in a brewing
                    <7>stand and lasts for the full duration.

                    <7>Gain <d>+15♣ Pet Luck <7>and <6>+0.05x
                    <6>Chocolate <7>per second.

                    <7>Duration: <a>36h 0m

                    <7>The duration of Mixins can be stacked!

                    <e>Right-click to consume!
                    <8>(Requires active Booster Cookie)

                    <9><l>RARE
</l>
                    <7>Cost
                    <6>{} Chocolate

                    <e>Click to trade!""",
                    ChocolateFactoryHelper.formatChocolate(1_500_000_000L));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 1_500_000_000L, "<9>Hot Chocolate Mixin", 5, c));

        // Slot 29: Chocolate Fortune
        layout.slot(29, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            int prestige = ChocolateFactoryHelper.getData(p).getPrestigeLevel();
            String footer = prestige < 5 ? "<c>Chocolate Factory VI." : "<e>Click to trade!";
            return ItemStacks.item(Material.COCOA_BEANS, 1, Text.of("<e>Chocolate Fortune"), List.of(
                    Text.of("<7>Permanently gain <6>+1☘ Cocoa Beans"),
                    Text.of("<6>Fortune <7>per tier."),
                    Text.empty(),
                    Text.of("<7>Cost"),
                    Text.of("<6>{} Chocolate", ChocolateFactoryHelper.formatChocolate(2_000_000_000L)),
                    Text.empty(),
                    Text.of(footer)));
        }, (click, c) -> handlePurchase((SkyBlockPlayer) c.player(), 2_000_000_000L, "<e>Chocolate Fortune", 5, c));

        // Slot 48: Go Back
        Components.back(layout, 48, ctx);

        // Slot 49: Close
        Components.close(layout, 49);

        // Slot 50: Chocolate Shop Milestones
        layout.slot(50, (s, c) -> {
            SkyBlockPlayer p = (SkyBlockPlayer) c.player();
            DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(p);

            return ItemStacks.item(Material.LADDER, """
                    <6>Chocolate Shop Milestones
                    <7>Unlock special <a>Chocolate Rabbits <7>by
                    <7>spending <6>Chocolate <7>in the <6>Chocolate
                    <6>Shop<7>.

                    <7>Chocolate Spent: <6>{}

                    <e>Click to view!""",
                    ChocolateFactoryHelper.formatChocolate(data.getTotalChocolateSpent()));
        }, (click, c) -> ((SkyBlockPlayer) c.player()).openView(new GUIChocolateShopMilestones()));
    }

    private ItemStack.Builder createLockedItem(String requirement) {
        return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<c>???"), List.of(
                Text.of("<7>???"),
                Text.empty(),
                Text.of(requirement)));
    }

    private void handlePurchase(SkyBlockPlayer player, long cost, String itemName, int requiredPrestige, ViewContext c) {
        DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);

        if (data.getPrestigeLevel() < requiredPrestige) {
            player.sendMessage("<c>You don't meet the requirements for this item!");
            return;
        }

        if (data.getChocolate() >= cost) {
            data.removeChocolate(cost);
            data.addChocolateSpent(cost);
            ChocolateFactoryHelper.saveData(player, data);
            player.sendMessage("<a>Purchased {} <a>for <6>{} Chocolate<a>!", Text.of(itemName),
                    ChocolateFactoryHelper.formatChocolate(cost));
            c.session(State.class).refresh();
        } else {
            player.sendMessage("<c>You don't have enough Chocolate!");
        }
    }

}
