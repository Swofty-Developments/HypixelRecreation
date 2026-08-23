package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIBoosterCookie extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Booster Cookie", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockDataHandler handler = player.getSkyblockDataHandler();
        int gems = handler.get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).getValue();

        layout.slot(11, ItemStacks.item(Material.DIAMOND, """
                <b>Bits
                <7>Bits are earned from booster
                <7>cookies and spent in the community
                <7>shop for unique items.

                <7>Bits Purse: <b>0

                <7>Bits Available: <b>0<3>/0
                <8>Eligible to farm while you have
                <8>the cookie buff.

                <7>Bits Multiplier: <b>1x
                <7>Per <6>Cookie<7>: <b>+4,800 bits <7>available

                <8>Your Fame Rank's bit multiplier
                <8>applies on the bits from every
                <8>cookie you've ever eaten!"""));
        layout.slot(13, ItemStacks.item(Material.COOKIE, """
                <6>Booster Cookie
                <7>Acquire Booster Cookies from the
                <b>Community Center <7>in the <a>Hub<7>.

                <d>Cookie Buff:
                <8>‣ <7>Ability to gain <b>Bits<7>!
                <8>‣ <3>+25☯ <7>on all <3>Wisdom <7>stats
                <8>‣ <b>+15✯ <7>Magic Find
                <8>‣ <7>Keep <6>coins <7>on death
                <8>‣ <e>Permafly <7>on private islands and gardens
                <8>‣ <7>Quick access to some menus using their
                <7>respective commands:
                  <6>/ah<7>, <6>/bazaar<7>, <a>/bank<7>, <6>/accessorybag<7>,
                  <b>/fishingbag<7>, <d>/timepocket<7>, <f>/anvil<7>, <d>/hex<7>,
                  <b>/etable<7>, <d>/potionbag<7>, <d>/rngmeter<7>, <d>/pity<7>,
                  <7>and <e>/quiver
                <8>‣ <7>Sell items directly to the trades and cookie menu
                <8>‣ <7>AFK <a>immunity <7>on your island and garden
                <8>‣ <7>Toggle specific <d>potion effects
                <8>‣ <7>Link your items in chat using <e>/show
                <8>‣ <7>Insta-sell your Material stash to the <6>Bazaar
                <8>‣ <7>Increases <6>Chocolate Factory <7>production by <6>+0.25x
                <8>‣ <7>Allows consuming <9>Mixins <7>directly from your inventory
                <8>‣ <7>Call <3>Abiphone Contacts <7>with <b>/call

                <c>You do not currently have a
                <c>Booster Cookie active!

                <7>Cost
                <a>325 SkyBlock Gems

                <7>You have: <a>{} Gems

                <c>You cannot afford this!
                <e>Click to get store info!""", gems));
        layout.slot(15, ItemStacks.item(Material.GOLDEN_HELMET, """
                <e>Fame Rank
                <7>Earn fame by <6>contributing to city
                <6>projects <7>and spending <b>Bits <7>& <a>Gems
                <7>with Elizabeth.

                <7>Your rank: <e>New player
                <7>Bits Multiplier: <b>1x
                <7>Election Votes: <a>1
                <8>The election is run every SkyBlock
                <8>year.

                <7>Your total: <e>10,850 Fame

                <7>Next rank: <b>Settler
                <6><l><m>           <f><l><m>         <r> <e>10,850<6>/<e>20k
</m>
                <8>You earn 1 fame per bit and
                <8>200 per gem spent in the
                <8>community shop."""));
        layout.slot(28, ItemStacks.head("3b11fb90db7f57beb435954013b1c7ef776c6bd96cbf3308aa8ebac29591ebbd", """
                <d>The Hex
                <7>Access <d>The Hex <7>from anywhere in
                <7>SkyBlock!

                <8>Also accessible via /hex

                <c>Requires Cookie Buff!"""));
        layout.slot(29, ItemStacks.item(Material.ENCHANTING_TABLE, """
                <6>Enchantment Table
                <7>Access an Enchantment Table from
                <7>anywhere in SkyBlock!

                <7>This portable table remembers the
                <7>highest <d>bookshelf power <7>you've seen!

                <8>Also accessible via /enchantingtable

                <c>Requires Cookie Buff!"""));
        layout.slot(30, ItemStacks.item(Material.ANVIL, """
                <6>Anvil
                <7>Access an Anvil from anywhere in
                <7>SkyBlock!

                <8>Also accessible via /anvil

                <c>Requires Cookie Buff!"""));
        layout.slot(32, ItemStacks.item(Material.POTION, """
                <a>Toggle Potion Effects
                <7>Choose which of your potion effects
                <7>are applied to you.

                <c>Requires Cookie Buff!"""));
        layout.slot(33, ItemStacks.item(Material.GOLDEN_HORSE_ARMOR, """
                <6>Auction House
                <7>Access the Auction House menu from
                <7>anywhere in SkyBlock!

                <8>Also accessible via /auctions

                <c>Requires Cookie Buff!"""));
        layout.slot(34, ItemStacks.head("c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7", """
                <6>Bazaar
                <7>Access the Bazaar from anywhere in
                <7>SkyBlock!

                <8>Also accessible via /bazaar

                <c>Requires Cookie Buff!"""));

        layout.slot(50, ItemStacks.item(Material.EMERALD, """
                <a>SkyBlock Gems
                <7>Use SkyBlock Gems to purchase:
                <7> - <6>Booster Cookies
                <7> - <c>Fire Sales
                <7> - <b>Taylor's Cosmetics
                <7> - <a>SkyMart Barn & Greenhouse Skins
                <7> - <d>Account & Profile Upgrades

                <7>You have: <a>{} Gems
                <8>Gems can be purchased from our
                <8>webstore at <b>store.hypixel.net<8>!

                <e>Click to get link!""", gems));
    }
}
