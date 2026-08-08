package io.github.term4.polyp.mechanics.blocking;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsModule;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.Services;
import io.github.term4.polyp.api.event.blocking.BlockingDamageEvent;
import io.github.term4.polyp.api.event.blocking.BlockingStartEvent;
import io.github.term4.polyp.api.event.blocking.BlockingStopEvent;
import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.BlockingContext;
import io.github.term4.polyp.mechanics.blocking.BlockingConfigResolver.ResolvedBlocking;
import io.github.term4.polyp.mechanics.damage.DamageConfigResolver.DamageContext;
import io.github.term4.polyp.mechanics.damage.DamageSystem;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Item blocking (sword block / shield) - a thin damage hook over the scope {@link BlockingConfig}. An item blocks when
 * its material is configured blockable and it isn't opted out via {@link #BLOCKABLE}; the server is "blocking" while
 * {@link Player#isUsingItem()}. The reduction is applied before armor (vanilla order) by {@link DamageSystem#apply} via
 * {@link #reduce}; <em>how</em> is the resolved {@link BlockingBehavior}'s call.
 *
 * <p>The {@code blocks_attacks} component is the CLIENT's prediction trigger, not the server's authorization - a 1.8
 * profile marks every sword blockable and none of them carry it. Modern clients need it in their VIEW to predict the
 * raise (the compat layer stamps it per-viewer, honouring the opt-out); 1.8 clients block natively. Movement slowdown
 * is client-predicted, nothing applied server-side.
 */
public final class BlockingSystem implements MechanicsModule {

    /** Per-item opt-out: absent = blockable, {@code false} = opted out (never enters the block state, never affects damage). */
    public static final Tag<Boolean> BLOCKABLE = Tag.Boolean("polyp:blockable");

    private final Polyp polyp;
    private final Services services;
    private final BlockingConfig config;
    private final EventNode<@NotNull PlayerEvent> node;

    public BlockingSystem(Polyp polyp, BlockingConfig config) {
        this.polyp = polyp;
        this.services = polyp.services();
        this.config = config;
        this.node = EventNode.type("polyp:blocking", EventFilter.PLAYER);
        node.addListener(PlayerUseItemEvent.class, this::onUse);
        node.addListener(PlayerCancelItemUseEvent.class, e -> onStopUsing(e.getPlayer(), e.getHand(), e.getItemStack()));
        node.addListener(PlayerFinishItemUseEvent.class, e -> onStopUsing(e.getPlayer(), e.getHand(), e.getItemStack()));
    }

    /** Vanilla's hold-forever use time for a raised block (what Minestom grants a {@code blocks_attacks} item). */
    private static final int BLOCK_USE_TICKS = 72000;

    private void onUse(PlayerUseItemEvent e) {
        ItemStack item = e.getItemStack();
        Player player = e.getPlayer();
        if (!blockable(item, configFor(player))) {
            // a component item we won't block (opted out / not configured) - no phantom block
            if (item.has(DataComponents.BLOCKS_ATTACKS)) e.setItemUseTime(0);
            return;
        }
        BlockingStartEvent start = new BlockingStartEvent(player, e.getHand(), item);
        EventDispatcher.call(start);
        if (start.isCancelled()) {
            e.setItemUseTime(0);
            return;
        }
        // Minestom only grants a use time to a blocks_attacks item, so an unstamped-but-configured sword starts here;
        // the already-using guard mirrors UseItemListener's, so an off-hand use can't open a second block
        if (e.getItemUseTime() <= 0 && !player.isUsingItem()) e.setItemUseTime(BLOCK_USE_TICKS);
    }

    private void onStopUsing(Player player, PlayerHand hand, ItemStack item) {
        if (blockable(item, configFor(player))) {
            EventDispatcher.call(new BlockingStopEvent(player, hand, item));
        }
    }

    private boolean blockable(ItemStack item, @Nullable BlockingConfig cfg) {
        return !Boolean.FALSE.equals(item.getTag(BLOCKABLE)) && cfg != null && cfg.blocks(item.material());
    }

    public EventNode<@NotNull PlayerEvent> node() { return node; }
    public BlockingConfig config() { return config; }

    /** Effective config for {@code subject} (the defender): the scoped profile, else the install config. */
    public BlockingConfig configFor(@Nullable Entity subject) {
        return polyp.profiles().resolveOr(subject, MechanicsKeys.BLOCKING, config);
    }

    /** Whether {@code player} is currently raising a blockable item (use-state only; the per-hit gates live in {@link #reduce}). */
    public boolean isBlocking(Player player) {
        if (!player.isUsingItem()) return false;
        PlayerHand hand = player.getItemUseHand();
        return hand != null && blockable(player.getItemInHand(hand), configFor(player));
    }

    /**
     * Reduces a blocked hit. Returns {@code amount} unchanged if the victim isn't blocking, the item isn't blockable in
     * scope, or the behavior declines; else fires {@link BlockingDamageEvent} and returns the reduced amount (never increases it).
     */
    public float reduce(LivingEntity victim, DamageContext damage, float amount) {
        if (amount <= 0 || !(victim instanceof Player p) || !p.isUsingItem()) return amount;
        PlayerHand hand = p.getItemUseHand();
        if (hand == null) return amount;
        ItemStack item = p.getItemInHand(hand);
        BlockingConfig cfg = configFor(p);
        if (!blockable(item, cfg)) return amount;

        BlockingContext ctx = new BlockingContext(p, item, hand, damage, services);
        ResolvedBlocking r = BlockingConfigResolver.resolve(cfg, ctx);
        if (!r.enabled()) return amount;

        float reduced = r.behavior().apply(ctx, r, amount);
        if (reduced >= amount) return amount;
        BlockingDamageEvent ev = new BlockingDamageEvent(p, damage.snap().source(), amount, reduced,
                damage.snap().type().minecraftType());
        EventDispatcher.call(ev);
        if (ev.isCancelled()) return amount;
        return reduced;
    }

    /** Installs the system with an empty config (nothing blocks until a scope/install config marks materials blockable). */
    public static BlockingSystem install(Polyp polyp) {
        return install(polyp, BlockingConfig.builder().build());
    }

    public static BlockingSystem install(Polyp polyp, BlockingConfig cfg) {
        BlockingSystem system = new BlockingSystem(polyp, cfg);
        polyp.register(system);
        polyp.install(system.node);
        return system;
    }
}
