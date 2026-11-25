package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.consumable.ConsumableConfigResolver.ConsumableContext;
import io.github.term4.polyp.platform.fixes.FixesConfig;
import io.github.term4.polyp.platform.fixes.FixToggleConfig;
import io.github.term4.polyp.presets.vanilla18.Consumables;
import io.github.term4.polyp.mechanics.consumable.catalog.VanillaConsumables;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.EntityStatusPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The vanilla {@code Consumable.canConsume} gate: a creative player can't START eating food in 1.8 (so a 1.8 viewer
 * never sees a phantom eating animation), but CAN in 26; drinks (milk/potion) are ungated in both.
 */
class ConsumableGateTest extends HeadlessServerTest {

    private static FakePlayer player;

    @BeforeAll
    static void connectAndInstall() {
        player = FakePlayer.connect(instance, new Pos(50.5, 64, 50.5), "Eater");
        ConsumableSystem.install(polyp, Consumables.config());
    }

    private static boolean canConsume(Material material, Consumable type,
                                      ConsumableConfig cfg) {
        ConsumableContext ctx = new ConsumableContext(player.player, ItemStack.of(material), PlayerHand.MAIN, type, services);
        return ConsumableConfigResolver.resolve(cfg, ctx).canConsume();
    }

    @Test
    void legacyBlocksCreativeFoodButNotDrink() {
        var cfg = Consumables.config();
        player.player.setGameMode(GameMode.SURVIVAL);
        assertTrue(canConsume(Material.GOLDEN_APPLE, VanillaConsumables.GOLDEN_APPLE, cfg), "survival can eat a golden apple");

        player.player.setGameMode(GameMode.CREATIVE);
        assertFalse(canConsume(Material.GOLDEN_APPLE, VanillaConsumables.GOLDEN_APPLE, cfg), "1.8 creative can't start eating food");
        assertFalse(canConsume(Material.ENCHANTED_GOLDEN_APPLE, VanillaConsumables.ENCHANTED_GOLDEN_APPLE, cfg), "same for the notch apple");
        assertTrue(canConsume(Material.MILK_BUCKET, VanillaConsumables.MILK_BUCKET, cfg), "drinks are ungated (creative can drink milk)");
        assertTrue(canConsume(Material.POTION, VanillaConsumables.POTION, cfg), "creative can drink a potion");
        player.player.setGameMode(GameMode.SURVIVAL);
    }

    @Test
    void modernAllowsCreativeFood() {
        var cfg = io.github.term4.polyp.presets.vanilla.Consumables.config();
        player.player.setGameMode(GameMode.CREATIVE);
        assertTrue(canConsume(Material.GOLDEN_APPLE, VanillaConsumables.GOLDEN_APPLE, cfg), "26 creative eats (no item loss) - the version difference");
        player.player.setGameMode(GameMode.SURVIVAL);
    }

