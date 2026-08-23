package net.swofty.type.backwaterbayou.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

// TODO: use ShopView
public class GUIJunkerJoel extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Junker Joel", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slot(10, ItemStacks.head(
            "d24892a3142d2e130e5feb88b805b83de905489d2ccd1d031b9d7a2922b96500",
            """
                    <9>Junk Sinker
                    <7>Grants <6>+10⛃ Treasure Chance</6> while
                    <7>in the <2>Backwater Bayou</2> but replaces
                    <7>all <6>Treasure</6> catches with <2>Junk</2>!

                    <7>Talk to <2>Roddy</2> in the <2>Backwater
                    <2>Bayou <7>to apply this <9>Sinker</9> to a
                    <7>Fishing Rod.

                    <9><l>RARE ROD PART</l>

                    <7>Cost
                    <6>10,000 Coins

                    <e>Click to trade!"""
        ));
        layout.slot(11, ItemStacks.head(
            "c1695c80854447b5db5a0ee6d57ef0a7d91d815bd7e6318c516a39d12fe0639e",
            """
                    <9>Treasure Bait <8>x16
                    <8>Fishing Bait
                    <8>Consumes on Cast

                    <7>Grants <b>+10☂ Fishing Speed</b> and <6>+2⛃
                    <6>Treasure Chance<7>.

                    <9><l>RARE BAIT</l>

                    <7>Cost
                    <a>Rusty Coin

                    <e>Click to trade!
                    <e>Right-click for more trading options!"""
        ).amount(16));
        layout.slot(12, ItemStacks.item(Material.FISHING_ROD, """
                <a>Challenging Rod
                <7>Damage: <c>+75
                <7>Strength: <c>+75
                <7>Fishing Speed: <b>+35
                <7>Sea Creature Chance: <3>+2%

                <9>ථ Hook <8><l>NONE</l>
                <9>ꨃ Line <8><l>NONE</l>
                <9>࿉ Sinker <8><l>NONE</l>

                <7>Talk to <2>Roddy</2> in the <2>Backwater
                <2>Bayou <7>to apply parts to this rod.

                <8>This item can be reforged!
                <a><l>UNCOMMON FISHING ROD</l>

                <7>Cost
                <f>Fishing Rod
                <a>Rusty Coin <8>x16

                <e>Click to trade!"""));
        layout.slot(13, ItemStacks.head(
            "f6f6c3ebab908a184d49a0f8c85edd3ed48a65d69213bc0367db67a7a1c0c3a7",
            """
                    <a>Backwater Helmet
                    <7>Health: <c>+30
                    <7>Defense: <a>+15
                    <7>Sea Creature Chance: <3>+1%
                    <7>Treasure Chance: <6>+0.1%

                    <8>Tiered Bonus: Swamp Soldier (0/8)
                    <7>Increases damage dealt to <3>Sea
                    <3>Creatures <7>by <a>5%</a>.

                    <8>This item can be reforged!
                    <a><l>UNCOMMON HELMET</l>

                    <7>Cost
                    <f>Angler Helmet
                    <a>Rusty Coin <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(14, ItemStacks.item(Material.LEATHER_CHESTPLATE, """
                <a>Backwater Chestplate
                <7>Health: <c>+50
                <7>Defense: <a>+25
                <7>Sea Creature Chance: <3>+1%
                <7>Treasure Chance: <6>+0.1%

                <8>Tiered Bonus: Swamp Soldier (0/8)
                <7>Increases damage dealt to <3>Sea
                <3>Creatures <7>by <a>5%</a>.

                <8>This item can be reforged!
                <a><l>UNCOMMON CHESTPLATE</l>

                <7>Cost
                <f>Angler Chestplate
                <a>Rusty Coin <8>x4

                <e>Click to trade!"""));
        layout.slot(15, ItemStacks.item(Material.LEATHER_LEGGINGS, """
                <a>Backwater Leggings
                <7>Health: <c>+40
                <7>Defense: <a>+20
                <7>Sea Creature Chance: <3>+1%
                <7>Treasure Chance: <6>+0.1%

                <8>Tiered Bonus: Swamp Soldier (0/8)
                <7>Increases damage dealt to <3>Sea
                <3>Creatures <7>by <a>5%</a>.

                <8>This item can be reforged!
                <a><l>UNCOMMON LEGGINGS</l>

                <7>Cost
                <f>Angler Leggings
                <a>Rusty Coin <8>x4

                <e>Click to trade!"""));
        layout.slot(16, ItemStacks.item(Material.LEATHER_BOOTS, """
                <a>Backwater Boots
                <7>Health: <c>+20
                <7>Defense: <a>+10
                <7>Sea Creature Chance: <3>+1%
                <7>Treasure Chance: <6>+0.1%

                <8>Tiered Bonus: Swamp Soldier (0/8)
                <7>Increases damage dealt to <3>Sea
                <3>Creatures <7>by <a>5%</a>.

                <8>This item can be reforged!
                <a><l>UNCOMMON BOOTS</l>

                <7>Cost
                <f>Angler Boots
                <a>Rusty Coin <8>x4

                <e>Click to trade!"""));
        layout.slot(19, ItemStacks.head(
            "d24c6d00c53b51685a6be7453d236228f9837f1c1e27a9175813983ca49c792f",
            """
                    <f>Junk Talisman
                    <7>Grants <6>+2.5⛃ Treasure Chance</6> while
                    <7>on the <2>Backwater Bayou</2>.

                    <8><o>One man's trash is another man's</o>
                    <8><o>treasure.</o>

                    <8>Works while in Accessory Bag!
                    <f><l>COMMON ACCESSORY</l>

                    <7>Cost
                    <a>Rusty Coin <8>x32

                    <e>Click to trade!"""
        ));
        layout.slot(20, ItemStacks.head(
            "4c920d3593ed4936defc894b88c43a2bb0b50c3a1e9a6dd8e859cb27bd3cabd",
            """
                    <a>Junk Ring
                    <7>Grants <6>+5⛃ Treasure Chance</6> while
                    <7>on the <2>Backwater Bayou</2>.

                    <8><o>One man's trash is another man's</o>
                    <8><o>treasure.</o>

                    <8>Works while in Accessory Bag!
                    <a><l>UNCOMMON ACCESSORY</l>

                    <7>Cost
                    <f>Junk Talisman
                    <9>Busted Belt Buckle <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(21, ItemStacks.head(
            "9727812f708dee1826bceecdadb9c7719e3d0f385a3b2515d00b5f665d8ba83e",
            """
                    <9>Junk Artifact
                    <7>Grants <6>+7.5⛃ Treasure Chance</6> while
                    <7>on the <2>Backwater Bayou</2>.

                    <8><o>One man's trash is another man's</o>
                    <8><o>treasure.</o>

                    <8>Works while in Accessory Bag!
                    <9><l>RARE ACCESSORY</l>

                    <7>Cost
                    <a>Junk Ring
                    <5>Old Leather Boot

                    <e>Click to trade!"""
        ));
        layout.slot(22, ItemStacks.head(
            "ab6f8eea74ca22c2ab64592bab2699df39c3e7c1db7d2c9fc687be0dc8c7f1ed",
            """
                    <a>Backwater Necklace
                    <7>Fishing Speed: <b>+2
                    <7>Sea Creature Chance: <3>+1%
                    <7>Treasure Chance: <6>+0.1%

                    <8>Tiered Bonus: Swamp Soldier (0/8)
                    <7>Increases damage dealt to <3>Sea
                    <3>Creatures <7>by <a>5%</a>.

                    <8>This item can be reforged!
                    <a><l>UNCOMMON NECKLACE</l>

                    <7>Cost
                    <f>Angler Necklace
                    <a>Rusty Coin <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(23, ItemStacks.head(
            "f2adeecbf20b58fd4cad8aaa3b4653d7165e07aa167be48a321b096d56a9fe35",
            """
                    <a>Backwater Cloak
                    <7>Fishing Speed: <b>+2
                    <7>Sea Creature Chance: <3>+1%
                    <7>Treasure Chance: <6>+0.1%

                    <8>Tiered Bonus: Swamp Soldier (0/8)
                    <7>Increases damage dealt to <3>Sea
                    <3>Creatures <7>by <a>5%</a>.

                    <8>This item can be reforged!
                    <a><l>UNCOMMON CLOAK</l>

                    <7>Cost
                    <f>Angler Cloak
                    <a>Rusty Coin <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(24, ItemStacks.head(
            "3c150be849a39208a38a83c5605e79aef93a12b37072b931693990192cb77a19",
            """
                    <a>Backwater Belt
                    <7>Fishing Speed: <b>+2
                    <7>Sea Creature Chance: <3>+1%
                    <7>Treasure Chance: <6>+0.1%

                    <8>Tiered Bonus: Swamp Soldier (0/8)
                    <7>Increases damage dealt to <3>Sea
                    <3>Creatures <7>by <a>5%</a>.

                    <8>This item can be reforged!
                    <a><l>UNCOMMON BELT</l>

                    <7>Cost
                    <f>Angler Belt
                    <a>Rusty Coin <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(25, ItemStacks.head(
            "9a6c887b86b2a59fdea5052413eab74d434c07f52231ce7ac33af5b395beded0",
            """
                    <a>Backwater Gloves
                    <7>Fishing Speed: <b>+2
                    <7>Sea Creature Chance: <3>+1%
                    <7>Treasure Chance: <6>+0.1%

                    <8>Tiered Bonus: Swamp Soldier (0/8)
                    <7>Increases damage dealt to <3>Sea
                    <3>Creatures <7>by <a>5%</a>.

                    <8>This item can be reforged!
                    <a><l>UNCOMMON GLOVES</l>

                    <7>Cost
                    <f>Angler Bracelet
                    <a>Rusty Coin <8>x4

                    <e>Click to trade!"""
        ));
        layout.slot(28, ItemStacks.head(
            "26629dfa3fdfef04054024e0156d5e19da5401b1911f59b4bd3982685fe54c2c",
            """
                    <7>[Lvl 100] <f>Hermit Crab
                    <8>Fishing Pet

                    <7>Defense: <a>+20
                    <7>Fishing Speed: <b>+20
                    <7>Sea Creature Chance: <3>+2%

                    <6>Comfort Zone
                    <7>Grants <b>+20☂ Fishing Speed</b> for <a>30s
                    <7>upon catching <6>Treasure</6>.

                    <c>This is a preview of Lvl 100.
                    <c>New pets are lowest level!

                    <7>Cost
                    <a>Rusty Coin <8>x32

                    <e>Click to trade!"""
        ));
        layout.slot(29, ItemStacks.head(
            "e886d5cac32bd32fc07938908c552b7b27965d92065b3157dfc7ef849281ee9d",
            """
                    <9>Stingy Sinker
                    <7>Grants a <a>10%</a> chance to not consume
                    <7>Bait.

                    <7>Talk to <2>Roddy</2> in the <2>Backwater
                    <2>Bayou <7>to apply this <9>Sinker</9> to a
                    <7>Fishing Rod.

                    <9><l>RARE ROD PART</l>

                    <7>Cost
                    <a>Rusty Coin <8>x64

                    <e>Click to trade!"""
        ));
        layout.slot(30, ItemStacks.head(
            "5cbac3c84e21e65ec88007604c4eba1da391e185544b90252fc16ca695c59b4b",
            """
                    <9>Speedy Line
                    <7>Grants <b>+10☂ Fishing Speed</b>.

                    <7>Talk to <2>Roddy</2> in the <2>Backwater
                    <2>Bayou <7>to apply this <9>Line</9> to a Fishing
                    <7>Rod.

                    <9><l>RARE ROD PART</l>

                    <7>Cost
                    <9>Busted Belt Buckle <8>x8

                    <e>Click to trade!"""
        ));
        layout.slot(31, ItemStacks.head(
            "9172c1e729e0ca00193ab5d43e893fabedf5a80fc647258176e8502432885925",
            """
                    <9>Bronze Ship Engine
                    <8>Ship Part

                    <7>Bring this item to <6>Captain Baha</6>.

                    <7>Grants <3>+0.5☯ Fishing Wisdom</3> when
                    <7>attached to your <6>Ship</6>.

                    <8><l>* Soulbound *</l>
                    <9><l>RARE</l>

                    <7>Cost
                    <a>Rusty Coin <8>x64
                    <9>Busted Belt Buckle <8>x8

                    <e>Click to trade!"""
        ));
        layout.slot(32, ItemStacks.head(
            "269698fd92fb14827af97e54a3f28f5e2685d7e94bd128c0c27f259df996717c",
            """
                    <6>Gold Bottle Cap
                    <8>Combinable in Anvil

                    <7>When applied to a fishing rod,
                    <7>increases its <9>Luck of the Sea</9> level
                    <7>by <a>1</a>!
                    <8>Can be applied once.
                    <8>Requires Luck of the Sea VI!

                    <8><o>One man's trash is, indeed, another</o>
                    <8><o>man's treasure.</o>

                    <6><l>LEGENDARY</l>

                    <7>Cost
                    <a>Rusty Coin <8>x512
                    <9>Busted Belt Buckle <8>x64
                    <5>Old Leather Boot <8>x8

                    <e>Click to trade!"""
        ));
        layout.slot(33, ItemStacks.head(
            "9809753cbab0380c7a1c18925faf9b51e44caadd1e5748542b0f23835f4ef64e",
            """
                    <9>Treasure Hook
                    <7>Only allows you to catch items and
                    <6>Treasure<7>.

                    <7>Talk to <2>Roddy</2> in the <2>Backwater
                    <2>Bayou <7>to apply this <9>Hook</9> to a Fishing
                    <7>Rod.

                    <4>❣ <c>Requires <a>Fishing Skill 25</a>.
                    <9><l>RARE ROD PART</l>

                    <7>Cost
                    <6>Bayou Water Orb
                    <5>Old Leather Boot <8>x4
                    <9>Busted Belt Buckle <8>x32
                    <a>Rusty Coin <8>x256

                    <c>Not unlocked!"""
        ));
        layout.slot(34, ItemStacks.item(Material.MAP, """
                <9>Travel Scroll to the Bayou
                <7>Consume this item to add its
                <7>destination to your Fast Travel
                <7>options.

                <7>Island: <a>Backwater Bayou
                <7>Teleport: <e>Spawn

                <9><l>RARE TRAVEL SCROLL</l>

                <7>Cost
                <9>Busted Belt Buckle <8>x8

                <e>Click to trade!"""));
        layout.slot(49, ItemStacks.item(Material.HOPPER, """
                <a>Sell Item
                <7>Click items in your inventory to sell
                <7>them to this Shop!"""));
    }
}
