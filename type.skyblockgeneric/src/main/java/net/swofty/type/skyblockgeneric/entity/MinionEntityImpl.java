package net.swofty.type.skyblockgeneric.entity;

import lombok.Getter;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionSkinComponent;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.SkyBlockMinion;
import net.swofty.type.skyblockgeneric.minion.extension.extensions.MinionSkinExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class MinionEntityImpl extends LivingEntity {
    @Getter
    private static final List<MinionEntityImpl> activeMinions = Collections.synchronizedList(new ArrayList<>());

    private final SkyBlockMinion minion;
    private IslandMinionData.IslandMinion islandMinion;

    public MinionEntityImpl(IslandMinionData.IslandMinion islandMinion, SkyBlockMinion minion) {
        super(EntityType.ARMOR_STAND);

        this.collidesWithEntities = false;
        this.hasPhysics = false;

        this.islandMinion = islandMinion;
        this.minion = minion;

        setAutoViewable(true);
        setAutoViewEntities(true);

        ArmorStandMeta meta = (ArmorStandMeta) this.entityMeta;
        meta.setSmall(true);
        meta.setHasNoBasePlate(true);
        meta.setHasNoGravity(true);
        meta.setHasArms(true);

        setInvisible(false);
    }

    public void updateMinionDisplay(IslandMinionData.IslandMinion updatedMinion) {
        islandMinion = updatedMinion;

        int tier = islandMinion.getTier();
        SkyBlockMinion.MinionTier minionTier = minion.getTiers().get(tier - 1);

        MinionSkinExtension skinExtension = (MinionSkinExtension) updatedMinion.getExtensionData().getOfType(MinionSkinExtension.class);

        if (skinExtension.getItemTypePassedIn() != null) {
            MinionSkinComponent skinItem = new SkyBlockItem(skinExtension.getItemTypePassedIn()).getComponent(MinionSkinComponent.class);

            setHelmet(skinItem.getHelmetStack());
            setBoots(skinItem.getBootsStack());
            setLeggings(skinItem.getLeggingsStack());
            setChestplate(skinItem.getChestplateStack());
        } else {
            setHelmet(ItemStacks.head(minionTier.texture(), "").build());
            setBoots(ItemStack.builder(Material.LEATHER_BOOTS).set(DataComponents.DYED_COLOR,
                    minion.getBootColour()).build());
            setLeggings(ItemStack.builder(Material.LEATHER_LEGGINGS).set(DataComponents.DYED_COLOR,
                    minion.getBootColour()).build());
            setChestplate(ItemStack.builder(Material.LEATHER_CHESTPLATE).set(DataComponents.DYED_COLOR,
                    minion.getBootColour()).build());
        }

        try {
            setItemInMainHand(ItemStack.builder(minionTier.heldItem()).build());
        } catch (Exception e) {
            org.tinylog.Logger.warn(e, "Failed to set held item for minion tier {}", minionTier);
        }
    }

    @Override
    public void spawn() {
        super.spawn();

        activeMinions.add(this);
        updateMinionDisplay(islandMinion);
    }

    @Override
    public void remove() {
        super.remove();
        activeMinions.remove(this);
    }

    public void swingAnimation() {
        ArmorStandMeta meta = (ArmorStandMeta) this.entityMeta;

        for (int rotation = 0; rotation < 90; rotation = rotation + 9) {
            int delay = rotation / 9;
            int inverseDelay = 11 - delay;

            int finalRotation = rotation;
            ScheduleUtility.delay(() -> {
                meta.setRightArmRotation(new Vec(360 - finalRotation,0, 0));
            }, delay + 1);

            ScheduleUtility.delay(() -> {
                meta.setRightArmRotation(new Vec(360 - finalRotation,0, 0));
            }, 10 + inverseDelay);
        }
    }

    public void placeAnimation() {
        ArmorStandMeta meta = (ArmorStandMeta) this.entityMeta;

        for (int rotation = 0; rotation < 90; rotation = rotation + 9) {
            int delay = rotation / 9;
            int inverseDelay = 11 - delay;

            int finalRotation = rotation;
            ScheduleUtility.delay(() -> {
                meta.setRightArmRotation(new Vec(360 - finalRotation,0, 0));
                meta.setLeftArmRotation(new Vec(360 - finalRotation,0, 0));
            }, delay + 1);

            ScheduleUtility.delay(() -> {
                meta.setRightArmRotation(new Vec(360 - finalRotation,0, 0));
                meta.setLeftArmRotation(new Vec(360 - finalRotation,0, 0));
            }, 10 + inverseDelay);
        }
    }
}