    /** The wire effect: a blocked use zeroes the item's native consumable duration so the eating state never starts. */
    @Test
    void blockedUseZeroesItemUseTime() {
        var node = MinecraftServer.getGlobalEventHandler();
        player.player.setGameMode(GameMode.CREATIVE);
        var creative = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(creative);
        assertEquals(0, creative.getItemUseTime(), "creative food use is zeroed - no eating animation on the wire");

        player.player.setGameMode(GameMode.SURVIVAL);
        var survival = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(survival);
        assertTrue(survival.getItemUseTime() > 0, "survival food starts the eat (drives the duration)");

        player.player.setGameMode(GameMode.CREATIVE);
        var drink = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.MILK_BUCKET), 32);
        EventDispatcher.call(drink);
        assertTrue(drink.getItemUseTime() > 0, "creative can still drink milk");
        player.player.setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Vanilla {@code die()} stops item use; Minestom's {@code kill()} leaves the timer armed, so an eat would
     * finish on the corpse - effects applied and the item eaten. Death must clear the use state.
     */
    @Test
    void deathStopsAnInProgressConsume() {
        player.player.setGameMode(GameMode.SURVIVAL);
        player.player.setHealth(20f);
        player.player.refreshItemUse(PlayerHand.MAIN, 32);
        assertTrue(player.player.isUsingItem(), "mid-eat");

        player.player.kill();

        assertFalse(player.player.isUsingItem(), "death stopped the use");
        assertEquals(0, player.player.getCurrentItemUseTime(), "and disarmed the finish timer");
        player.player.setHealth(20f);
        player.player.clearItemUse();
    }

    /**
     * The opt-in legacy consume fix ({@code FixesConfig.legacyConsume}): while a LEGACY client is already mid-use a fresh
     * consume is refused (no spam-restart double-eat), but a modern client - which gates itself - is never touched.
     */
    @Test
    void reuseGateRefusesAReUseOnlyForLegacyWhenEnabled() {
        player.player.setGameMode(GameMode.SURVIVAL);
        polyp.clientInfo().setProxyDetails(player.player, "{\"version\": 47}"); // 1.8

        // Fix OFF (default): a re-use while mid-use still starts - the raw behavior
        polyp.profiles().setGlobal(MechanicsProfile.builder().build());
        player.player.refreshItemUse(PlayerHand.MAIN, 32);
        var off = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(off);
        assertTrue(off.getItemUseTime() > 0, "with the fix off, a re-use while mid-use still starts");

        // Fix ON + legacy client: the re-use is refused
        polyp.profiles().setGlobal(MechanicsProfile.builder()
                .set(MechanicsKeys.FIXES, FixesConfig.builder().legacyConsume(FixToggleConfig.on()).build())
                .build());
        player.player.refreshItemUse(PlayerHand.MAIN, 32);
        var on = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(on);
        assertEquals(0, on.getItemUseTime(), "with the fix on, a legacy re-use while mid-use is zeroed (no double-eat)");

        // a fresh use (hand freed) still works with the fix on
        player.player.clearItemUse();
        var fresh = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(fresh);
        assertTrue(fresh.getItemUseTime() > 0, "a fresh consume still works");

        // Fix ON but a MODERN client: never gated - it gates its own consumption
        polyp.clientInfo().setProxyDetails(player.player, "{\"version\": 774}"); // modern
        player.player.refreshItemUse(PlayerHand.MAIN, 32);
        var modern = new PlayerUseItemEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE), 32);
        EventDispatcher.call(modern);
        assertTrue(modern.getItemUseTime() > 0, "a modern client's re-use is never gated");

        polyp.profiles().setGlobal(MechanicsProfile.builder().build());
        player.player.clearItemUse();
    }

    /**
     * The legacy count pacing: a finish sends a self {@code entity_status 9} + a {@code window_items} confirm to a 1.8
     * client (so it can't race the count), and neither to a modern client - which just gets the plain decremented slot.
     */
    @Test
    void legacyFinishPacesCountModernDoesNot() {
        player.player.setGameMode(GameMode.SURVIVAL);
        polyp.profiles().setGlobal(MechanicsProfile.builder()
                .set(MechanicsKeys.FIXES, FixesConfig.builder().legacyConsume(FixToggleConfig.on()).build())
                .build());
        int slot = player.player.getHeldSlot();

        polyp.clientInfo().setProxyDetails(player.player, "{\"version\": 47}"); // 1.8
        player.player.getInventory().setItemStack(slot, ItemStack.of(Material.GOLDEN_APPLE, 5));
        player.sent.clear();
        EventDispatcher.call(new PlayerFinishItemUseEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE, 5), 32));
        assertFalse(player.sent(EntityStatusPacket.class).isEmpty(), "legacy finish sends entity_status 9");
        assertFalse(player.sent(WindowItemsPacket.class).isEmpty(), "legacy finish sends a window_items count confirm");

        polyp.clientInfo().setProxyDetails(player.player, "{\"version\": 774}"); // modern
        player.player.getInventory().setItemStack(slot, ItemStack.of(Material.GOLDEN_APPLE, 5));
        player.sent.clear();
        EventDispatcher.call(new PlayerFinishItemUseEvent(player.player, PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_APPLE, 5), 32));
        assertTrue(player.sent(EntityStatusPacket.class).isEmpty(), "modern finish sends no extra status 9");
        assertTrue(player.sent(WindowItemsPacket.class).isEmpty(), "modern finish sends no window_items");

        polyp.profiles().setGlobal(MechanicsProfile.builder().build());
    }
}
