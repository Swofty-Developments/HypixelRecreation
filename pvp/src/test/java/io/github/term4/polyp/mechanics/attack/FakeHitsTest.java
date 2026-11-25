package io.github.term4.polyp.mechanics.attack;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.knockback.KnockbackSnapshot;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.platform.compatibility.Compat18;
import io.github.term4.polyp.platform.player.OptimizedPlayer;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import io.github.term4.polyp.util.tick.TickSystem;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FakeHits} swing fill on its compat (bare-fist) configuration. Geometry: the attacker's ray runs parallel to
 * the victim's box at {@code x = 8.85} - real box ends at 8.8, the 0.1-padded one at 8.9 - so a landed hit proves the
 * fill covers exactly the gap the {@code attack_range} stamp can't (no item to ride).
 */
class FakeHitsTest extends HeadlessServerTest {

    @BeforeAll
    static void installAttack() {
        AttackSystem.install(polyp);
    }

    private record Duo(FakePlayer attacker, Player victim, Instance inst) {}

    /** Attacker 2.7 blocks from the victim's front face, sight parallel to +Z at {@code x = attackerX}; victim box x 8.2..8.8. */
    private static Duo duo(double attackerX, String tag) {
        Instance inst = flatInstance(MechanicsProfile.builder().set(MechanicsKeys.ATTACK, Vanilla18.attack()).build());
        FakePlayer attacker = FakePlayer.connect(inst, new Pos(attackerX, 64, 8.5, 0f, 0f), "Fist" + tag);
        Player victim = FakePlayer.connect(inst, new Pos(8.5, 64, 11.5), "Victim" + tag).player;
        victim.setHealth(20f);
        return new Duo(attacker, victim, inst);
    }

