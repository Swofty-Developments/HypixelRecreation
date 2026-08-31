package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.AquaAffinity;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Efficiency;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.ConnectionState;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityAttributesPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Player equipment attributes are tick-driven (vanilla {@code detectEquipmentUpdates}), not synchronous on the equip -
 * a synchronous push races the use-item client prediction, but the tick lag leaves a held-swap window.
 * {@link AttributeConfig#attributeSwapping} gates that window: disabled (default) refreshes the held push immediately
 * on a hotbar-slot change. Mobs are unaffected.
 */
class PlayerEquipAttributeTest extends HeadlessServerTest {

    private static ItemStack aquaHelmet() {
        return enchanted(Material.DIAMOND_HELMET, AquaAffinity.KEY, 1);
    }

    private static ItemStack efficiencyPick() {
        return enchanted(Material.DIAMOND_PICKAXE, Efficiency.KEY, 1);
    }

    private static boolean hasAqua(LivingEntity e) {
        return e.getAttribute(Attribute.SUBMERGED_MINING_SPEED).modifiers().stream()
                .anyMatch(m -> m.operation() == AttributeOperation.ADD_MULTIPLIED_TOTAL && Math.abs(m.amount() - 4.0) < 1e-9);
    }

    private static boolean hasMining(LivingEntity e) {
        var inst = e.getAttribute(Attribute.MINING_EFFICIENCY);
        return inst != null && !inst.modifiers().isEmpty();
    }

    /** Joined-state player recording the packets sent to its own client. */
    private static final class Recorded {
        final Player player;
        final List<SendablePacket> sent = new ArrayList<>();

        Recorded() {
            PlayerConnection conn = new PlayerConnection() {
                @Override public void sendPacket(SendablePacket packet) { sent.add(packet); }
                @Override public SocketAddress getRemoteAddress() { return new InetSocketAddress("localhost", 0); }
            };
            conn.setServerState(ConnectionState.PLAY); // onAttributeChanged syncs to self only when state == PLAY
            this.player = new Player(conn, new GameProfile(UUID.randomUUID(), "Tester"));
            this.player.getInventory().addViewer(this.player);
        }

        void permitAttributeSwapping() {
            polyp.profiles().setPlayer(player, MechanicsProfile.builder()
                    .set(MechanicsKeys.ATTRIBUTES, AttributeConfig.builder().attributeSwapping(true).build()).build());
        }

        void tick() { EventDispatcher.call(new EntityTickEvent(player)); }

        boolean clientGotAqua() {
            return sent.stream()
                    .filter(p -> p instanceof EntityAttributesPacket).map(p -> (EntityAttributesPacket) p)
                    .flatMap(p -> p.properties().stream())
                    .filter(prop -> prop.attribute().equals(Attribute.SUBMERGED_MINING_SPEED))
                    .flatMap(prop -> prop.modifiers().stream())
                    .anyMatch(m -> Math.abs(m.amount() - 4.0) < 1e-9);
        }
    }

    @Test
    void armorEquipAppliesOnTickNotSynchronously() {
        Recorded r = new Recorded();
        r.player.setEquipment(EquipmentSlot.HELMET, aquaHelmet());
        assertFalse(hasAqua(r.player), "a player equip must NOT apply synchronously - the synchronous push races the use-item client prediction");
        r.tick();
        assertTrue(hasAqua(r.player), "the per-tick reconcile applies it (vanilla detectEquipmentUpdates timing)");
    }

    @Test
    void tickReconcileSendsAquaToClient() {
        Recorded r = new Recorded();
        r.player.setEquipment(EquipmentSlot.HELMET, aquaHelmet());
        r.sent.clear(); // drop the equip-time slot packets
        r.tick();
        assertTrue(r.clientGotAqua(), "after the reconcile the client receives SUBMERGED_MINING_SPEED(+4) - the thing that makes Aqua actually work client-side");
    }

    @Test
    void heldSwapPatchedByDefaultAppliesImmediately() {
        Recorded r = new Recorded(); // attributeSwapping unset -> false (patched)
        r.player.getInventory().setItemStack(3, efficiencyPick());
        EventDispatcher.call(new PlayerChangeHeldSlotEvent(r.player, (byte) 0, (byte) 3));
        assertTrue(hasMining(r.player), "patched (default): the held push applies immediately on the slot change, closing the swap window");
    }

    @Test
    void heldSwapLagsWhenSwappingPermitted() {
        Recorded r = new Recorded();
        r.permitAttributeSwapping();
        r.player.getInventory().setItemStack(3, efficiencyPick());
        EventDispatcher.call(new PlayerChangeHeldSlotEvent(r.player, (byte) 0, (byte) 3));
        assertFalse(hasMining(r.player), "swapping permitted: the held push lags the slot change (the exploit window stays open)");
    }
}
