package io.github.term4.polyp.testsupport;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.presets.vanilla18.Attributes;
import io.github.term4.polyp.presets.vanilla18.Damage;
import io.github.term4.polyp.presets.vanilla18.Items;
import io.github.term4.polyp.presets.vanilla18.Knockback;
import io.github.term4.polyp.mechanics.attribute.AttributeSystem;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import io.github.term4.polyp.mechanics.knockback.KnockbackSystem;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.InstanceContainer;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.registry.RegistryKey;
import org.junit.jupiter.api.BeforeAll;

/**
 * Headless harness: one {@link MinecraftServer} process (registries + dispatcher, no socket) per JVM, plus a
 * loaded flat instance. Modules reset per class - the vanilla-1.8 base trio is reinstalled and each class adds
 * its own. Nothing ticks - the calculators under test are pure functions of entity/config state.
 * See docs/attributes-design.md (step 0 harness).
 */
public abstract class HeadlessServerTest {

    protected static Polyp polyp;
    protected static Services services;
    protected static InstanceContainer instance;

    @BeforeAll
    static void boot() {
        if (polyp == null) { // one server per JVM, shared by every subclass
            MinecraftServer.init();
            // entity registration/ticking partition threads (as EnvImpl does); lets setInstance(...).join() complete
            MinecraftServer.process().dispatcher().start();

            polyp = Polyp.getInstance();
            polyp.init();
            services = polyp.services();

            instance = MinecraftServer.getInstanceManager().createInstanceContainer();
            instance.setGenerator(unit -> unit.modifier().fillHeight(0, 64, Block.STONE));
            instance.loadChunk(0, 0).join();
            // item stats resolve from the profile: scope them here so a test swapping the GLOBAL profile doesn't wipe them
            polyp.profiles().setInstance(instance, MechanicsProfile.builder().set(MechanicsKeys.ITEMS, Items.registry()).build());
        }
        // fresh module set per class (install throws on duplicates): the vanilla-1.8 base trio plus whatever the class installs
        polyp.unregisterAll();
        DamageSystem.install(polyp, Damage.config());
        KnockbackSystem.install(polyp, Knockback.melee());
        AttributeSystem.install(polyp, Attributes.config());
    }

    /** A fresh loaded stone-floor instance, with {@code profile} scoped onto it when non-null. */
    protected static InstanceContainer flatInstance(MechanicsProfile profile) {
        InstanceContainer inst = MinecraftServer.getInstanceManager().createInstanceContainer();
        inst.setGenerator(unit -> unit.modifier().fillHeight(0, 64, Block.STONE));
        inst.loadChunk(0, 0).join();
        if (profile != null) polyp.profiles().setInstance(inst, profile);
        return inst;
    }

    /** A stationary zombie at {@code pos}; non-player, so its tracked velocity is zero. */
    protected static LivingEntity zombie(Pos pos) {
        LivingEntity e = new LivingEntity(EntityType.ZOMBIE);
        e.setInstance(instance, pos).join();
        return e;
    }

    /** {@code stack} with {@code enchant} at {@code level} added. */
    protected static ItemStack enchant(ItemStack stack, Key enchant, int level) {
        return stack.with(DataComponents.ENCHANTMENTS,
                new EnchantmentList(RegistryKey.<Enchantment>unsafeOf(enchant), level));
    }

    /** Plain item at {@code level <= 0}. */
    protected static ItemStack enchanted(Material material, Key enchant, int level) {
        ItemStack base = ItemStack.of(material);
        return level <= 0 ? base : enchant(base, enchant, level);
    }

    /** Unbound to any instance: {@link Entity#getPosition()} is the origin, tracked velocity zero. */
    protected static LivingEntity looseZombie() {
        return new LivingEntity(EntityType.ZOMBIE);
    }

    /** Waits (max 2s) for an entity's async {@code setInstance} to land. */
    protected static void awaitSpawn(Entity e) {
        long deadline = System.currentTimeMillis() + 2000;
        while (e.getInstance() == null && System.currentTimeMillis() < deadline) Thread.onSpinWait();
        if (e.getInstance() == null) throw new AssertionError("launch setInstance did not complete");
    }

    /** The client's decode of an LP-encoded velocity (the modern wire grid). */
    protected static Vec lpRoundTrip(Vec v) {
        byte[] wire = NetworkBuffer.makeArray(NetworkBuffer.LP_VECTOR3, v);
        return NetworkBuffer.wrap(wire, 0, wire.length).read(NetworkBuffer.LP_VECTOR3);
    }

    /** Vanilla/Via truncate to shorts; the 1.8 client reads {@code short / 8000.0}. */
    protected static double legacyShortAxis(double bt) {
        return (short) (int) (bt * 8000.0) / 8000.0;
    }
}
