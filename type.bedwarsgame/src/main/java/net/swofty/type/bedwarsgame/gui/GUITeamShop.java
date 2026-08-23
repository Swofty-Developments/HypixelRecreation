package net.swofty.type.bedwarsgame.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.shop.TeamShopManager;
import net.swofty.type.bedwarsgame.shop.TeamUpgrade;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeTier;
import net.swofty.type.bedwarsgame.shop.Trap;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.bedwarsgame.shop.TrapManager;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.util.BedWarsInventoryManipulator;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUITeamShop extends StatelessView {

    private static final int[] UPGRADE_SLOTS = {10, 11, 12, 19, 20, 21};
    private static final int[] TRAP_SHOP_SLOTS = {14, 15, 16, 23, 24, 25};
    private static final int[] TRAP_QUEUE_SLOTS = {39, 40, 41};
    private static final int[] SEPARATOR_SLOTS = {27, 28, 29, 30, 31, 32, 33, 34, 35};

    private final TeamShopManager teamShopService = TypeBedWarsGameLoader.getTeamShopManager();
    private final TrapManager trapManager = TypeBedWarsGameLoader.getTrapManager();

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Team Shop", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        for (int slot : SEPARATOR_SLOTS) {
            layout.slot(slot, ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, " "));
        }

        BedWarsPlayer player = (BedWarsPlayer) ctx.player();
        BedWarsGame game = player.getGame();
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (game == null || teamKey == null) {
            for (int slot : UPGRADE_SLOTS) {
                layout.slot(slot, ItemStacks.named(Material.BARRIER, "<c>No Game/Team"));
            }
            for (int slot : TRAP_SHOP_SLOTS) {
                layout.slot(slot, ItemStacks.named(Material.BARRIER, "<c>No Game/Team"));
            }
            return;
        }

        List<TeamUpgrade> upgrades = teamShopService.getUpgrades();
        List<Trap> traps = trapManager.getTraps();

        for (int i = 0; i < UPGRADE_SLOTS.length; i++) {
            int slot = UPGRADE_SLOTS[i];
            int index = i;
            layout.slot(slot,
                (s, c) -> renderUpgradeItem((BedWarsPlayer) c.player(), upgrades, index),
                (click, c) -> handleUpgradeClick(click, c, upgrades, index)
            );
        }

        for (int i = 0; i < TRAP_SHOP_SLOTS.length; i++) {
            int slot = TRAP_SHOP_SLOTS[i];
            int index = i;
            layout.slot(slot,
                (s, c) -> renderTrapItem((BedWarsPlayer) c.player(), traps, index),
                (click, c) -> handleTrapClick(c, traps, index)
            );
        }

        for (int i = 0; i < TRAP_QUEUE_SLOTS.length; i++) {
            int slot = TRAP_QUEUE_SLOTS[i];
            int index = i;
            layout.slot(slot, (s, c) -> renderTrapQueueItem((BedWarsPlayer) c.player(), traps, index));
        }
    }

    @Override
    public boolean onBottomClick(ClickContext<DefaultState> click, ViewContext ctx) {
        return false;
    }

    private void handleUpgradeClick(ClickContext<DefaultState> click, ViewContext ctx, List<TeamUpgrade> upgrades, int index) {
        if (index >= upgrades.size()) return;

        BedWarsPlayer player = (BedWarsPlayer) click.player();
        BedWarsGame game = player.getGame();
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (game == null || teamKey == null) return;

        TeamUpgrade upgrade = upgrades.get(index);
        TeamUpgradeTier nextTier = upgrade.getNextTier(game, teamKey);
        if (nextTier == null) {
            player.sendMessage("<c>Your team has already bought this upgrade!");
            playClickSound(player);
            return;
        }
        if (!upgrade.hasEnoughCurrency(player, nextTier)) {
            player.sendMessage("<c>You don't have enough {}!", nextTier.getCurrency().getName());
            playClickSound(player);
            return;
        }

        upgrade.purchase(game, player);
        playBuySound(player);
        ctx.session(DefaultState.class).refresh();
    }

    private ItemStack.Builder renderUpgradeItem(BedWarsPlayer player, List<TeamUpgrade> upgrades, int index) {
        if (index >= upgrades.size()) return ItemStack.builder(Material.AIR);

        BedWarsGame game = player.getGame();
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (game == null || teamKey == null) {
            return ItemStacks.named(Material.BARRIER, "<c>No Game");
        }

        TeamUpgrade upgrade = upgrades.get(index);
        TeamUpgradeTier nextTier = upgrade.getNextTier(game, teamKey);
        boolean isMaxed = nextTier == null;
        boolean canAfford = !isMaxed && upgrade.hasEnoughCurrency(player, nextTier);

        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<7>{}", upgrade.getDescription()));
        lore.add(Text.empty());

        int currentLevel = upgrade.getCurrentLevel(game, teamKey);
        for (TeamUpgradeTier tier : upgrade.getTiers()) {
            boolean owned = tier.getLevel() <= currentLevel;
            boolean next = !owned && !isMaxed && tier.getLevel() == nextTier.getLevel();
            String colour = owned ? "<a>" : (next ? "<e>" : "<7>");
            lore.add(Text.of(colour + "Tier {}: {}, <b>{} {}",
                tier.getLevel(),
                tier.getDescription(),
                tier.getPrice(),
                tier.getCurrency().getName() + (tier.getPrice() != 1 ? "s" : "")));
        }

        lore.add(Text.empty());
        if (isMaxed) {
            lore.add(Text.of("<a>UNLOCKED"));
        } else if (canAfford) {
            lore.add(Text.of("<e>Click to purchase!"));
        } else {
            lore.add(Text.of("<c>You don't have enough {}!", nextTier.getCurrency().getName()));
        }

        return ItemStacks.text(upgrade.getDisplayItem().builder(),
            Text.of(isMaxed || canAfford ? "<a>{}" : "<c>{}", upgrade.getName()),
            lore);
    }

    private void handleTrapClick(ViewContext ctx, List<Trap> traps, int index) {
        if (index >= traps.size()) return;

        BedWarsPlayer player = (BedWarsPlayer) ctx.player();
        BedWarsGame game = player.getGame();
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (game == null || teamKey == null) return;

        int trapSize = game.getTeamTraps(teamKey).size();
        if (trapSize >= 3) {
            player.sendMessage("<c>You can't have more traps than 3!");
            playClickSound(player);
            return;
        }

        Trap trap = traps.get(index);
        int price = trap.getPrice(game, teamKey);
        int owned = Arrays.stream(player.getInventory().getItemStacks())
            .filter(s -> s.material() == trap.getCurrency().getMaterial())
            .mapToInt(ItemStack::amount)
            .sum();
        if (owned < price) {
            player.sendMessage("<c>You don't have enough {}!", trap.getCurrency().getName());
            playClickSound(player);
            return;
        }

        BedWarsInventoryManipulator.removeItems(player, trap.getCurrency().getMaterial(), price);
        game.addTeamTrap(teamKey, trap.getId());
        broadcastTeamPurchase(game, teamKey, player, trap.getName());
        playBuySound(player);
        ctx.session(DefaultState.class).refresh();

        if (trapSize == 2) {
            for (BedWarsPlayer teamPlayer : game.getPlayersOnTeam(player.getTeamKey())) {
                if (teamPlayer.allowsPersistentProgress()) {
                    teamPlayer.getAchievementHandler().completeAchievement("bedwars.minefield");
                }
            }
        }
    }

    private ItemStack.Builder renderTrapItem(BedWarsPlayer player, List<Trap> traps, int index) {
        if (index >= traps.size()) return ItemStack.builder(Material.AIR);

        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        BedWarsGame game = player.getGame();
        if (game == null || teamKey == null) {
            return ItemStacks.named(Material.BARRIER, "<c>No Game");
        }

        Trap trap = traps.get(index);
        int price = trap.getPrice(game, teamKey);
        int owned = Arrays.stream(player.getInventory().getItemStacks())
            .filter(s -> s.material() == trap.getCurrency().getMaterial())
            .mapToInt(ItemStack::amount)
            .sum();
        boolean canAfford = owned >= price;

        return ItemStacks.text(trap.getDisplayItem().builder(),
            Text.of(canAfford ? "<a>{}" : "<c>{}", trap.getName()),
            List.of(
                Text.of("<7>{}", trap.getDescription()),
                Text.empty(),
                Text.of("<7>Cost: <b>{} {}", price,
                    trap.getCurrency().getName() + (price != 1 ? "s" : "")),
                Text.empty(),
                canAfford
                    ? Text.of("<e>Click to purchase!")
                    : Text.of("<c>You don't have enough {}!", trap.getCurrency().getName())
            ));
    }

    private ItemStack.Builder renderTrapQueueItem(BedWarsPlayer player, List<Trap> traps, int index) {
        BedWarsGame game = player.getGame();
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (game == null || teamKey == null) {
            return ItemStacks.named(Material.BARRIER, "<c>No Game");
        }

        List<TrapId> queued = game.getTeamTraps(teamKey);
        if (index < queued.size()) {
            Trap trap = trapManager.getTrap(queued.get(index));
            if (trap != null) {
                return ItemStacks.named(Material.GRAY_STAINED_GLASS_PANE, index + 1,
                    "<b>Trap #{}: {}", index + 1, trap.getName());
            }
        }

        return ItemStacks.item(Material.GRAY_STAINED_GLASS, index + 1, """
            <c>Trap #{}: No Trap
            <7>The first enemy to walk into your
            <7>base will trigger this trap!

            <7>Purchasing a trap will queue it here.
            <7>Its cost scales with traps queued.""", index + 1);
    }

    private void broadcastTeamPurchase(BedWarsGame game, BedWarsMapsConfig.TeamKey teamName, BedWarsPlayer buyer, String name) {
        for (BedWarsPlayer pl : game.getPlayers()) {
            if (teamName.equals(pl.getTeamKey())) {
                pl.sendMessage("{} <a>purchased <6>{}!", Text.of("<color:{}> ", buyer.getTeamKey().chatColor()), name);
            }
        }
    }

    private void playClickSound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1.0f, 1.0f));
    }

    private void playBuySound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.MASTER, 1.0f, 1.0f));
    }
}
