package io.github.term4.polyp.platform.compatibility;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttackRange;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.utils.Unit;
import io.github.term4.polyp.mechanics.blocking.BlockingSystem;
import io.github.term4.polyp.platform.PacketShapes;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetCursorItemPacket;
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import net.minestom.server.network.packet.server.play.WindowItemsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Per-player cross-version compat state plus the pose/hitbox/eye/attack-box logic it drives. The policy is the held
 * {@link CompatConfig} (swapped whole by {@link #apply} - a profile push is a clean mode SWITCH, never a sticky merge);
 * the rest is operational/identity state. One instance per {@code OptimizedPlayer}, exposed via {@code compat()}.
 */
public final class CompatState {

    /** All-off policy (a modern client left untouched). */
    private static final CompatConfig OFF = CompatConfig.builder().build();

    /** Vanilla throwables whose modern client-side {@code use()} returns {@code SUCCESS} (source-verified), i.e. swings.
     *  A CLIENT-behavior fact, deliberately NOT derived from the projectile system's registered types - the swing happens
     *  client-side whether or not the preset throws the item. */
    private static final Set<Material> THROW_ON_USE = Set.of(Material.SNOWBALL, Material.EGG, Material.ENDER_PEARL,
            Material.WIND_CHARGE, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.EXPERIENCE_BOTTLE);

    private static final Set<Material> SWORDS = Material.values().stream()
            .filter(m -> m.key().value().endsWith("_sword")).collect(Collectors.toUnmodifiableSet());

    /** View-only {@code blocks_attacks}: grants the client the BLOCK use animation; every gameplay field inert (the server's blocking system owns the damage). */
    private static final BlocksAttacks BLOCK_POSE = new BlocksAttacks(0f, 1f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null, null, null);

    private @NotNull CompatConfig policy = OFF;
    private boolean attackCooldownRemoved = false;
    private double savedAttackSpeedBase = 4.0; // vanilla generic.attack_speed default until a removal captures the real base
    private boolean sprintStripped = false;
    private @Nullable EntityPose interceptedPose;
    private @NotNull Set<AnimatiumFeature> nativeFeatures = Set.of();
    private boolean animatiumClient = false;
    private @NotNull Set<AnimatiumFeature> supportedFeatures = Set.of();
    private boolean legacyClient;

    /**
     * Swaps the whole policy; {@code null} = all off (modern). Operational/identity state is left untouched; the
     * {@code attackHitboxMargin} re-send + attack-cooldown attribute are the caller's job (they touch the player).
     */
    public void apply(@Nullable CompatConfig config) {
        this.policy = config != null ? config : OFF;
    }

    public @NotNull Set<EntityPose> disabledPoses() { return policy.disabledPoses != null ? policy.disabledPoses : Set.of(); }
    public boolean restrictMovement() { return on(policy.restrictMovement); }
    /** Server hitbox/eye height stay at 1.8 dimensions regardless of pose (no crouch shrink). */
    public boolean legacyHitbox() { return on(policy.legacyHitbox); }
    /** {@code attack_range.hitbox_margin} for the client's view of held items; {@code null} = untouched. */
    public @Nullable Float attackHitboxMargin() { return policy.attackHitboxMargin; }
    public boolean disableOffhand() { return on(policy.disableOffhand); }
    public boolean restrictSprintSneak() { return on(policy.restrictSprintSneak); }
    public boolean restrictSprintUse() { return on(policy.restrictSprintUse); }
    public boolean restrictSwimSpeed() { return on(policy.restrictSwimSpeed); }
    public double swimFactor() { return policy.swimFactor != null ? policy.swimFactor : 1.25; }
    public double swimVerticalFactor() { return policy.swimVerticalFactor != null ? policy.swimVerticalFactor : 3.0; }
    /** Max blocks from the server eye to a placement's clicked point; {@code null} = unmanaged. */
    public @Nullable Double blockPlaceReach() { return policy.blockPlaceReach; }
    public boolean oldPlacement() { return on(policy.oldPlacement); }

    private static boolean on(@Nullable Boolean v) { return Boolean.TRUE.equals(v); }

    public boolean hookPredictionEscort() { return policy.hookPredictionEscort == null || policy.hookPredictionEscort; }

    /** Tracked so a profile swap restores the saved base only on a real change. */
    public boolean attackCooldownRemoved() { return attackCooldownRemoved; }
    public void setAttackCooldownRemoved(boolean v) { this.attackCooldownRemoved = v; }

    /** The {@code ATTACK_SPEED} base captured before the cooldown removal - restored on switch-off so an app-set base survives. */
    public double savedAttackSpeedBase() { return savedAttackSpeedBase; }
    public void setSavedAttackSpeedBase(double v) { this.savedAttackSpeedBase = v; }

    public boolean anySpeedRestriction() { return restrictSprintSneak() || restrictSprintUse() || restrictSwimSpeed(); }

    /** Whether {@code CompatMovement} forced sprint off and owes a restore (so a combat sprint-reset isn't undone). */
    public boolean sprintStripped() { return sprintStripped; }
    public void setSprintStripped(boolean v) { this.sprintStripped = v; }

    public boolean isPoseDisabled(@NotNull EntityPose pose) { return disabledPoses().contains(pose); }

    /** The Animatium features this client applies natively (empty for non-Animatium clients); the enforcers gate the matching hack off via {@link #handlesNatively}. */
    public void setNativeFeatures(@NotNull Set<AnimatiumFeature> features) { this.nativeFeatures = features; }
    public @NotNull Set<AnimatiumFeature> nativeFeatures() { return nativeFeatures; }

    /** Whether the {@code animatium:info} handshake arrived - distinguishes a feature-less Animatium client from a non-Animatium one (both have empty {@link #nativeFeatures}). */
    public boolean isAnimatiumClient() { return animatiumClient; }
    public void setAnimatiumClient(boolean v) { this.animatiumClient = v; }

    public void setSupportedFeatures(@NotNull Set<AnimatiumFeature> features) { this.supportedFeatures = features; }
    /** Whether the client advertised a decoder for {@code feature} - required before sending a wire-format one like {@link AnimatiumFeature#SHORTS_VELOCITY}. */
    public boolean supports(@NotNull AnimatiumFeature feature) { return supportedFeatures.contains(feature); }

    /** Whether the client applies {@code feature} natively, so the matching server-side hack should be skipped for it. */
    public boolean handlesNatively(@NotNull AnimatiumFeature feature) {
        return nativeFeatures.contains(AnimatiumFeature.ALL) || nativeFeatures.contains(feature);
    }

    /** Confirmed legacy (&le;1.8) client: skips the {@code attack_range} stamp - already native, and it only round-trips through Via as junk NBT. */
    public boolean legacyClient() { return legacyClient; }
    public void setLegacyClient(boolean v) { this.legacyClient = v; }

    public void recordInterceptedPose(@NotNull EntityPose pose) { this.interceptedPose = pose; }

    /** Called once per tick before pose recomputation. */
    public void resetInterceptedPose() { this.interceptedPose = null; }

    /**
     * The pose the CLIENT believes it's in: the disabled pose intercepted this tick (crawl = {@code SWIMMING}), else the
     * authoritative {@code serverPose}. Lets the compat/reach layer read the client's belief while the server stays STANDING.
     */
    public @NotNull EntityPose clientPose(@NotNull EntityPose serverPose) {
        return interceptedPose != null ? interceptedPose : serverPose;
    }

    /**
     * The server-treated eye height (value (b) of the eye model): with {@code legacyHitbox} on, the fixed 1.8 preset
     * (the standing default already matches), so a crouching modern client still spawns/drowns at the 1.8 eye.
     */
    public double eyeHeight(double nativeEye, @NotNull EntityPose pose) {
        return legacyHitbox() && pose == EntityPose.SNEAKING ? ClientEye.LEGACY_SNEAKING : nativeEye;
    }

    /** Whether this client receives the {@code attack_range} stamp (echo counterpart: {@link #sanitizeInboundItem}). Animatium clients take the 1.8 set natively - the stamp would double it. */
    public boolean stampsAttackRange() {
        return policy.attackHitboxMargin != null && !legacyClient && !animatiumClient;
    }

    /** Whether this client gets the throwable reskin (echo counterpart: {@link #sanitizeInboundItem}). Excluded for Animatium: its native 1.8 animations key off the real item. */
    public boolean suppressesThrowSwing() {
        return on(policy.suppressThrowSwing) && !legacyClient && !animatiumClient;
    }

    /** Whether this client's bare-fist swings get the {@code FakeHits} ray fill - the empty-hand half of the attack-box, so it follows the stamp's gating. */
    public boolean fistRayHits() {
        return on(policy.fistRayHits) && stampsAttackRange();
    }

    /** Animatium only RESTYLES an existing block pose, so it needs the stamp like any modern client; 1.8 clients block
     *  natively and the component is junk through Via. */
    public boolean swordBlockingPose() {
        return on(policy.swordBlockingPose) && !legacyClient;
    }

    /** Animatium disables the glide natively too - the strip is harmless doubled, and covers a spoofed handshake. */
    public boolean stripsGlider() {
        return on(policy.disableElytraFlight) && !legacyClient;
    }

    /** Not Animatium-excluded (not one of its features). */
    public boolean stripsUseCooldowns() {
        return on(policy.removeUseCooldowns) && !legacyClient;
    }

    /** Every client version enrolls in the shared no-push team - the OTHER side of a pair predicts its own push. */
    public boolean noEntityPush() {
        return on(policy.disableEntityPush);
    }

    /** The melee reach the attack-box advertises (stamped on items, used by the bare-fist ray); unset = 3 (1.8). */
    public float attackReach() {
        return policy.attackReach != null ? policy.attackReach : 3f;
    }

    /**
     * Everything that decides how {@link #rewriteItems} renders this client's items. The config applier compares it
     * across a policy swap and re-sends the inventory on any change - comparing just the margin would leave stale views
     * when only a reskin/pose/strip knob differs.
     */
    public @NotNull List<Object> itemViewKey() {
        return Arrays.asList(
                stampsAttackRange() ? attackHitboxMargin() : null,
                stampsAttackRange() ? attackReach() : null,
                suppressesThrowSwing(), swordBlockingPose(), stripsGlider(), stripsUseCooldowns());
    }

    /** Whether {@link #rewriteItems} would change anything - lets senders keep the grouped/cached fast path. */
    public boolean rewritesItems() {
        return stampsAttackRange() || suppressesThrowSwing() || swordBlockingPose() || stripsGlider() || stripsUseCooldowns();
    }

    /**
     * Applies this client's view-only item rewrites to an outgoing packet; server items stay clean. Must cover every
     * item-carrying packet or a slot/cursor update (pickup, held swap, drag) reaches the client unrewritten until the
     * next full {@link WindowItemsPacket}.
     */
    public @NotNull SendablePacket rewriteItems(@NotNull SendablePacket packet) {
        if (!rewritesItems()) return packet;
        return switch (PacketShapes.unwrapStateless(packet)) {
            case SetSlotPacket p -> new SetSlotPacket(p.windowId(), p.stateId(), p.slot(), rewrite(p.itemStack()));
            case SetPlayerInventorySlotPacket p -> new SetPlayerInventorySlotPacket(p.slot(), rewrite(p.itemStack()));
            case WindowItemsPacket p -> new WindowItemsPacket(p.windowId(), p.stateId(), p.items().stream().map(this::rewrite).toList(), rewrite(p.carriedItem()));
            case SetCursorItemPacket p -> new SetCursorItemPacket(rewrite(p.itemStack()));
            // another player's held item: a modern client reads the block POSE off blocks_attacks, so without the stamp
            // it never renders anyone else blocking. The other rewrites are the viewer's own first-person concern.
            case EntityEquipmentPacket p when swordBlockingPose() -> new EntityEquipmentPacket(p.entityId(),
                    p.equipments().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> blockPose(e.getValue()))));
            case null, default -> packet;
        };
    }

    /**
     * Inbound counterpart to {@link #rewriteItems}: undoes what an affected client echoes back through creative
     * (client-authoritative slots), so a view-only rewrite never becomes server state.
     */
    public @NotNull ItemStack sanitizeInboundItem(@NotNull ItemStack item) {
        ItemStack out = item;
        if (stampsAttackRange() && out.get(DataComponents.ATTACK_RANGE) != null) out = out.without(DataComponents.ATTACK_RANGE);
        if (suppressesThrowSwing()) out = restoreThrowable(out);
        if (swordBlockingPose() && SWORDS.contains(out.material())) out = out.without(DataComponents.BLOCKS_ATTACKS);
        // an echoed strip must not become a truly component-less server item: re-add the material's own default
        if (stripsGlider() && out.get(DataComponents.GLIDER) == null && ItemStack.of(out.material()).get(DataComponents.GLIDER) != null) {
            out = out.with(DataComponents.GLIDER, Unit.INSTANCE);
        }
        if (stripsUseCooldowns() && out.get(DataComponents.USE_COOLDOWN) == null) {
            var prototype = ItemStack.of(out.material()).get(DataComponents.USE_COOLDOWN);
            if (prototype != null) out = out.with(DataComponents.USE_COOLDOWN, prototype);
        }
        return out;
    }

    private ItemStack rewrite(ItemStack item) {
        if (stampsAttackRange()) item = withAttackRange(item);
        if (suppressesThrowSwing()) item = reskinThrowable(item);
        if (swordBlockingPose()) item = blockPose(item);
        if (stripsGlider() && item.get(DataComponents.GLIDER) != null) item = item.without(DataComponents.GLIDER);
        if (stripsUseCooldowns() && item.get(DataComponents.USE_COOLDOWN) != null) item = item.without(DataComponents.USE_COOLDOWN);
        return item;
    }

    /** Honours the per-item opt-out: stamping one would have the client predict a raise the server then refuses. */
    private ItemStack blockPose(ItemStack item) {
        if (!SWORDS.contains(item.material()) || item.get(DataComponents.BLOCKS_ATTACKS) != null
                || Boolean.FALSE.equals(item.getTag(BlockingSystem.BLOCKABLE))) return item;
        return item.with(DataComponents.BLOCKS_ATTACKS, BLOCK_POSE);
    }

    private ItemStack withAttackRange(ItemStack item) {
        // respect an item's own attack_range (minigame weapon, spear); also keeps a creative-echoed stamp
        // from being re-stamped onto itself
        if (item.isAir() || item.get(DataComponents.ATTACK_RANGE) != null) return item;
        return item.with(DataComponents.ATTACK_RANGE, new AttackRange(0f, attackReach(), 0f, 5f, policy.attackHitboxMargin, 1f));
    }

    private ItemStack reskinThrowable(ItemStack item) {
        Material original = item.material();
        if (!THROW_ON_USE.contains(original)) return item;
        // PAPER's use() PASSes -> no client swing, use_item still sent. Name and stack are re-applied as the original's
        // EFFECTIVE values (get() resolves override-or-default) so paper's "Paper"/64 never show.
        return item.withMaterial(Material.PAPER)
                .withItemModel(original.key().asString())
                .with(DataComponents.ITEM_NAME, item.get(DataComponents.ITEM_NAME))
                .withMaxStackSize(item.get(DataComponents.MAX_STACK_SIZE));
    }

    /** Inverse of {@link #reskinThrowable} for a creative echo: the re-applied name/stack equal the true item's defaults, so only the marker needs dropping. */
    private ItemStack restoreThrowable(ItemStack item) {
        if (item.material() != Material.PAPER) return item;
        String model = item.get(DataComponents.ITEM_MODEL);
        Material original = model != null ? Material.fromKey(model) : null;
        if (original == null || !THROW_ON_USE.contains(original)) return item;
        return item.withMaterial(original).without(DataComponents.ITEM_MODEL);
    }
}
