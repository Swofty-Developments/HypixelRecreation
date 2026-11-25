package io.github.term4.polyp.mechanics.cooldown;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickSystem;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link CooldownSystem#tryUse} arms on use (sending the client overlay), gates while active, re-allows at expiry. */
class CooldownSystemTest extends HeadlessServerTest {

    private static CooldownSystem system;

    @BeforeAll
    static void installCooldowns() {
        system = CooldownSystem.install(polyp);
    }

    @Test
    void gatesArmsAndExpires() {
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.COOLDOWNS, CooldownConfig.builder().cooldown(Material.ENDER_PEARL, 20).build())
                .build());
        FakePlayer p = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), "Cooldown");
        setCombatTick(inst, 0);

        p.sent.clear();
        assertTrue(system.tryUse(p.player, Material.ENDER_PEARL), "first use allowed");
        assertTrue(p.sent.stream().anyMatch(x -> x instanceof SetCooldownPacket c
                && c.cooldownGroup().equals("minecraft:ender_pearl") && c.cooldownTicks() == 20), "overlay sent");
        assertTrue(system.isOnCooldown(p.player, Material.ENDER_PEARL));
        assertFalse(system.tryUse(p.player, Material.ENDER_PEARL), "spam use gated");
        assertTrue(system.tryUse(p.player, Material.SNOWBALL), "unconfigured material never gated");

        setCombatTick(inst, 20);
        assertFalse(system.isOnCooldown(p.player, Material.ENDER_PEARL));
        assertTrue(system.tryUse(p.player, Material.ENDER_PEARL), "re-allowed at expiry");
    }

    /** Each instance counts its own ticks, so the move alone strands the expiry - no world bridge involved. */
    @Test
    void anInstanceChangeClearsTheCooldownItWouldStrand() {
        Instance from = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.COOLDOWNS, CooldownConfig.builder().cooldown(Material.ENDER_PEARL, 20).build())
                .build());
        Instance to = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.COOLDOWNS, CooldownConfig.builder().cooldown(Material.ENDER_PEARL, 20).build())
                .build());
        FakePlayer p = FakePlayer.connect(from, new Pos(16.5, 64, 16.5), "InstanceHop");
        try {
            setCombatTick(from, 10_000);
            assertTrue(system.tryUse(p.player, Material.ENDER_PEARL));
            assertTrue(system.isOnCooldown(p.player, Material.ENDER_PEARL));

            p.player.setInstance(to, new Pos(16.5, 64, 16.5)).join();

            assertFalse(system.isOnCooldown(p.player, Material.ENDER_PEARL),
                    "the instance swap alone must clear it - the new clock never reaches the old stamp");
        } finally {
            p.player.remove();
        }
    }

    /** Against a clock counting from zero the old expiry is unreachable, and nothing else clears it. */
    @Test
    void aClockChangeDropsCooldownsInsteadOfStrandingThem() {
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.COOLDOWNS, CooldownConfig.builder().cooldown(Material.ENDER_PEARL, 20).build())
                .build());
        FakePlayer p = FakePlayer.connect(inst, new Pos(12.5, 64, 12.5), "ClockHop");
        try {
            setCombatTick(inst, 10_000); // armed deep into the old clock
            assertTrue(system.tryUse(p.player, Material.ENDER_PEARL));
            assertTrue(system.isOnCooldown(p.player, Material.ENDER_PEARL));

            setCombatTick(inst, 0); // the new clock counts from zero
            assertTrue(system.isOnCooldown(p.player, Material.ENDER_PEARL), "stale stamp still reads as active");

            TickSystem.clockChanged(p.player);
            assertFalse(system.isOnCooldown(p.player, Material.ENDER_PEARL),
                    "a cooldown never survives a clock change - a stranded one has no way to expire");
        } finally {
            p.player.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private static void setCombatTick(Instance inst, long value) {
        try {
            Field f = TickSystem.class.getDeclaredField("CLOCKS");
            f.setAccessible(true);
            ((Map<Instance, AtomicLong>) f.get(null)).computeIfAbsent(inst, k -> new AtomicLong()).set(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
