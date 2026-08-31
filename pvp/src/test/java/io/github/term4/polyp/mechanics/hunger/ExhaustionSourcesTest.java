package io.github.term4.polyp.mechanics.hunger;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.mechanics.attack.AttackSnapshot;
import io.github.term4.polyp.mechanics.attack.AttackSystem;
import io.github.term4.polyp.presets.vanilla18.Hunger;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Action exhaustion sources against the pristine-source vanilla values (docs/exhaustion-sources.md). */
class ExhaustionSourcesTest extends HeadlessServerTest {

    @BeforeAll
    static void installHunger() {
        HungerSystem.install(polyp);
        AttackSystem.install(polyp);
    }

    private static Instance legacyInstance() {
        return flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.HUNGER, Hunger.config())
                .set(MechanicsKeys.ATTACK, Vanilla18.attack())
                .build());
    }

    private static Player player(Instance inst, String name) {
        return FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), name).player;
    }

    private static void move(Player p, double dx, double dy, double dz, boolean onGround) {
        EventDispatcher.call(new PlayerMoveEvent(p, p.getPosition().add(dx, dy, dz), onGround));
    }

    private static void ground(Player p, boolean value) throws Exception {
        Field f = Entity.class.getDeclaredField("onGround");
        f.setAccessible(true);
        f.set(p, value);
    }

    @Test
    void walkChargesLegacyButNotModern() {
        Player legacy = player(legacyInstance(), "Walker18");
        move(legacy, 5, 0, 0, true);
        assertEquals(0.05f, HungerSystem.exhaustion(legacy), 1e-6, "1.8 walking: 0.01/m");

        var modern = flatInstance(MechanicsProfile.builder()
                .set(MechanicsKeys.HUNGER, io.github.term4.polyp.presets.vanilla.Hunger.config()).build());
        Player p = player(modern, "WalkerModern");
        move(p, 5, 0, 0, true);
        assertEquals(0f, HungerSystem.exhaustion(p), 1e-6, "modern walking is free (unpriced key)");
    }

    /** Vanilla gates every charge on invulnerable abilities ({@code applyExhaustion}), so creative and
     *  spectator never build exhaustion - their hunger can't drain. */
    @Test
    void invulnerableGameModesNeverCharge() {
        Instance inst = legacyInstance();
        for (GameMode mode : new GameMode[]{GameMode.CREATIVE, GameMode.SPECTATOR}) {
            Player p = player(inst, "Free" + mode.name());
            p.setGameMode(mode);
            p.setSprinting(true);
            move(p, 5, 0, 0, true);
            assertEquals(0f, HungerSystem.exhaustion(p), 1e-6, mode + " never charges exhaustion");
        }
        Player survival = player(inst, "PaysHisWay");
        survival.setGameMode(GameMode.SURVIVAL);
        survival.setSprinting(true);
        move(survival, 5, 0, 0, true);
        assertEquals(0.099999994f * 5, HungerSystem.exhaustion(survival), 1e-6, "survival still charges");
    }

    @Test
    void sprintChargesPerMeter() {
        Player p = player(legacyInstance(), "Sprinter");
        p.setSprinting(true);
        move(p, 5, 0, 0, true);
        assertEquals(0.099999994f * 5, HungerSystem.exhaustion(p), 1e-6);
    }

    @Test
    void jumpIsTheVanillaRisingEdge() throws Exception {
        Player p = player(legacyInstance(), "Jumper");
        ground(p, true);
        move(p, 0, 0.42, 0, false); // grounded + rising + packet airborne
        assertEquals(0.2f, HungerSystem.exhaustion(p), 1e-6, "jump: 0.2");

        ground(p, true);
        p.setSprinting(true);
        move(p, 0.2, 0.42, 0, false);
        assertEquals(0.2f + 0.8f, HungerSystem.exhaustion(p), 1e-6, "sprint-jump: 0.8");
    }

    @Test
    void waterMovementSplitsDiveAndSwim() {
        Instance inst = legacyInstance();
        Player p = player(inst, "Swimmer");
        inst.setBlock(8, 64, 8, Block.WATER);
        inst.setBlock(8, 65, 8, Block.WATER); // eye height ~65.6 -> submerged
        move(p, 3, 0, 0, false);
        assertEquals(0.015f * 3, HungerSystem.exhaustion(p), 1e-5, "dive: 3D meters at 0.015");

        inst.setBlock(8, 65, 8, Block.AIR); // feet wet, eye out -> surface swim
        move(p, 0, 0, 4, false);
        assertEquals(0.015f * 3 + 0.015f * 4, HungerSystem.exhaustion(p), 1e-5, "swim: horizontal meters");
    }

    @Test
    void blockBreakChargesSurvivalOnly() {
        Instance inst = legacyInstance();
        Player p = player(inst, "Miner");
        EventDispatcher.call(new PlayerBlockBreakEvent(p, inst, Block.STONE, Block.AIR, new BlockVec(8, 63, 8), BlockFace.TOP));
        assertEquals(0.025f, HungerSystem.exhaustion(p), 1e-6);

        p.setGameMode(GameMode.CREATIVE);
        EventDispatcher.call(new PlayerBlockBreakEvent(p, inst, Block.STONE, Block.AIR, new BlockVec(8, 63, 8), BlockFace.TOP));
        assertEquals(0.025f, HungerSystem.exhaustion(p), 1e-6, "creative never harvests");
    }

    @Test
    void attackChargesAttackerAndVictimPerVanilla() {
        Instance inst = legacyInstance();
        Player victim = player(inst, "Punched");
        LivingEntity zombie = zombie(new Pos(9.5, 64, 8.5));
        zombie.setInstance(inst, new Pos(9.5, 64, 8.5)).join();
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(zombie, victim, null));
        assertEquals(0.3f, HungerSystem.exhaustion(victim), 1e-6, "victim: the melee type's 0.3");

        Player attacker = player(inst, "Puncher");
        LivingEntity target = zombie(new Pos(7.5, 64, 8.5));
        target.setInstance(inst, new Pos(7.5, 64, 8.5)).join();
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker, target, null));
        assertEquals(0.3f, HungerSystem.exhaustion(attacker), 1e-6, "attacker: 0.3 on the landed hit");
        polyp.module(AttackSystem.class).apply(new AttackSnapshot(attacker, target, null));
        assertEquals(0.3f, HungerSystem.exhaustion(attacker), 1e-6, "an i-frame-eaten hit charges nothing");
    }

    @Test
    void hungerEffectChargesEveryTick() { // rides the attribute catalog's Hunger source (EntityTickEvent-driven)
        Instance inst = legacyInstance();
        Player p = player(inst, "Hungry");
        p.addEffect(new Potion(PotionEffect.HUNGER, (byte) 1, 600)); // amplifier 1 -> quantity 2
        for (int i = 0; i < 4; i++) EventDispatcher.call(new EntityTickEvent(p));
        assertEquals(0.025f * 2 * 4, HungerSystem.exhaustion(p), 1e-6);
    }

    @Test
    void starvationTicksAtFoodZeroDownToTheFloor() {
        Instance inst = legacyInstance();
        Player p = player(inst, "Starving");
        p.setFood(0);
        for (int i = 0; i < 80; i++) EventDispatcher.call(new InstanceTickEvent(inst, 0, 0));
        assertEquals(19f, p.getHealth(), 1e-6, "1.0 starve damage per 80 ticks");

        p.setHealth(1f);
        for (int i = 0; i < 200; i++) EventDispatcher.call(new InstanceTickEvent(inst, 0, 0));
        assertEquals(1f, p.getHealth(), 1e-6, "NORMAL floor: never starves below 1.0");
    }
}
