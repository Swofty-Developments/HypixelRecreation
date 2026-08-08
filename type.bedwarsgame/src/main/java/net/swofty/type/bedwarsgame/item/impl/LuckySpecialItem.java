package net.swofty.type.bedwarsgame.item.impl;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.item.SimpleInteractableItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.utility.ScheduleUtility;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LuckySpecialItem extends SimpleInteractableItem {
    public static final Tag<String> ACTION_TAG = Tag.String("lucky_action");
    private static final Tag<Integer> USES_TAG = Tag.Integer("lucky_uses");

    public LuckySpecialItem() {
        super("lucky_special");
    }

    @Override
    public ItemStack getBlandItem() {
        return item("Lucky Item", Material.NETHER_STAR, "generic", 1);
    }

    public ItemStack item(String name, Material material, String action, int uses, String... lore) {
        List<String> itemLore = new java.util.ArrayList<>(List.of(lore));
        if (uses > 0) {
            itemLore.add("");
            itemLore.add("§7Uses: §e" + uses);
        }
        return ItemStackCreator.getStack("§a" + name, material, 1, itemLore).build()
            .with(DataComponents.CUSTOM_DATA, new CustomData(CompoundBinaryTag.builder()
                .putString("item", getId())
                .putString("lucky_action", action)
                .putInt("lucky_uses", uses)
                .build()));
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        handle((BedWarsPlayer) event.getPlayer(), event.getHand(), event.getItemStack(), null);
    }

    @Override
    public void onItemUseOnBlock(PlayerUseItemOnBlockEvent event) {
        handle((BedWarsPlayer) event.getPlayer(), event.getHand(), event.getItemStack(), event.getPosition());
    }

    private void handle(BedWarsPlayer player, PlayerHand hand, ItemStack stack, Point clickedBlock) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        String action = data.getTag(ACTION_TAG);
        if (action == null) return;

        boolean consumed = switch (action) {
            case "bed_pointer" -> bedPointer(player);
            case "sleepinator" -> targetEffect(player, "Sleepinator", PotionEffect.BLINDNESS, 1, 100);
            case "lava_rune" -> placeTemporaryBlock(player, clickedBlock, Block.LAVA, 80, "§aLava Rune activated.");
            case "hot_potato" -> hotPotato(player);
            case "instant_barrier" -> instantBarrier(player);
            case "stasis" -> stasis(player);
            case "time_warp_pearl" -> timeWarp(player);
            case "vampire_blood" -> vampireBlood(player);
            case "grappling_hook" -> grapplingHook(player);
            case "gravity_gun", "jedi_force" ->
                pushTarget(player, action.equals("gravity_gun") ? 16 : 20, 12, "§aForce pushed target.");
            case "dreadlord_skull" -> dreadlordSkull(player);
            case "teleport_beam" -> teleportHome(player);
            case "super_star" -> superStar(player);
            case "void_maker" -> voidMaker(player);
            case "nuke" -> nuke(player);
            case "magic_toy_stick" -> magicToyStick(player);
            case "water_balloon" ->
                placeTemporaryBlock(player, clickedBlock, Block.WATER, 240, "§bWater splashed down.");
            case "chicken_bomb" -> chickenBomb(player);
            case "explosive_chicken" -> explosiveChicken(player);
            default -> {
                player.sendMessage("§cThis Lucky item is not implemented yet.");
                yield false;
            }
        };

        if (consumed) {
            consumeUse(player, hand, stack);
        }
    }

    private void consumeUse(BedWarsPlayer player, PlayerHand hand, ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        int uses = data == null ? 1 : Math.max(1, data.getTag(USES_TAG));
        if (uses <= 1) {
            player.setItemInHand(hand, stack.consume(1));
            return;
        }
        player.setItemInHand(hand, stack.with(DataComponents.CUSTOM_DATA, new CustomData(data.nbt().putInt("lucky_uses", uses - 1))));
    }

    private boolean bedPointer(BedWarsPlayer player) {
        Optional<BedWarsMapsConfig.MapTeam> nearest = player.getGame().getMapEntry().getConfiguration().getTeams().entrySet().stream()
            .filter(entry -> !entry.getKey().equals(player.getTeamKey()))
            .filter(entry -> player.getGame().isBedAlive(entry.getKey()))
            .map(java.util.Map.Entry::getValue)
            .filter(team -> team.getBed() != null && team.getBed().feet() != null)
            .min(Comparator.comparingDouble(team -> player.getPosition().distance(pos(team.getBed().feet()))));
        if (nearest.isEmpty()) {
            player.sendMessage("§cNo enemy beds found.");
            return false;
        }
        Vec3i feet = nearest.get().getBed().feet();
        player.sendMessage("§aNearest enemy bed: §e" + feet.x() + ", " + feet.y() + ", " + feet.z());
        return false;
    }

    private boolean targetEffect(BedWarsPlayer player, String name, PotionEffect effect, int amplifier, int ticks) {
        BedWarsPlayer target = target(player, 12).orElse(null);
        if (target == null) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        target.addEffect(new Potion(effect, (byte) amplifier, ticks));
        target.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 1, ticks));
        player.sendMessage("§a" + name + " hit " + target.getUsername() + ".");
        return true;
    }

    private boolean placeTemporaryBlock(BedWarsPlayer player, Point clickedBlock, Block block, int ticks, String message) {
        Point base = clickedBlock != null ? clickedBlock.add(0, 1, 0) : firstAirInFront(player, 4);
        if (base == null) {
            player.sendMessage("§cNo space in front of you.");
            return false;
        }
        Block previous = player.getInstance().getBlock(base);
        player.getInstance().setBlock(base, block.withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true));
        ScheduleUtility.delay(() -> {
            if (player.getInstance() != null && player.getInstance().getBlock(base).compare(block)) {
                player.getInstance().setBlock(base, previous);
            }
        }, TaskSchedule.tick(ticks));
        player.sendMessage(message);
        return true;
    }

    private boolean hotPotato(BedWarsPlayer player) {
        UUID playerUuid = player.getUuid();
        player.sendMessage("§cHot Potato will explode in 5 seconds!");
        ScheduleUtility.delay(() -> {
            BedWarsPlayer current = player.getGame() == null ? null : player.getGame().getPlayer(playerUuid).orElse(null);
            if (current == null || current.getInstance() == null) return;
            explode(current.getInstance(), current.getPosition(), 3.0f, current);
        }, TaskSchedule.seconds(5));
        return true;
    }

    private boolean instantBarrier(BedWarsPlayer player) {
        Vec direction = horizontalDirection(player);
        Vec side = new Vec(-direction.z(), 0, direction.x());
        Pos center = player.getPosition().add(direction.mul(3)).asPos();
        java.util.Map<Point, Block> previous = new java.util.HashMap<>();
        for (int y = 0; y < 3; y++) {
            for (int x = -2; x <= 2; x++) {
                Point point = center.add(side.mul(x)).add(0, y, 0);
                previous.put(point, player.getInstance().getBlock(point));
                player.getInstance().setBlock(point, Block.OBSIDIAN.withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true));
            }
        }
        ScheduleUtility.delay(() -> previous.forEach((point, block) -> {
            if (player.getInstance() != null && player.getInstance().getBlock(point).compare(Block.OBSIDIAN)) {
                player.getInstance().setBlock(point, block);
            }
        }), TaskSchedule.seconds(8));
        return true;
    }

    private boolean stasis(BedWarsPlayer player) {
        player.getGame().getPlayers().stream()
            .filter(other -> other != player && !other.getTeamKey().equals(player.getTeamKey()))
            .filter(other -> other.getPosition().distance(player.getPosition()) <= 18)
            .forEach(other -> {
                other.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 10, 120));
                other.addEffect(new Potion(PotionEffect.MINING_FATIGUE, (byte) 5, 120));
            });
        player.sendMessage("§aStasis activated.");
        return true;
    }

    private boolean timeWarp(BedWarsPlayer player) {
        Pos start = player.getPosition();
        player.sendMessage("§aTime Warp started.");
        ScheduleUtility.delay(() -> {
            if (player.getInstance() != null) {
                player.teleport(start);
                player.sendMessage("§aTime warped!");
            }
        }, TaskSchedule.seconds(5));
        return true;
    }

    private boolean vampireBlood(BedWarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 2, 160));
        player.addEffect(new Potion(PotionEffect.STRENGTH, (byte) 0, 160));
        return true;
    }

    private boolean grapplingHook(BedWarsPlayer player) {
        player.setVelocity(player.getVelocity().add(player.getPosition().direction().mul(18)).add(0, 5, 0));
        return true;
    }

    private boolean pushTarget(BedWarsPlayer player, double range, double strength, String message) {
        BedWarsPlayer target = target(player, range).orElse(null);
        if (target == null) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        target.setVelocity(target.getVelocity().add(player.getPosition().direction().mul(strength)).add(0, 4, 0));
        player.sendMessage(message);
        return true;
    }

    private boolean dreadlordSkull(BedWarsPlayer player) {
        BedWarsPlayer target = target(player, 18).orElse(null);
        if (target == null) {
            player.sendMessage("§cNo target in sight.");
            return false;
        }
        target.damage(Damage.fromPlayer(player, 7f));
        target.setVelocity(target.getVelocity().add(player.getPosition().direction().mul(10)).add(0, 3, 0));
        return true;
    }

    private boolean teleportHome(BedWarsPlayer player) {
        BedWarsMapsConfig.MapTeam team = player.getGame().getMapEntry().getConfiguration().getTeams().get(player.getTeamKey());
        if (team == null || team.getSpawn() == null) return false;
        player.teleport(pos(team.getSpawn()));
        return true;
    }

    private boolean superStar(BedWarsPlayer player) {
        player.addEffect(new Potion(PotionEffect.RESISTANCE, (byte) 10, 180));
        player.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 2, 180));
        player.sendMessage("§eYou are invincible for a short time!");
        return true;
    }

    private boolean voidMaker(BedWarsPlayer player) {
        Point point = firstBreakableInFront(player, 8);
        if (point == null) {
            player.sendMessage("§cNo obsidian or bedrock in range.");
            return false;
        }
        player.getInstance().setBlock(point, Block.AIR);
        player.sendMessage("§5Void Maker removed a block.");
        return true;
    }

    private boolean nuke(BedWarsPlayer player) {
        Point target = firstSolidInFront(player, 50);
        if (target == null) {
            player.sendMessage("§cNo target selected.");
            return false;
        }
        player.sendMessage("§cNuke inbound!");
        ScheduleUtility.delay(() -> explode(player.getInstance(), target.asPos(), 8f, player), TaskSchedule.seconds(5));
        return true;
    }

    private boolean magicToyStick(BedWarsPlayer player) {
        Point target = firstSolidInFront(player, 8);
        if (target == null) return false;
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    Point point = target.add(x, y, z);
                    if (Boolean.TRUE.equals(player.getInstance().getBlock(point).getTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG))) {
                        player.getInstance().setBlock(point, Block.AIR);
                    }
                }
            }
        }
        return true;
    }

    private boolean chickenBomb(BedWarsPlayer player) {
        for (int i = 0; i < 8; i++) {
            LivingEntity chicken = new LivingEntity(EntityType.CHICKEN);
            chicken.setInstance(player.getInstance(), player.getPosition().add(Math.random() * 2 - 1, 0, Math.random() * 2 - 1));
            chicken.scheduler().buildTask(chicken::remove).delay(TaskSchedule.seconds(20)).schedule();
        }
        return true;
    }

    private boolean explosiveChicken(BedWarsPlayer player) {
        LivingEntity chicken = new LivingEntity(EntityType.CHICKEN);
        chicken.setCustomName(Component.text("§cExplosive Chicken"));
        chicken.setCustomNameVisible(true);
        chicken.setInstance(player.getInstance(), player.getPosition());
        ScheduleUtility.delay(() -> {
            if (!chicken.isRemoved()) {
                explode(player.getInstance(), chicken.getPosition(), 3.5f, player);
                chicken.remove();
            }
        }, TaskSchedule.seconds(6));
        return true;
    }

    private Optional<BedWarsPlayer> target(BedWarsPlayer player, double range) {
        Vec direction = player.getPosition().direction().normalize();
        return player.getGame().getPlayers().stream()
            .filter(other -> other != player)
            .filter(other -> !other.getTeamKey().equals(player.getTeamKey()))
            .filter(other -> other.getPosition().distance(player.getPosition()) <= range)
            .filter(other -> {
                Vec toTarget = other.getPosition().sub(player.getPosition()).asVec().normalize();
                return direction.dot(toTarget) > 0.65;
            })
            .min(Comparator.comparingDouble(other -> other.getPosition().distance(player.getPosition())));
    }

    private Point firstAirInFront(BedWarsPlayer player, int range) {
        Vec direction = player.getPosition().direction().normalize();
        for (int i = 1; i <= range; i++) {
            Point point = player.getPosition().add(0, player.getEyeHeight(), 0).add(direction.mul(i));
            if (player.getInstance().getBlock(point).air()) {
                return new Pos(point.blockX(), point.blockY(), point.blockZ());
            }
        }
        return null;
    }

    private Point firstSolidInFront(BedWarsPlayer player, int range) {
        Vec direction = player.getPosition().direction().normalize();
        for (int i = 1; i <= range; i++) {
            Point point = player.getPosition().add(0, player.getEyeHeight(), 0).add(direction.mul(i));
            if (!player.getInstance().getBlock(point).air()) {
                return new Pos(point.blockX(), point.blockY(), point.blockZ());
            }
        }
        return null;
    }

    private Point firstBreakableInFront(BedWarsPlayer player, int range) {
        Vec direction = player.getPosition().direction().normalize();
        for (int i = 1; i <= range; i++) {
            Point point = player.getPosition().add(0, player.getEyeHeight(), 0).add(direction.mul(i));
            Block block = player.getInstance().getBlock(point);
            Material material = block.material();
            if (material == Material.OBSIDIAN || material == Material.BEDROCK) {
                return new Pos(point.blockX(), point.blockY(), point.blockZ());
            }
        }
        return null;
    }

    private Vec horizontalDirection(BedWarsPlayer player) {
        Vec direction = player.getPosition().direction();
        return new Vec(direction.x(), 0, direction.z()).normalize();
    }

    private void explode(Instance instance, Point point, float power, BedWarsPlayer source) {
        if (instance == null) return;
        instance.explode((float) point.x(), (float) point.y(), (float) point.z(), power,
            CompoundBinaryTag.builder()
                .putString("requiredTag", TypeBedWarsGameLoader.PLAYER_PLACED_TAG.getKey())
                .putIntArray("shooter", uuidInts(source.getUuid()))
                .build());
    }

    private int[] uuidInts(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return new int[]{(int) (msb >> 32), (int) msb, (int) (lsb >> 32), (int) lsb};
    }

    private Pos pos(HypixelPosition position) {
        return new Pos(position.x(), position.y(), position.z(), position.pitch(), position.yaw());
    }

    private Pos pos(Vec3i position) {
        return new Pos(position.x(), position.y(), position.z());
    }
}
