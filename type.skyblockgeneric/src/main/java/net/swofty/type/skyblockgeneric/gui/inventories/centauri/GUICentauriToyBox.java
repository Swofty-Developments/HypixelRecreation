package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHOTM;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.slayer.SlayerType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUICentauriToyBox implements View<GUICentauriToyBox.State> {
    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Toy Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        info(layout, 4, Material.CHEST, "<a>Toy Box", "<7>A variety of useful and cheaty", "<7>utilities to make your life easier!");
        action(layout, 10, Material.COOKIE, "<6>Cookie Jar", "<8>I'd give you a cookie, but I ate it.", "<7>Claim free infinite <6>Booster Cookies<7>!", p -> give(p, ItemType.BOOSTER_COOKIE)); //should give years worth of cookie buff once cookie is impl
        action(layout, 11, Material.POTION, "<6>Potion Seller", "<8>My potions are too strong for you.", "<7>Claim a <c>God Potion<7>!", p -> give(p, ItemType.GOD_POTION)); //should give all potion effects instead of god potion for a long time
        action(layout, 12, Material.DIAMOND_PICKAXE, "<6>Max Items", "<8>The Great Sword begotten in dragon's breath...", "<7>Maximise the stack in your hand.", this::maxItem);
        action(layout, 13, Material.GOLD_BLOCK, "<6>Duplicate Item", "<8>The original is impossible to discern.", "<7>Duplicate the item you are holding.", this::duplicate);
        action(layout, 14, Material.PAINTING, "<6>Max Collections", "<8>Gotta collect 'em all.", "<7>Max every available collection.", this::maxCollections);
        action(layout, 15, Material.DIAMOND_SWORD, "<6>Max Skills", "<8>Gotta collect 'em all.", "<7>Max every skill.", this::maxSkills);
        action(layout, 16, Material.ROTTEN_FLESH, "<6>Max Slayers", "<8>Gotta collect 'em all.", "<7>Impress Maddox!", this::maxSlayers);
        action(layout, 19, Material.PRISMARINE_CRYSTALS, "<5>50k HOTM Exp", "<8>The power of a pickaxe.", "<7>Gain <5>50,000 HOTM Experience<7>.", p -> hotm(p).addExperience(50_000));
        action(layout, 20, Material.GUNPOWDER, "<a>Big ol' chunk o' Powder!", "<8>Mindcraft.", "<7>Get 2,000,000 of each powder.", p -> { hotm(p).addMithrilPowder(2_000_000); hotm(p).addGemstonePowder(2_000_000); });
        action(layout, 21, Material.EMERALD, "<a>5k Gems", "<8>Get rich quick.", "<7>Set your gem purse to 5,000.", p -> p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.GEMS, DatapointInteger.class).setValue(5_000));
        unavailable(layout, 22, Material.EXPERIENCE_BOTTLE, "<a>Set RNG Meter Progress", "<7>Set every RNG Meter to a custom value.");
        unavailable(layout, 23, Material.GLASS_BOTTLE, "<a>Clear RNG Meter Progress", "<7>Clear every RNG Meter.");
        unavailable(layout, 24, Material.OAK_LOG, "<a>Fill Agatha's Rewards", "<7>Grant 25 contest rewards.");
        action(layout, 25, Material.SEA_LANTERN, "<a>Big ol' chunk o' Whispers!", "<8>To turn your tears to roses.", "<7>Get 1,000,000 Forest Whispers.", p -> p.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.FOREST_WHISPERS, DatapointLong.class).setValue(1_000_000L));
        unavailable(layout, 28, Material.PRISMARINE_SHARD, "<a>Shards!", "<7>Get 10 of each available Shard.");
        unavailable(layout, 29, Material.GREEN_DYE, "<2>Pile of Sowdust", "<7>Give yourself <2>1,000,000,000 Sowdust<7>.");
        unavailable(layout, 30, Material.BRICKS, "<c>Vault of Copper", "<7>Give yourself <c>1,000,000 Copper<7>.");
        unavailable(layout, 31, Material.SUNFLOWER, "<a>\"Organically Grown\" Garden Levels!", "<7>Increase Garden Experience by a custom amount.");
        info(layout, 50, Material.OAK_SIGN, "<a>Got Suggestions?", "<7>Share Alpha Network utility ideas in our discord!");
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private void duplicate(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.addAndUpdateItem(held);
    }

    private void maxItem(SkyBlockPlayer player) {
        ItemStack held = player.getItemInMainHand();
        if (held.isAir()) { player.sendMessage("<c>Hold an item first!"); return; }
        player.updateItemInSlot(player.getHeldSlot(), item -> {
            int applied = CentauriItemMaximizer.maximize(player, item);
            if (applied == 0) player.sendMessage("<e>That item has no supported upgrades left to apply.");
            else player.sendMessage("<a>Applied <e>{} <a>maximum item upgrades!", applied);
        });
    }

    private void maxCollections(SkyBlockPlayer player) {
        CollectionCategories.getCategories().forEach(category -> {
            for (var collection : category.getCollections()) player.getCollection().set(collection.type(), Integer.MAX_VALUE);
        });
    }

    private void maxSkills(SkyBlockPlayer player) {
        var skills = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SKILLS, DatapointSkills.class).getValue();
        for (SkillCategories category : SkillCategories.values()) skills.setRaw(player, category, 1_000_000_000D);
    }

    private void maxSlayers(SkyBlockPlayer player) {
        var slayers = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue();
        for (SlayerType type : SlayerType.values()) slayers.progress(type).setXp(Integer.MAX_VALUE);
    }

    private DatapointHOTM.PlayerHOTMData hotm(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.HOTM, DatapointHOTM.class).getValue();
    }

    private void give(SkyBlockPlayer player, ItemType type) { player.addAndUpdateItem(type); }
    private void success(SkyBlockPlayer player) { player.sendMessage("<a>Centauri granted your request!"); }

    private void action(ViewLayout<State> layout, int slot, Material material, String name, String flavor, String description, java.util.function.Consumer<SkyBlockPlayer> action) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, flavor, " ", description, " ", "<e>Click to claim!"),
                (click, c) -> { action.accept((SkyBlockPlayer) c.player()); success((SkyBlockPlayer) c.player()); });
    }

    private void unavailable(ViewLayout<State> layout, int slot, Material material, String name, String description) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, "<8>Coming from Centauri's toy box.", " ", description, " ", "<e>Click to claim!"),
                (click, c) -> c.player().sendMessage("<c>This progression system is not available on this server type yet."));
    }

    private void info(ViewLayout<State> layout, int slot, Material material, String name, String... lore) {
        layout.slot(slot, (s, c) -> GUICentauri.item(name, material, lore));
    }

    public record State() {}
}
