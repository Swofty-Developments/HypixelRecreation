package net.swofty.type.skyblockgeneric.hunting;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.gui.inventories.hunting.GUIHuntrap;
import net.swofty.type.skyblockgeneric.item.handlers.place.PlaceEventRegistry;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class HuntrapService {
    private static final Map<TrapKey, HuntrapDisplay> DISPLAYS = new HashMap<>();

    private HuntrapService() {
    }

    public static void initialize() {
        PlaceEventRegistry.register("HUNTRAP", (event, player, item) -> {
            event.setCancelled(true);
            event.setBlock(Block.AIR);
            ItemType type = item.getAttributeHandler().getPotentialType();
            if (type == null) return;
            HuntrapTier tier;
            try {
                tier = HuntrapTier.valueOf(type.name());
            } catch (IllegalArgumentException ignored) {
                return;
            }
            Pos position = event.getBlockPosition().asPos();
            List<AttributeDefinition> pool = eligible(player, position);
            if (pool.isEmpty()) {
                player.sendMessage("<c>No huntable creatures can be caught by a Huntrap here!");
                return;
            }
            String world = HypixelConst.getTypeLoader().getType().name();
            long now = System.currentTimeMillis();
            long duration = tier.durationMillis();
            if (pool.stream().allMatch(value -> value.category() == AttributeDefinition.AttributeCategory.FOREST)) {
                duration = Math.round(duration / (1D + AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("U11")) / 100D));
            }
            DatapointHunting.PlacedHuntrap trap = new DatapointHunting.PlacedHuntrap(world, position.x(),
                    position.y(), position.z(), tier.name(), now, now + duration, null);
            player.getHuntingData().getTraps().add(trap);
            item.setAmount(item.getAmount() - 1);
            player.setItemInMainHand(item.getAmount() <= 0 ? ItemStack.AIR : item.getItemStack());
            spawn(player, trap);
            player.sendMessage("<a>Placed your <e>{}<a>!", tier.displayName());
        });
        MinecraftServer.getSchedulerManager().submitTask(() -> {
            tick();
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_START);
    }

    private static void tick() {
        List<SkyBlockPlayer> loadedPlayers = SkyBlockGenericLoader.getLoadedPlayers();
        for (SkyBlockPlayer player : loadedPlayers) {
            String world = HypixelConst.getTypeLoader().getType().name();
            List<DatapointHunting.PlacedHuntrap> traps = player.getHuntingData().getTraps();
            for (int i = 0; i < traps.size(); i++) {
                DatapointHunting.PlacedHuntrap trap = traps.get(i);
                if (!trap.world().equals(world)) continue;
                TrapKey key = TrapKey.of(player.getUuid(), trap);
                if (trap.caughtShard() == null && trap.catchAt() <= System.currentTimeMillis()) {
                    List<AttributeDefinition> pool = eligible(player, new Pos(trap.x(), trap.y(), trap.z()));
                    if (!pool.isEmpty()) {
                        AttributeDefinition caught = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
                        DatapointHunting.PlacedHuntrap complete = new DatapointHunting.PlacedHuntrap(trap.world(), trap.x(),
                                trap.y(), trap.z(), trap.tier(), trap.placedAt(), trap.catchAt(), caught.id().toString());
                        traps.set(i, complete);
                        HuntrapDisplay old = DISPLAYS.remove(key);
                        if (old != null) old.remove();
                        trap = complete;
                    }
                }
                if (!DISPLAYS.containsKey(key)) spawn(player, trap);
            }
            List<TrapKey> obsolete = DISPLAYS.keySet().stream().filter(key -> key.owner.equals(player.getUuid()))
                    .filter(key -> traps.stream().noneMatch(trap -> key.equals(TrapKey.of(player.getUuid(), trap)))).toList();
            obsolete.forEach(key -> {
                HuntrapDisplay display = DISPLAYS.remove(key);
                if (display != null) display.remove();
            });
        }
        Set<UUID> loaded = loadedPlayers.stream().map(SkyBlockPlayer::getUuid).collect(Collectors.toSet());
        DISPLAYS.keySet().stream().filter(key -> !loaded.contains(key.owner)).toList().forEach(key -> {
            HuntrapDisplay display = DISPLAYS.remove(key);
            if (display != null) display.remove();
        });
    }

    private static void spawn(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        if (player.getInstance() == null) return;
        TrapKey key = TrapKey.of(player.getUuid(), trap);
        HuntrapTier tier = HuntrapTier.valueOf(trap.tier());
        AttributeDefinition caught = AttributeRegistry.get(trap.caughtShard());
        HuntrapDisplay display = new HuntrapDisplay(player.getInstance(), new Pos(trap.x(), trap.y(), trap.z()),
                player.getUuid(), tier, caught, trap.catchAt(), clicker -> GUIHuntrap.open(clicker, trap));
        DISPLAYS.put(key, display);
    }

    public static DatapointHunting.PlacedHuntrap current(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        return player.getHuntingData().getTraps().stream()
                .filter(value -> TrapKey.of(player.getUuid(), value).equals(TrapKey.of(player.getUuid(), trap)))
                .findFirst().orElse(null);
    }

    public static void claim(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        DatapointHunting.PlacedHuntrap current = current(player, trap);
        if (current == null) return;
        AttributeDefinition caught = AttributeRegistry.get(current.caughtShard());
        if (caught == null) {
            long seconds = Math.max(0, (current.catchAt() - System.currentTimeMillis()) / 1000);
            player.sendMessage("<c>This Huntrap is empty! <7>Time remaining: <a>{}",
                    "%d h %02d m %02d s".formatted(seconds / 3600, seconds / 60 % 60, seconds % 60));
            return;
        }
        int amount = AttributeEffectService.fortunateAmount(player, caught);
        player.getHuntingData().addShards(caught.id(), amount);
        HuntrapTier tier = HuntrapTier.valueOf(current.tier());
        player.addAndUpdateItem(tier.itemType());
        player.getHuntingData().getTraps().remove(current);
        HuntrapDisplay display = DISPLAYS.remove(TrapKey.of(player.getUuid(), current));
        if (display != null) display.remove();
        player.sendMessage("<a>Your Huntrap caught <e>{}x <a>{}<a>!", amount, caught.shardName());
    }

    public static void pickup(SkyBlockPlayer player, DatapointHunting.PlacedHuntrap trap) {
        DatapointHunting.PlacedHuntrap current = current(player, trap);
        if (current == null) return;
        if (current.caughtShard() != null) {
            claim(player, current);
            return;
        }
        HuntrapTier tier = HuntrapTier.valueOf(current.tier());
        player.addAndUpdateItem(tier.itemType());
        player.getHuntingData().getTraps().remove(current);
        HuntrapDisplay display = DISPLAYS.remove(TrapKey.of(player.getUuid(), current));
        if (display != null) display.remove();
        player.sendMessage("<a>Picked up your <e>{}<a>!", tier.displayName());
    }

    static List<AttributeDefinition> eligible(SkyBlockPlayer player, Pos position) {
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(position);
        String regionName = region == null ? "" : StringUtility.toNormalCase(region.getType().name()).toLowerCase(Locale.ROOT);
        String worldName = StringUtility.toNormalCase(HypixelConst.getTypeLoader().getType().name()
                .replace("SKYBLOCK_", "")).toLowerCase(Locale.ROOT);
        List<AttributeDefinition> trapAttributes = AttributeRegistry.values().stream()
                .filter(value -> value.sources().stream().anyMatch(source -> source.type() == AttributeDefinition.SourceType.TRAP))
                .toList();
        List<AttributeDefinition> regional = trapAttributes.stream().filter(value -> region != null
                && value.sources().stream().anyMatch(source -> source.locations().contains(region.getType()))).toList();
        if (!regional.isEmpty()) return regional;
        return trapAttributes.stream().filter(value -> value.sources().stream()
                .anyMatch(source -> source.island() == null || source.island() == HypixelConst.getTypeLoader().getType())).toList();
    }

    private record TrapKey(UUID owner, double x, double y, double z, String world) {
        static TrapKey of(UUID owner, DatapointHunting.PlacedHuntrap trap) {
            return new TrapKey(owner, trap.x(), trap.y(), trap.z(), trap.world());
        }
    }
}
