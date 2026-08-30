package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.source.Source;
import io.github.term4.polyp.mechanics.attribute.source.EntitySource;
import io.github.term4.polyp.mechanics.attribute.source.ItemSource;
import io.github.term4.polyp.mechanics.attribute.source.ArmorSource;
import io.github.term4.polyp.mechanics.attribute.source.HeldSource;
import io.github.term4.polyp.mechanics.attribute.source.SourceRegistry;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.AttributeContext;
import io.github.term4.polyp.mechanics.attribute.AttributeConfigResolver.ResolvedAttributeConfig;
import io.github.term4.polyp.mechanics.attribute.combat.HitContext;
import io.github.term4.polyp.mechanics.attribute.combat.OnHit;
import io.github.term4.polyp.mechanics.attribute.defense.Bypass;
import io.github.term4.polyp.mechanics.attribute.defense.MitigationRequest;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import io.github.term4.polyp.item.Enchants;
import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attribute/enchant/potion system: a {@link SourceRegistry} plus a scan of active effects and the in-context item's
 * enchants into active sources, read via {@link #context}.
 */
public final class AttributeSystem implements MechanicsModule {

    /** Identity for per-module TPS scaling. */
    public static final Key KEY = Key.key("polyp:attribute");

    static final Key RESISTANCE_KEY = Key.key("minecraft:resistance");
    /** The armor stage's identity for targeted {@link Bypass#attribute} bypass. */
    static final Key ARMOR_ATTRIBUTE_KEY = Key.key("minecraft:armor");

    private final Polyp polyp;
    private final SourceRegistry registry = new SourceRegistry();
    private final AttributeConfig config;
    private final EventNode<@NotNull EntityEvent> node;
    /** Re-entry guard: the re-added potion fires the add event again. */
    private final ThreadLocal<Boolean> rescaling = ThreadLocal.withInitial(() -> Boolean.FALSE);
    /** The refresh's incoming potion, held across the re-drive: mutes the behavior half of {@link #onPotion} and
     *  marks the internal removal as the swap's own. */
    private final ThreadLocal<Potion> replacing = new ThreadLocal<>();
    /** Per-entity {@code armorEnchantKey -> worn level}, driving {@link ArmorSource} behavior transitions + ticking. */
    private static final Tag<Map<Key, Integer>> WORN_ARMOR = Tag.Transient("polyp:worn-armor-sources");
    /** Last-reconciled {@code [helmet, chest, legs, boots, mainhand]}. */
    private static final Tag<ItemStack[]> EQUIP_SNAPSHOT = Tag.Transient("polyp:equip-snapshot");

    public AttributeSystem(Polyp polyp, @Nullable AttributeConfig config) {
        this.polyp = polyp;
        this.config = config != null ? config : AttributeConfig.builder().build();
        this.node = EventNode.type("polyp:attributes", EventFilter.ENTITY);
        for (Source source : this.config.sources()) registry.register(source);
        node.addListener(EntityPotionAddEvent.class, this::onPotionAdd);
        node.addListener(EntityPotionRemoveEvent.class, this::onPotionRemove);
        node.addListener(EntityTickEvent.class, this::onEntityTick);
        node.addListener(EntityEquipEvent.class, this::onEntityEquip);
        node.addListener(PlayerChangeHeldSlotEvent.class, this::onHeldSlotChange);
        // no death listener: DamageSystem clears effects, which reaches us as potion-remove
    }

    private void onEntityTick(EntityTickEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le)) return;
        tickEffects(le);
        tickArmor(le);
        if (le instanceof Player p) tickEquipment(p);
    }

    /** Mobs only: a synchronous push races the player's use-item prediction, so players ride {@link #tickEquipment}. */
    private void onEntityEquip(EntityEquipEvent e) {
        if (!(e.getEntity() instanceof LivingEntity le) || le instanceof Player) return;
        if (isArmorSlot(e.getSlot())) syncArmor(le, e.getSlot(), e.getEquippedItem());
        else if (e.getSlot() == EquipmentSlot.MAIN_HAND) syncHeld(le, e.getEquippedItem());
    }

    /** A hotbar switch fires no equip event; pushing here closes the swap window (see {@link AttributeConfig#attributeSwapping}). */
    private void onHeldSlotChange(PlayerChangeHeldSlotEvent e) {
        if (!configFor(e.getPlayer()).attributeSwapping()) syncHeld(e.getPlayer(), e.getItemInNewSlot());
    }

    /** Vanilla's {@code duration % interval} cadence. */
    private void tickEffects(LivingEntity entity) {
        List<TimedPotion> active = entity.getActiveEffects();
        if (active.isEmpty()) return;
        long now = entity.getAliveTicks();
        for (TimedPotion tp : active) {
            EntitySource source = registry.entitySource(tp.potion().effect().key());
            if (source == null) continue;
            int level = tp.potion().amplifier() + 1;
            int interval = source.behavior().tickInterval(level);
            if (interval <= 0) continue;
            int scaledInterval = Math.max(1, TickScaler.duration(entity, interval, KEY));
            int remaining = tp.potion().duration() - (int) (now - tp.startingTicks());
            if (remaining > 0 && remaining % scaledInterval == 0) source.behavior().onTick(polyp.services(), entity, level);
        }
    }

    /**
     * Minestom decrements potion duration at the live TPS, so at high TPS an effect expires too early in real time. Re-add
     * it at a TPS-scaled duration (identity at 20); the re-add re-fires this event, guarded by {@link #rescaling}.
     */
    private void onPotionAdd(EntityPotionAddEvent e) {
        Potion p = e.getPotion();
        if (!rescaling.get() && p.duration() != Potion.INFINITE_DURATION) {
            int scaled = TickScaler.duration(e.getEntity(), p.duration(), KEY);
            if (scaled != p.duration()) {
                e.setCancelled(true);
                rescaling.set(Boolean.TRUE);
                try { e.getEntity().addEffect(new Potion(p.effect(), p.amplifier(), scaled, p.flags())); }
                finally { rescaling.set(Boolean.FALSE); }
                return;
            }
        }
        // A refresh reaches us BEFORE the removeEffect Minestom does inside addEffect, so the removal that
        // follows would strip the push below. Levels usually differ and modifierId keeps them apart, but a
        // same-level refresh (speed II over speed II) collides: re-drive the add holding the incoming potion,
        // so onPotionRemove can tell that removal from a real expiry.
        if (replacing.get() == null && e.getEntity().hasEffect(p.effect())) {
            e.setCancelled(true);
            // behavior sees vanilla's combine via onRefresh (default remove-then-add): absorption's clamped
            // remove makes a re-eaten gapple a refill, not a stack. The re-driven add is modifiers-only.
            behaviorRefresh(e.getEntity(), p);
            replacing.set(p);
            try { e.getEntity().addEffect(p); }
            finally { replacing.remove(); }
            return;
        }
        onPotion(e.getEntity(), p, true);
    }

    private void behaviorRefresh(Entity entity, Potion incoming) {
        if (!(entity instanceof LivingEntity living)) return;
        EntitySource source = registry.entitySource(incoming.effect().key());
        if (source == null) return;
        for (TimedPotion tp : living.getActiveEffects()) {
            if (tp.potion().effect() == incoming.effect()) {
                source.behavior().onRefresh(living, tp.potion().amplifier() + 1, incoming.amplifier() + 1);
                return;
            }
        }
    }

    /** The swap's own removal (same effect while {@code replacing} is set): its behavior pair already ran, so only
     *  strip a different OLD level's modifier push. Anything else is a real expiry. */
    private void onPotionRemove(EntityPotionRemoveEvent e) {
        Potion incoming = replacing.get();
        Potion gone = e.getPotion();
        if (incoming != null && incoming.effect() == gone.effect()) {
            if (incoming.amplifier() != gone.amplifier() && e.getEntity() instanceof LivingEntity living) {
                EntitySource source = registry.entitySource(gone.effect().key());
                if (source != null) syncModifiers(living, source, gone.amplifier() + 1, false);
            }
            return;
        }
        onPotion(e.getEntity(), gone, false);
    }

    private void onPotion(Entity entity, Potion potion, boolean added) {
        if (!(entity instanceof LivingEntity living)) return;
        EntitySource source = registry.entitySource(potion.effect().key());
        if (source == null) return;
        int level = potion.amplifier() + 1;
        syncModifiers(living, source, level, added);
        if (replacing.get() != null) return; // a refresh's re-drive: the behavior already ran via onRefresh
        if (added) source.behavior().onApply(living, level);
        else source.behavior().onRemove(living, level);
    }

    private void syncModifiers(LivingEntity entity, Source source, int level, boolean add) {
        AttributeContext ctx = context(entity, null);
        pushModifiers(entity, ctx.config().tuningFor(source.key()).apply(ctx, source.modifiers(level)), modifierId(source, level), add);
    }

    /** Custom attributes (null handle) are pull-only via AttributeContext, never pushed. */
    private static void pushModifiers(LivingEntity entity, List<Source.Mod> mods, Key id, boolean add) {
        for (Source.Mod mod : mods) {
            var handle = mod.attribute().handle();
            if (handle == null) continue;
            AttributeInstance instance = entity.getAttribute(handle);
            if (instance == null) continue;
            instance.removeModifier(id);
            if (add) instance.addModifier(new AttributeModifier(id, mod.amount(), mod.operation()));
        }
    }

    /**
     * The level in the id keeps a higher effect's push from being stripped when a replaced lower one's removal fires
     * (Minestom replaces via add-then-remove, same key).
     */
    private static Key modifierId(Source source, int level) {
        return Key.key("polyp", flatKey(source) + "." + level);
    }

    private static String flatKey(Source source) {
        return source.key().asString().replace(':', '.');
    }

    /** Full recompute off current armor, not an add/remove diff, so order-independent. */
    private void syncArmor(LivingEntity entity, EquipmentSlot changedSlot, ItemStack changedItem) {
        Map<Key, Integer> worn = wornArmorMap(entity);
        AttributeContext ctx = context(entity, null);
        for (ArmorSource source : registry.armorSources()) {
            int level = wornLevel(entity, source.key(), changedSlot, changedItem);
            applyEquipModifiers(ctx, entity, source, level, armorModifierId(source));
            int was = worn.getOrDefault(source.key(), 0);
            if (was == 0 && level > 0) source.behavior().onApply(entity, level);
            else if (was > 0 && level == 0) source.behavior().onRemove(entity, was);
            if (level > 0) worn.put(source.key(), level); else worn.remove(source.key());
        }
    }

    /** Sets ({@code level > 0}) or clears a worn/held source's pushed modifiers under {@code id}. */
    private static void applyEquipModifiers(AttributeContext ctx, LivingEntity entity, Source source, int level, Key id) {
        pushModifiers(entity, ctx.config().tuningFor(source.key()).apply(ctx, source.modifiers(Math.max(level, 1), ctx)), id, level > 0);
    }

    /** Pushed, not pulled: mining is client-computed off the holder's attributes. */
    private void syncHeld(LivingEntity entity, ItemStack held) {
        AttributeContext ctx = context(entity, null);
        for (HeldSource source : registry.heldSources()) {
            applyEquipModifiers(ctx, entity, source, Enchants.level(held, source.key()), heldModifierId(source));
        }
    }

    /** Level-independent: held sources are reconciled by recompute, not add/remove events. */
    private static Key heldModifierId(HeldSource source) {
        return Key.key("polyp", "held." + flatKey(source));
    }

    private void tickArmor(LivingEntity entity) {
        Map<Key, Integer> worn = entity.getTag(WORN_ARMOR);
        if (worn == null || worn.isEmpty()) return;
        long now = entity.getAliveTicks();
        for (Map.Entry<Key, Integer> e : worn.entrySet()) {
            ArmorSource source = registry.armorSource(e.getKey());
            if (source == null) continue;
            int interval = source.behavior().tickInterval(e.getValue());
            if (interval <= 0) continue;
            int scaled = Math.max(1, TickScaler.duration(entity, interval, KEY));
            if (now % scaled == 0) source.behavior().onTick(polyp.services(), entity, e.getValue());
        }
    }

    /**
     * Vanilla {@code detectEquipmentUpdates}: reconciles off the settled equipment on the tick, so a use-item client
     * prediction can't drop the push (as it would a synchronous equip-event one).
     */
    private void tickEquipment(Player player) {
        ItemStack[] cur = {
                player.getEquipment(EquipmentSlot.HELMET), player.getEquipment(EquipmentSlot.CHESTPLATE),
                player.getEquipment(EquipmentSlot.LEGGINGS), player.getEquipment(EquipmentSlot.BOOTS),
                player.getItemInMainHand()
        };
        ItemStack[] prev = player.getTag(EQUIP_SNAPSHOT);
        if (prev != null && Arrays.equals(prev, cur)) return;
        player.setTag(EQUIP_SNAPSHOT, cur);
        syncArmor(player, null, null); // null slot -> wornLevel reads every slot from getEquipment
        syncHeld(player, player.getItemInMainHand());
    }

    /**
     * Highest level of {@code enchantKey} across the four armor pieces. A non-null {@code changedSlot} reads
     * {@code changedItem} for that slot: the equip event fires before Minestom commits it.
     */
    private static int wornLevel(LivingEntity entity, Key enchantKey, EquipmentSlot changedSlot, ItemStack changedItem) {
        int max = 0;
        for (EquipmentSlot slot : EquipmentSlot.armors()) {
            ItemStack item = slot == changedSlot ? changedItem : entity.getEquipment(slot);
            int lvl = Enchants.level(item, enchantKey);
            if (lvl > max) max = lvl;
        }
        return max;
    }

    private static boolean isArmorSlot(EquipmentSlot s) {
        return s == EquipmentSlot.HELMET || s == EquipmentSlot.CHESTPLATE || s == EquipmentSlot.LEGGINGS || s == EquipmentSlot.BOOTS;
    }

    private static Map<Key, Integer> wornArmorMap(LivingEntity e) {
        Map<Key, Integer> m = e.getTag(WORN_ARMOR);
        if (m == null) { m = new HashMap<>(); e.setTag(WORN_ARMOR, m); }
        return m;
    }

    /** Level-independent: armor is reconciled by recompute, not add/remove events. */
    private static Key armorModifierId(ArmorSource source) {
        return Key.key("polyp", "armor." + flatKey(source));
    }

    /**
     * Fires the {@link OnHit} side effects of {@code item}'s enchants; called by the damage system after a landed hit.
     * Knockback-domain enchants are not {@code OnHit} - they feed the KB computation.
     */
    public void dispatchWeaponOnHit(LivingEntity attacker, LivingEntity victim, ItemStack item) {
        if (item == null || item.isAir()) return;
        EnchantmentList enchants = item.get(DataComponents.ENCHANTMENTS);
        if (enchants == null) return;
        for (Map.Entry<RegistryKey<Enchantment>, Integer> e : enchants.enchantments().entrySet()) {
            if (e.getValue() <= 0) continue;
            ItemSource s = registry.itemSource(e.getKey().key());
            if (s instanceof OnHit oh) {
                oh.onHit(new HitContext(attacker, victim, e.getValue(), item, polyp.services()));
            }
        }
    }

    public AttributeSystem register(Source source) {
        registry.register(source);
        return this;
    }

    /** The scoped profile, else the install config. */
    public AttributeConfig configFor(@Nullable Entity entity) {
        return polyp.profiles().resolveOr(entity, MechanicsKeys.ATTRIBUTES, config);
    }

    public AttributeContext context(LivingEntity entity, @Nullable ItemStack item) {
        return AttributeContext.of(entity, item, configFor(entity), polyp.services());
    }

    public ResolvedAttributeConfig resolveConfig(AttributeContext ctx) {
        return AttributeConfigResolver.resolve(ctx.config(), ctx);
    }

    /**
     * Defense mitigation, vanilla order: armor → resistance → EPF/protection. Absorption is Minestom's, applied later by
     * {@code living.damage()}. Each stage self-gates on the {@link MitigationRequest} bypass and the victim's config.
     */
    public float mitigate(LivingEntity victim, float damage, MitigationRequest req) {
        if (damage <= 0) return damage;
        AttributeConfig cfg = configFor(victim);
        return MitigationPipeline.run(cfg.mitigationStages,
                new MitigationPipeline.State(this, victim, req, cfg, damage));
    }

    /** {@code 0} = absent. */
    int effectLevel(LivingEntity victim, Key key) {
        return context(victim, null).effectLevel(key);
    }

    /** Source-contributed modifiers folded onto Minestom's attribute value; the caller decides how to apply it. */
    public double knockbackResistance(LivingEntity living) {
        double base = living.getAttributeValue(net.minestom.server.entity.attribute.Attribute.KNOCKBACK_RESISTANCE);
        return context(living, null).value(Attribute.KNOCKBACK_RESISTANCE, base);
    }

    /** Total immunity: DamageSystem blocks at hit entry (vanilla's {@code return false}), not a mitigation stage. */
    public boolean fireResistant(LivingEntity living) {
        return living.hasEffect(PotionEffect.FIRE_RESISTANCE);
    }

    /** Registered matches of the entity's active effects plus the in-context {@code item}'s enchants. */
    public List<Active> activeSources(LivingEntity entity, @Nullable ItemStack item) {
        List<Active> out = new ArrayList<>();
        for (TimedPotion tp : entity.getActiveEffects()) {
            EntitySource s = registry.entitySource(tp.potion().effect().key());
            if (s != null) out.add(new Active(s, tp.potion().amplifier() + 1));
        }
        if (item != null && !item.isAir()) {
            EnchantmentList enchants = item.get(DataComponents.ENCHANTMENTS);
            if (enchants != null) {
                for (Map.Entry<RegistryKey<Enchantment>, Integer> e : enchants.enchantments().entrySet()) {
                    ItemSource s = registry.itemSource(e.getKey().key());
                    if (s != null && e.getValue() > 0) out.add(new Active(s, e.getValue()));
                }
            }
        }
        return out;
    }

    public SourceRegistry registry() { return registry; }
    public AttributeConfig config() { return config; }
    public EventNode<@NotNull EntityEvent> node() { return node; }

    /** {@code level} is 1-based. */
    public record Active(Source source, int level) {}

    /**
     * Installs from the GLOBAL profile's {@link AttributeConfig}, registering its {@code sources} up front - set the
     * profile before installing, or the system is inert.
     */
    public static AttributeSystem install(Polyp polyp) {
        return install(polyp, polyp.profiles().resolve(null, MechanicsKeys.ATTRIBUTES));
    }

    public static AttributeSystem install(Polyp polyp, @Nullable AttributeConfig config) {
        AttributeSystem system = new AttributeSystem(polyp, config);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }
}
