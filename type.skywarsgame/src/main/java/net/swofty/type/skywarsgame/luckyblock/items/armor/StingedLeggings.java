package net.swofty.type.skywarsgame.luckyblock.items.armor;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockArmor;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class StingedLeggings implements LuckyBlockArmor {

    public static final String ID = "stinged_leggings";
    private static final int TRAIL_TICK_INTERVAL = 10;
    private static final double POISON_RADIUS = 2.0;
    private static final int POISON_DURATION_TICKS = 60;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Stinged Leggings";
    }

    @Override
    public EquipmentSlot getSlot() {
        return EquipmentSlot.LEGGINGS;
    }

    @Override
    public Material getBaseMaterial() {
        return Material.LEATHER_LEGGINGS;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.LEATHER_LEGGINGS, """
                        <d>Stinged Leggings

                        <7>Protection II

                        <7>Leaves a <2>poison cloud<7> behind you!

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(DataComponents.DYED_COLOR, new Color(255, 0, 255))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public void onEquip(SkywarsPlayer player) {
        player.sendMessage("<2>Your legs ooze with poison!");
    }

    @Override
    public void onWornTick(SkywarsPlayer player) {
        if (player.getAliveTicks() % TRAIL_TICK_INTERVAL != 0) {
            return;
        }

        Instance instance = player.getInstance();
        if (instance == null) {
            return;
        }

        Pos pos = player.getPosition();

        ParticlePacket packet = new ParticlePacket(
                Particle.ENTITY_EFFECT,
                false,
                false,
                pos.x(), pos.y() + 0.5, pos.z(),
                0.5f, 0.3f, 0.5f,
                0.0f,
                10
        );
        instance.sendGroupedPacket(packet);

        for (Entity entity : instance.getNearbyEntities(pos, POISON_RADIUS)) {
            if (entity instanceof SkywarsPlayer target && target != player) {
                target.addEffect(new Potion(PotionEffect.POISON, (byte) 0, POISON_DURATION_TICKS));
            }
        }
    }

    @Override
    public boolean hasTrailEffect() {
        return true;
    }
}
