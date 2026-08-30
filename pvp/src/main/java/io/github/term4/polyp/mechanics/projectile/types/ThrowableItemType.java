package io.github.term4.polyp.mechanics.projectile.types;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.cooldown.CooldownSystem;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base for a hand-thrown item projectile (snowball, egg, ender pearl): using the {@link #material} launches one and
 * consumes it (unless creative). Subclasses add only their flight entity + impact behavior.
 */
public abstract class ThrowableItemType extends ProjectileType {

    /** World age of the player's last throw: a block-aimed click can reach the server as TWO packets (see {@link #enable}),
     *  and a block-action item still sees both when its client-side use FAILS (the 1.8 adventure edge). One throw per tick. */
    private static final Tag<Long> LAST_THROW_AGE = Tag.Transient("polyp:last-throw-age");

    private final Material material;
    private final boolean blockAction;
    private @Nullable EventNode<@NotNull PlayerEvent> node;
    private @Nullable ProjectileSystem system;
    private @Nullable Polyp polyp;

    /** Launch-sound effect key ({@link Fx#THROW_SNOWBALL} etc.), or null for none. */
    protected @Nullable Key throwSound() { return null; }

    protected ThrowableItemType(Key key, String name, EntityType entityType, Material material) {
        this(key, name, entityType, material, false);
    }

    /** {@code blockAction}: the item has a client-side block use (fire charge - lights fire), so a block-aimed click
     *  consumes it and sends ONLY use_item_on_block; that event must throw too. */
    protected ThrowableItemType(Key key, String name, EntityType entityType, Material material, boolean blockAction) {
        super(key, name, entityType);
        this.material = material;
        this.blockAction = blockAction;
    }

    @Override
    public void enable(ProjectileSystem system, Polyp polyp) {
        this.system = system;
        this.polyp = polyp;
        EventNode<@NotNull PlayerEvent> n = EventNode.type("polyp:" + key().value(), EventFilter.PLAYER);
        // use_item ONLY for a plain throwable: aimed at a block in reach, the client sends use_item_on_block FOLLOWED
        // by use_item (its useOn passes client-side), and vanilla throws from the latter alone - handling both threw twice
        n.addListener(PlayerUseItemEvent.class, e -> throwItem(e.getPlayer(), e.getHand(), e.getItemStack()));
        // a block action (fire charge) consumes the click client-side: use_item_on_block is then the only signal
        if (blockAction) n.addListener(PlayerUseItemOnBlockEvent.class, e -> throwItem(e.getPlayer(), e.getHand(), e.getItemStack()));
        system.node().addChild(n);
        node = n;
    }

    private void throwItem(Player p, PlayerHand hand, ItemStack item) {
        if (item.material() != material || system == null) return;
        Instance instance = p.getInstance();
        long age = instance != null ? MechanicsWorld.of(p).worldAge() : 0;
        Long last = p.getTag(LAST_THROW_AGE);
        if (last != null && last == age) return; // the same click's second packet
        // server-authoritative cooldown (the client's own overlay is prediction-only and spammable)
        CooldownSystem cooldowns = polyp != null ? polyp.module(CooldownSystem.class) : null;
        if (cooldowns != null && !cooldowns.tryUse(p, material)) return;
        p.setTag(LAST_THROW_AGE, age);
        Key sound = throwSound();
        if (sound != null && polyp != null) Fx.play(polyp.services(), sound, FxContext.of(p));
        var proj = system.launch(ProjectileSnapshot.of(p, this).withItem(item));
        if (p.getGameMode() != GameMode.CREATIVE) {
            p.setItemInHand(hand, item.withAmount(item.amount() - 1));
        }
        system.firstStep(proj);
    }

    @Override
    public void disable() {
        if (system != null && node != null) system.node().removeChild(node);
        node = null;
    }
}
