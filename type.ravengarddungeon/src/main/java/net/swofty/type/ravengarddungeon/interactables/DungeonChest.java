package net.swofty.type.ravengarddungeon.interactables;

import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.inventory.Inventories;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DungeonChest extends DungeonInteractable {
    private static final String TITLE_GLYPHS = new String(Character.toChars(0xCFFD0)) + "";
    private static final int LID_TICKS = 5;
    private static final int[] LOOT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 20, 22, 24};

    private final Entity bottom;
    private final Entity lid;
    private final Pos base;
    private final Random random;
    private final String tier;

    private DungeonChest(Instance instance, Pos base, Random random, String tier,
                         Entity bottom, Entity lid) {
        super(instance);
        this.base = base;
        this.random = random;
        this.tier = tier;
        this.bottom = bottom;
        this.lid = lid;
        this.interaction = spawnInteraction(base.sub(0, 0.5, 0), 1.3f, 1.2f);
    }

    public static int convertDecorChests(Instance instance, long seed) {
        List<Entity> bottoms = new ArrayList<>();
        List<Entity> tops = new ArrayList<>();
        for (Entity entity : instance.getEntities()) {
            String model = modelOf(entity);
            if (model == null || !model.contains(":item/gameplay/chests/")) continue;
            if (model.endsWith("_bottom")) bottoms.add(entity);
            else if (model.endsWith("_top")) tops.add(entity);
        }
        int converted = 0;
        for (Entity bottom : bottoms) {
            Entity top = null;
            for (Entity candidate : tops) {
                if (candidate.getPosition().sameBlock(bottom.getPosition())) {
                    top = candidate;
                    break;
                }
            }
            if (top == null) continue;
            tops.remove(top);
            String model = modelOf(bottom);
            String tier = model.contains("/silver") ? "silver"
                    : model.contains("/gold") ? "gold" : "normal";
            Pos base = bottom.getPosition();
            DungeonChest chest = new DungeonChest(instance, base,
                    new Random(seed ^ base.hashCode()), tier, bottom, top);
            InteractableRegistry.register(chest);
            converted++;
        }
        return converted;
    }

    private static String modelOf(Entity entity) {
        if (!(entity.getEntityMeta() instanceof ItemDisplayMeta meta)) return null;
        var stack = meta.getItemStack();
        if (stack == null || stack.isAir()) return null;
        var model = stack.get(DataComponents.ITEM_MODEL);
        return model == null ? null : model.toString();
    }

    @Override
    public String castLabel() {
        return "Opening Chest";
    }

    @Override
    public void open(Player player) {
        opened = true;
        Pos lidHome = lid.getPosition();
        double radians = Math.toRadians(lidHome.yaw());
        Vec back = new Vec(Math.sin(radians), 0, -Math.cos(radians));
        for (int step = 1; step <= LID_TICKS; step++) {
            int tick = step;
            MinecraftServer.getSchedulerManager().buildTask(() ->
                    lid.teleport(lidHome
                            .add(back.mul(0.0625 * tick))
                            .add(0, 0.1375 * tick, 0))
            ).delay(TaskSchedule.tick(step)).schedule();
        }

        Inventory inventory = Inventories.of(InventoryType.CHEST_3_ROW, "<f>{}",
                net.kyori.adventure.text.Component.text(TITLE_GLYPHS)
                        .font(net.swofty.type.ravengardgeneric.gui.RavengardFont.RAVENGARD));
        double distance = Math.hypot(base.x(), base.z());
        double tierBonus = tier.equals("gold") ? 200 : tier.equals("silver") ? 100 : 0;
        int rolls = 2 + random.nextInt(2) + (tier.equals("normal") ? 0 : 1);
        for (int roll = 0; roll < rolls; roll++) {
            inventory.setItemStack(LOOT_SLOTS[random.nextInt(LOOT_SLOTS.length)],
                    DungeonLoot.roll(random, distance + tierBonus));
        }
        InteractableRegistry.watchClose(inventory, closer -> explode());
        MinecraftServer.getSchedulerManager().buildTask(() -> player.openInventory(inventory))
                .delay(TaskSchedule.tick(LID_TICKS + 1)).schedule();
    }

    private void explode() {
        Pos center = base.add(0, 0.4, 0);
        instance.sendGroupedPacket(new ParticlePacket(Particle.POOF,
                center.x(), center.y(), center.z(), 0.4f, 0.4f, 0.4f, 0.02f, 24));
        bottom.remove();
        lid.remove();
        remove();
    }
}
