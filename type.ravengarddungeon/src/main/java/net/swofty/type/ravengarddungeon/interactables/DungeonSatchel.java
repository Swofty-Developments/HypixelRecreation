package net.swofty.type.ravengarddungeon.interactables;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.inventory.Inventories;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class DungeonSatchel extends DungeonInteractable {
    private static final String CLOSED_MODEL = "hypixel_ravengard:item/gameplay/lootbags/lootbag_2";
    private static final String OPEN_MODEL = "hypixel_ravengard:item/gameplay/lootbags/lootbag_2_open";
    private static final String SKULL_MODEL = "hypixel_ravengard:item/gameplay/trinkets/crystal_skull/sprite";
    private static final int[] LOOT_SLOTS = {29, 30, 31, 32, 33};

    private final Entity bag;
    private final Pos base;
    private Task skullTask;

    private DungeonSatchel(Instance instance, Pos base) {
        super(instance);
        this.base = base;
        this.interaction = spawnInteraction(base.sub(0, 0.5, 0), 0.5f, 0.65f);
        this.bag = spawnDisplay(base, CLOSED_MODEL);
    }

    public static DungeonSatchel spawn(Instance instance, Pos position) {
        Pos base = position.withY(groundY(instance, position) + 0.5)
                .withPitch(0f).withYaw(0f);
        DungeonSatchel satchel = new DungeonSatchel(instance, base);
        instance.sendGroupedPacket(new ParticlePacket(Particle.CAMPFIRE_COSY_SMOKE,
                base.x(), base.y(), base.z(), 0.25f, 0.2f, 0.25f, 0.01f, 14));
        satchel.skullTask = MinecraftServer.getSchedulerManager()
                .buildTask(satchel::riseSkull)
                .delay(TaskSchedule.tick(15)).repeat(TaskSchedule.tick(24)).schedule();
        InteractableRegistry.register(satchel);
        return satchel;
    }

    private void riseSkull() {
        if (opened) return;
        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        for (int burst = 0; burst < 3; burst++) {
            double jitterX = random.nextDouble(-0.25, 0.25);
            double jitterZ = random.nextDouble(-0.25, 0.25);
            for (int height = 0; height < 4; height++) {
                int delay = burst * 3 + height * 4 + 1;
                double y = 0.2 + height * 0.28;
                MinecraftServer.getSchedulerManager().buildTask(() -> {
                    if (opened) return;
                    instance.sendGroupedPacket(new ParticlePacket(
                            Particle.DUST.withColor(new net.minestom.server.color.Color(255, 30, 30))
                                    .withScale(0.7f),
                            base.x() + jitterX, base.y() + y, base.z() + jitterZ,
                            0.03f, 0.05f, 0.03f, 0f, 2));
                }).delay(TaskSchedule.tick(delay)).schedule();
            }
        }
    }

    private static double groundY(Instance instance, Pos position) {
        int x = (int) Math.floor(position.x()), z = (int) Math.floor(position.z());
        for (int y = (int) Math.floor(position.y()) + 1; y >= (int) Math.floor(position.y()) - 8; y--) {
            try {
                if (!instance.getBlock(x, y, z).air()) {
                    return y + 1;
                }
            } catch (Exception exception) {
                return position.y();
            }
        }
        return position.y();
    }

    @Override
    public boolean openOnLook() {
        return false;
    }

    @Override
    public String castLabel() {
        return "Searching Body";
    }

    @Override
    public void open(Player player) {
        opened = true;
        if (skullTask != null) {
            skullTask.cancel();
        }
        bag.editEntityMeta(ItemDisplayMeta.class, meta -> meta.setItemStack(modelStack(OPEN_MODEL)));

        Inventory inventory = Inventories.of(InventoryType.CHEST_5_ROW, "Dead Body");
        Random random = ThreadLocalRandom.current();
        double distance = Math.hypot(base.x(), base.z());
        int rolls = 1 + random.nextInt(3);
        for (int roll = 0; roll < rolls; roll++) {
            inventory.setItemStack(LOOT_SLOTS[random.nextInt(LOOT_SLOTS.length)],
                    DungeonLoot.roll(random, distance));
        }
        InteractableRegistry.watchClose(inventory, closer -> {
            instance.sendGroupedPacket(new ParticlePacket(Particle.POOF,
                    base.x(), base.y(), base.z(), 0.25f, 0.25f, 0.25f, 0.02f, 12));
            bag.remove();
            remove();
        });
        player.openInventory(inventory);
    }
}
