package net.swofty.type.ravengarddungeon.interactables;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class InteractableRegistry {
    private static final double MAX_DISTANCE = 4.5;
    private static final int CHANNEL_TICKS = 18;

    private static final Map<Integer, DungeonInteractable> BY_INTERACTION = new ConcurrentHashMap<>();
    private static final Map<UUID, Channel> CHANNELS = new ConcurrentHashMap<>();
    private static final Map<UUID, BossBar> BARS = new ConcurrentHashMap<>();
    private static final Map<Inventory, Consumer<Player>> CLOSE_HANDLERS = new ConcurrentHashMap<>();

    private record Channel(DungeonInteractable target, float progress) {
    }

    private InteractableRegistry() {
    }

    public static void register(DungeonInteractable interactable) {
        BY_INTERACTION.put(interactable.getInteraction().getEntityId(), interactable);
    }

    public static void registerExtraInteraction(DungeonInteractable interactable, Entity extra) {
        BY_INTERACTION.put(extra.getEntityId(), interactable);
    }

    public static DungeonInteractable byInteraction(Entity entity) {
        return entity == null ? null : BY_INTERACTION.get(entity.getEntityId());
    }

    public static void unregister(DungeonInteractable interactable) {
        if (interactable.getInteraction() != null) {
            BY_INTERACTION.remove(interactable.getInteraction().getEntityId());
        }
    }

    public static void onClick(Player player, Entity target) {
        DungeonInteractable interactable = BY_INTERACTION.get(target == null ? -1 : target.getEntityId());
        if (interactable == null
                || (interactable.isOpened() && !interactable.allowReactivation())) {
            return;
        }
        Channel current = CHANNELS.get(player.getUuid());
        if (current == null || current.target() != interactable) {
            CHANNELS.put(player.getUuid(), new Channel(interactable, 0f));
            bar(player, interactable, 0f);
        }
    }

    public static void watchClose(Inventory inventory, Consumer<Player> handler) {
        CLOSE_HANDLERS.put(inventory, handler);
    }

    public static void onInventoryClose(Player player, Inventory inventory) {
        Consumer<Player> handler = inventory == null ? null : CLOSE_HANDLERS.remove(inventory);
        if (handler != null) {
            handler.accept(player);
        }
    }

    public static void startTask() {
        MinecraftServer.getSchedulerManager().buildTask(InteractableRegistry::tick)
                .repeat(TaskSchedule.tick(1)).schedule();
    }

    private static void tick() {
        for (Map.Entry<UUID, Channel> entry : CHANNELS.entrySet()) {
            Player player = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(entry.getKey());
            Channel channel = entry.getValue();
            if (player == null
                    || (channel.target().isOpened() && !channel.target().allowReactivation())
                    || !isLookingAt(player, channel.target())) {
                org.tinylog.Logger.info("Channel cancel: player={} opened={} looking={}",
                        player != null,
                        channel.target().isOpened(),
                        player != null && isLookingAt(player, channel.target()));
                CHANNELS.remove(entry.getKey());
                if (player != null) {
                    hideBar(player);
                }
                continue;
            }
            float progress = channel.progress() + 1f / CHANNEL_TICKS;
            if (progress >= 1f) {
                CHANNELS.remove(entry.getKey());
                hideBar(player);
                channel.target().open(player);
            } else {
                CHANNELS.put(entry.getKey(), new Channel(channel.target(), progress));
                bar(player, channel.target(), progress);
            }
        }
    }

    private static boolean isLookingAt(Player player, DungeonInteractable interactable) {
        Entity interaction = interactable.getInteraction();
        if (interaction.getInstance() != player.getInstance()) {
            return false;
        }
        var meta = (net.minestom.server.entity.metadata.other.InteractionMeta) interaction.getEntityMeta();
        Pos base = interaction.getPosition();
        Vec center = new Vec(base.x(), base.y() + meta.getHeight() / 2.0, base.z());
        Vec eye = new Vec(player.getPosition().x(),
                player.getPosition().y() + player.getEyeHeight(),
                player.getPosition().z());
        Vec toCenter = center.sub(eye);
        double distance = toCenter.length();
        if (distance > MAX_DISTANCE) {
            return false;
        }
        Vec direction = player.getPosition().direction();
        double along = toCenter.dot(direction);
        if (along < -0.5) {
            return false;
        }
        Vec closest = eye.add(direction.mul(Math.max(0, along)));
        double miss = closest.sub(center).length();
        double radius = Math.max(meta.getWidth(), meta.getHeight()) / 2.0 + 0.9;
        return miss <= radius;
    }

    private static void bar(Player player, DungeonInteractable target, float progress) {
        BossBar bar = BARS.computeIfAbsent(player.getUuid(), ignored -> {
            BossBar created = BossBar.bossBar(Component.empty(), 0f,
                    BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            player.showBossBar(created);
            return created;
        });
        bar.name(Text.of("<#fefd02>{}", target.castLabel()));
        bar.progress(Math.min(1f, progress));
    }

    private static void hideBar(Player player) {
        BossBar bar = BARS.remove(player.getUuid());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }
}
