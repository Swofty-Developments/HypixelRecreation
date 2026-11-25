package io.github.term4.polyp;

import io.github.term4.polyp.world.MechanicsWorld;
import java.util.function.Consumer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.Taggable;
import org.jetbrains.annotations.Nullable;

/**
 * Scoped {@link MechanicsProfile} registry: assign profiles per player, per world (a virtual game world), per instance,
 * or globally. Configs are immutable, so a runtime swap takes effect on the next hit. Every assignment takes
 * {@code null} to clear. Resolution is per <em>member</em>, highest scope first:
 * <pre>player profile -> world profile -> instance profile -> global profile -> the system's install config</pre>
 * so a partial profile (e.g. knockback only) overrides just that system. Resolve a single member with {@link #resolve};
 * a hit reading several members should use {@link #resolved} to walk the scopes once.
 *
 * <p><b>Scope subject.</b> Attack resolves against the <em>attacker</em>; damage and knockback resolve against the
 * <em>victim</em>. Both usually share the instance, so the distinction only matters for player overrides.
 *
 * <p>Player and instance assignments live in transient tags, so they clean up with their holder (a player's profile
 * drops on disconnect).
 */
public final class MechanicsProfiles {

    private static final Tag<MechanicsProfile> PROFILE = Tag.Transient("polyp:profile");

    private volatile @Nullable MechanicsProfile global;
    // fires with the affected player, or null for a wider scope (global/world/instance)
    private volatile @Nullable Consumer<@Nullable Player> changeHook;

    MechanicsProfiles() {}

    void onChange(Consumer<@Nullable Player> hook) { this.changeHook = hook; }

    private void changed(@Nullable Player player) {
        var hook = changeHook;
        if (hook != null) hook.accept(player);
    }

    /** The server-wide fallback profile. */
    public void setGlobal(@Nullable MechanicsProfile profile) {
        this.global = profile;
        changed(null);
    }
    public @Nullable MechanicsProfile global() { return global; }

    /** Re-fires the change hook - call after moving a player between worlds. */
    public void refresh() { changed(null); }

    private static void assign(Taggable holder, @Nullable MechanicsProfile profile) {
        if (profile == null) holder.removeTag(PROFILE);
        else holder.setTag(PROFILE, profile);
    }

    public void setWorld(MechanicsWorld world, @Nullable MechanicsProfile profile) {
        assign(world, profile);
        changed(null);
    }
    public @Nullable MechanicsProfile world(MechanicsWorld world) { return world.getTag(PROFILE); }

    public void setInstance(Instance instance, @Nullable MechanicsProfile profile) {
        assign(instance, profile);
        changed(null);
    }
    public @Nullable MechanicsProfile instance(Instance instance) { return instance.getTag(PROFILE); }

    public void setPlayer(Player player, @Nullable MechanicsProfile profile) {
        assign(player, profile);
        changed(player);
    }
    public @Nullable MechanicsProfile player(Player player) { return player.getTag(PROFILE); }

    // single-member assignment: merges into the scope's existing profile instead of replacing it

    private static MechanicsProfile with(@Nullable MechanicsProfile base, ConfigKey<?> key, @Nullable Object value) {
        MechanicsProfile.Builder b = base != null ? base.toBuilder() : MechanicsProfile.builder();
        @SuppressWarnings("unchecked")
        ConfigKey<Object> typed = (ConfigKey<Object>) key;
        return b.set(typed, value).build();
    }

    public <C> void setGlobal(ConfigKey<C> key, @Nullable C value) {
        setGlobal(with(global, key, value));
    }

    public <C> void setWorld(MechanicsWorld world, ConfigKey<C> key, @Nullable C value) {
        setWorld(world, with(world(world), key, value));
    }

    public <C> void setInstance(Instance instance, ConfigKey<C> key, @Nullable C value) {
        setInstance(instance, with(instance(instance), key, value));
    }

    public <C> void setPlayer(Player player, ConfigKey<C> key, @Nullable C value) {
        setPlayer(player, with(player(player), key, value));
    }

    /** The effective value of {@code key} for {@code subject}. For a hit reading several members, prefer {@link #resolved}. */
    public <C> @Nullable C resolve(@Nullable Entity subject, ConfigKey<C> key) {
        if (subject != null) {
            if (subject instanceof Player p) {
                C v = memberOf(p.getTag(PROFILE), key);
                if (v != null) return v;
            }
            Instance in = subject.getInstance();
            if (in != null) {
                C v = memberOf(MechanicsWorld.of(subject).getTag(PROFILE), key);
                if (v != null) return v;
                v = memberOf(in.getTag(PROFILE), key);
                if (v != null) return v;
            }
        }
        return memberOf(global, key);
    }

    /** The effective value of {@code key} for a WORLD with no entity in hand - a sourceless explosion, a
     *  block-driven effect. Walks the same chain minus the player: shard/world -&gt; instance -&gt; global. */
    public <C> @Nullable C resolveWorld(@Nullable MechanicsWorld world, ConfigKey<C> key) {
        if (world != null) {
            C v = memberOf(world.getTag(PROFILE), key);
            if (v != null) return v;
            Instance in = world.instance();
            if (in != null) {
                v = memberOf(in.getTag(PROFILE), key);
                if (v != null) return v;
            }
        }
        return memberOf(global, key);
    }

    /** {@link #resolve} with a fallback: the effective value of {@code key} for {@code subject}, else {@code fallback}. */
    public <C> C resolveOr(@Nullable Entity subject, ConfigKey<C> key, C fallback) {
        C scoped = resolve(subject, key);
        return scoped != null ? scoped : fallback;
    }

    private static <C> @Nullable C memberOf(@Nullable MechanicsProfile profile, ConfigKey<C> key) {
        return profile != null ? profile.get(key) : null;
    }

    /** Snapshots {@code subject}'s scopes once, then answers any key off it - one scope walk for a whole hit. */
    public Resolved resolved(@Nullable Entity subject) {
        MechanicsProfile player = subject instanceof Player p ? p.getTag(PROFILE) : null;
        MechanicsProfile world = null;
        MechanicsProfile instance = null;
        if (subject != null) {
            Instance in = subject.getInstance();
            if (in != null) {
                world = MechanicsWorld.of(subject).getTag(PROFILE);
                instance = in.getTag(PROFILE);
            }
        }
        return new Resolved(player, world, instance, global);
    }

    /** A one-shot resolution view over fixed player / world / instance / global scopes. */
    public static final class Resolved {
        private final @Nullable MechanicsProfile player;
        private final @Nullable MechanicsProfile world;
        private final @Nullable MechanicsProfile instance;
        private final @Nullable MechanicsProfile global;

        private Resolved(@Nullable MechanicsProfile player, @Nullable MechanicsProfile world,
                         @Nullable MechanicsProfile instance, @Nullable MechanicsProfile global) {
            this.player = player;
            this.world = world;
            this.instance = instance;
            this.global = global;
        }

        public <C> @Nullable C get(ConfigKey<C> key) {
            C v;
            if (player != null && (v = player.get(key)) != null) return v;
            if (world != null && (v = world.get(key)) != null) return v;
            if (instance != null && (v = instance.get(key)) != null) return v;
            return global != null ? global.get(key) : null;
        }
    }
}
