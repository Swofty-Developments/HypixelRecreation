package net.swofty.type.bedwarsgame.game;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.respawn.RespawnHandler;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class BedWarsRespawnHandler implements RespawnHandler<BedWarsPlayer> {
    private static final int DEFAULT_RESPAWN_DELAY = 5;

    private final BedWarsGame game;
    private final Map<UUID, Task> respawnTasks = new ConcurrentHashMap<>();

    @Override
    public boolean canRespawn(UUID playerId) {
        return game.getPlayerTeam(playerId)
            .map(BedWarsTeam::isBedAlive)
            .orElse(false);
    }

    @Override
    public int getRespawnDelay(UUID playerId) {
        return DEFAULT_RESPAWN_DELAY;
    }

    @Override
    public void startRespawn(BedWarsPlayer player) {
        if (!canRespawn(player.getUuid())) {
            game.onPlayerEliminated(player);
            return;
        }

        AtomicInteger countdown = new AtomicInteger(getRespawnDelay(player.getUuid()));
        AtomicReference<Task> taskRef = new AtomicReference<>();

        Task task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (!player.isOnline()) {
                cancelRespawn(player.getUuid());
                return;
            }

            int remaining = countdown.getAndDecrement();

            if (remaining > 0) {
                showRespawnTitle(player, remaining);
            } else {
                completeRespawn(player);
                Task t = taskRef.get();
                if (t != null) t.cancel();
                respawnTasks.remove(player.getUuid());
            }
        }).repeat(TaskSchedule.seconds(1)).schedule();

        taskRef.set(task);
        respawnTasks.put(player.getUuid(), task);
    }

    @Override
    public void cancelRespawn(UUID playerId) {
        Task task = respawnTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public boolean isRespawning(UUID playerId) {
        return respawnTasks.containsKey(playerId);
    }

    final Title.Times titleTimes = Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(1), Duration.ofMillis(100));

    private void showRespawnTitle(BedWarsPlayer player, int seconds) {
        player.showTitle("<c>YOU DIED!", "<e>You will respawn in <c>{}</c> second{}!", titleTimes,
            seconds, seconds == 1 ? "" : "s");
    }

    public void equipTeamArmor(BedWarsPlayer player, BedWarsMapsConfig.TeamKey teamKey) {
        final int armorLevel = getArmorLevel(player);
        final Color teamColor = new Color(teamKey.rgb());

        equipLegArmor(player, armorLevel, teamColor);
        equipUpperArmor(player, teamColor);
    }

    private int getArmorLevel(BedWarsPlayer player) {
        return player.getTag(TypeBedWarsGameLoader.ARMOR_LEVEL_TAG) == null
            ? 0
            : player.getTag(TypeBedWarsGameLoader.ARMOR_LEVEL_TAG);
    }

    private void equipLegArmor(BedWarsPlayer player, int armorLevel, Color teamColor) {
        ArmorTier tier = ArmorTier.fromLevel(armorLevel);

        if (tier == ArmorTier.LEATHER) {
            player.setEquipment(EquipmentSlot.BOOTS, dyed(Material.LEATHER_BOOTS, teamColor));
            player.setEquipment(EquipmentSlot.LEGGINGS, dyed(Material.LEATHER_LEGGINGS, teamColor));
            return;
        }

        player.setEquipment(EquipmentSlot.BOOTS, ItemStack.of(tier.boots()));
        player.setEquipment(EquipmentSlot.LEGGINGS, ItemStack.of(tier.leggings()));
    }

    private void equipUpperArmor(BedWarsPlayer player, Color teamColor) {
        player.setEquipment(EquipmentSlot.CHESTPLATE, dyed(Material.LEATHER_CHESTPLATE, teamColor));
        player.setEquipment(EquipmentSlot.HELMET, dyed(Material.LEATHER_HELMET, teamColor));
    }

    private ItemStack dyed(Material material, Color color) {
        return ItemStack.of(material)
            .with(DataComponents.DYED_COLOR, color);
    }

    private enum ArmorTier {
        LEATHER(null, null),
        CHAINMAIL(Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS),
        IRON(Material.IRON_BOOTS, Material.IRON_LEGGINGS),
        DIAMOND(Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS);

        private final Material boots;
        private final Material leggings;

        ArmorTier(Material boots, Material leggings) {
            this.boots = boots;
            this.leggings = leggings;
        }

        public Material boots() {
            return boots;
        }

        public Material leggings() {
            return leggings;
        }

        public static ArmorTier fromLevel(int level) {
            return switch (level) {
                case 1 -> CHAINMAIL;
                case 2 -> IRON;
                case 3 -> DIAMOND;
                default -> LEATHER;
            };
        }
    }

    private void completeRespawn(BedWarsPlayer player) {
        player.clearTitle();

        game.setupPlayer(player);
        game.getReplayManager().recordPlayerRespawn(player);
    }
}