    /** Records a hit (i-frame window 0..10), then parks the combat clock at {@code tick}. */
    private static void seedCombat(Duo d, long tick) {
        setCombatTick(d.inst(), 0);
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(d.attacker().player, d.victim(), null));
        d.victim().setHealth(20f);
        setCombatTick(d.inst(), tick);
    }

    private static void swingAndLook(Duo d) {
        EventDispatcher.call(new PlayerHandAnimationEvent(d.attacker().player, PlayerHand.MAIN));
        EventDispatcher.call(new PlayerMoveEvent(d.attacker().player, d.attacker().player.getPosition(), true));
    }

    /** Swings at combat tick 10 (the i-frame window edge). */
    private static void seedAndSwing(Duo d) {
        seedCombat(d, 10);
        swingAndLook(d);
    }

    @Test
    void bareFistSwingFillsMarginGraze() {
        Duo d = duo(8.85, "A"); // outside the real box (8.8), inside the padded one (8.9)
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedAndSwing(d);
        assertTrue(d.victim().getHealth() < 20f, "the margin graze lands as a real attack");
    }

    /** A victim past its last hit's i-frame window only fills on the head band: a side graze at eye height stays a miss. */
    @Test
    void staleTargetSideGrazeDoesNotFill() {
        Duo d = duo(8.85, "K");
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedCombat(d, 40); // stale: well past the hit's i-frame window (0..10)
        swingAndLook(d);
        assertEquals(20f, d.victim().getHealth(), "out of recent combat, the body-side expansion no longer fills");
    }

    /** ... but a ray entering ABOVE the real box top (the head band of the 0.1 expansion) still fills. */
    @Test
    void staleTargetTopGrazeFills() {
        Duo d = duo(8.5, "L"); // straight on; the ray is aimed up into the head band by the move look below
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedCombat(d, 40);
        EventDispatcher.call(new PlayerHandAnimationEvent(d.attacker().player, PlayerHand.MAIN));
        // eye 65.62 aiming to enter the padded front face (z=11.1, 2.6 away) at y=65.85 - above the real top 65.8
        float pitch = (float) -Math.toDegrees(Math.atan2(0.23, 2.6));
        EventDispatcher.call(new PlayerMoveEvent(d.attacker().player, new Pos(8.5, 64, 8.5, 0f, pitch), true));
        assertTrue(d.victim().getHealth() < 20f, "the head band of the expansion fills even out of recent combat");
    }

    /** Regression: a preset's WINDOWED rule must not shadow the windowless compat rule - both are live layers. */
    @Test
    void presetWindowedRuleDoesNotShadowTheCompatBareFistFill() {
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ATTACK, Vanilla18.attack().toBuilder().fakeHits(FakeHitConfig.ofReach(3.0)).build())
                .build());
        FakePlayer attacker = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5, 0f, 0f), "FistI");
        Player victim = FakePlayer.connect(inst, new Pos(8.5, 64, 11.5), "VictimI").player;
        ((OptimizedPlayer) attacker.player).compat().apply(Compat18.config());
        setCombatTick(inst, 0);
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker.player, victim, null));
        victim.setHealth(20f);
        setCombatTick(inst, 40); // far outside the preset window - only the compat layer can grant this head-band graze
        EventDispatcher.call(new PlayerHandAnimationEvent(attacker.player, PlayerHand.MAIN));
        float pitch = (float) -Math.toDegrees(Math.atan2(0.23, 2.6));
        EventDispatcher.call(new PlayerMoveEvent(attacker.player, new Pos(8.5, 64, 8.5, 0f, pitch), true));
        assertTrue(victim.getHealth() < 20f, "the bare-fist pick emulation fills even under a windowed preset rule");
    }

    /** A windowed (preset) rule keeps the MineMen combo gating: the same swing outside the window stays a miss. */
    @Test
    void windowedRuleStillRequiresTheWindow() {
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ATTACK, Vanilla18.attack().toBuilder().fakeHits(FakeHitConfig.ofReach(3.0)).build())
                .build());
        FakePlayer attacker = FakePlayer.connect(inst, new Pos(8.85, 64, 8.5, 0f, 0f), "FistH");
        Player victim = FakePlayer.connect(inst, new Pos(8.5, 64, 11.5), "VictimH").player;
        setCombatTick(inst, 0);
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker.player, victim, null));
        victim.setHealth(20f);
        setCombatTick(inst, 37);
        EventDispatcher.call(new PlayerHandAnimationEvent(attacker.player, PlayerHand.MAIN));
        EventDispatcher.call(new PlayerMoveEvent(attacker.player, attacker.player.getPosition(), true));
        assertEquals(20f, victim.getHealth(), "a windowed rule only fills at the i-frame boundary");
    }

    @Test
    void heldItemSwingIsNotFilledOnTheCompatRule() {
        Duo d = duo(8.85, "B");
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        d.attacker().player.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD)); // covered by the attack_range stamp instead
        seedAndSwing(d);
        assertEquals(20f, d.victim().getHealth(), "a held-item swing is the stamp's job, never ray-filled");
    }

    @Test
    void rayOffThePaddedBoxDoesNotFill() {
        Duo d = duo(8.95, "C"); // outside even the padded box (8.9)
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedAndSwing(d);
        assertEquals(20f, d.victim().getHealth(), "a swing whose ray misses the padded box stays a miss");
    }

    @Test
    void unarmedClientIsNeverFilled() {
        Duo d = duo(8.85, "D"); // no compat policy, no scope fakeHits -> combat never recorded
        seedAndSwing(d);
        assertEquals(20f, d.victim().getHealth());
    }

    @Test
    void dropSwingDoesNotArm() {
        Duo d = duo(8.85, "E");
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedCombat(d, 10);
        EventDispatcher.call(new ItemDropEvent(d.attacker().player, ItemStack.of(Material.SNOWBALL)));
        swingAndLook(d);
        assertEquals(20f, d.victim().getHealth(), "a drop's swing is never an attack");
    }

    /** The creative-instabreak hole: no dig events fire at all (straight to breakBlock), so the DIGGING tag never gates the mining swing. */
    @Test
    void instantBreakSwingDoesNotArm() {
        Duo d = duo(8.85, "J");
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedCombat(d, 10);
        EventDispatcher.call(new PlayerBlockBreakEvent(d.attacker().player, d.inst(), Block.STONE, Block.AIR,
                new BlockVec(8, 63, 9), BlockFace.TOP));
        swingAndLook(d);
        assertEquals(20f, d.victim().getHealth(), "a mining swing is never an attack on a modern client");
    }

    @Test
    void useItemSwingDoesNotArm() {
        Duo d = duo(8.85, "F");
        ((OptimizedPlayer) d.attacker().player).compat().apply(Compat18.config());
        seedCombat(d, 10);
        EventDispatcher.call(new PlayerUseItemEvent(d.attacker().player, PlayerHand.MAIN, ItemStack.of(Material.SNOWBALL), 0));
        swingAndLook(d);
        assertEquals(20f, d.victim().getHealth(), "a right-click use's swing is never an attack");
    }

    /** A fake crit's sparkle must ALSO reach the attacker: its client never registered a hit, so it predicts nothing. */
    @Test
    void fakeCritSparkleIncludesTheAttacker() {
        Instance inst = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.ATTACK, Vanilla18.attack().toBuilder().criticalRule(e -> true).build())
                .set(MechanicsKeys.FX, Fx.vanilla18())
                .build());
        FakePlayer attacker = FakePlayer.connect(inst, new Pos(8.5, 64, 8.5, 0f, 0f), "CritFake");
        Player victim = FakePlayer.connect(inst, new Pos(8.5, 64, 10.5), "CritVictim").player;

        setCombatTick(inst, 0);
        victim.setHealth(20f);
        attacker.sent.clear();
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker.player, victim, null)); // a real client hit
        assertFalse(critSeen(attacker.sent, victim), "a real crit is excluded from the attacker (its client predicts its own)");

        setCombatTick(inst, 10);
        victim.setHealth(20f);
        attacker.sent.clear();
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker.player, victim, null,
                attacker.player.getPosition())); // a swing-filled hit (aim set)
        assertTrue(critSeen(attacker.sent, victim), "a fake crit's sparkle is sent to the attacker too");
    }

    private static boolean critSeen(List<SendablePacket> sent, Player victim) {
        return sent.stream().anyMatch(p -> p instanceof EntityAnimationPacket a
                && a.entityId() == victim.getEntityId()
                && a.animation() == EntityAnimationPacket.Animation.CRITICAL_EFFECT);
    }

    /** The fill's knockback follows the intersecting ray: an explicit snapshot direction outranks the source's look. */
    @Test
    void explicitKnockbackDirectionOutranksSourceLook() {
        LivingEntity attacker = zombie(new Pos(100, 64, 100));      // yaw 0 -> looks +Z
        LivingEntity victim = zombie(new Pos(100, 64, 101));        // straight +Z of the attacker
        // extra=1: the extra component is pure look (vanilla18 extraYawWeight 1), so the override is directly observable
        services.knockback().apply(new KnockbackSnapshot(victim, true, attacker, null, new Vec(1, 0, 0), null, 1));
        Vec v = victim.getVelocity();
        assertTrue(v.x() > 0.5, "the extra component follows the explicit +X direction, not the source's +Z look; got " + v);
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
