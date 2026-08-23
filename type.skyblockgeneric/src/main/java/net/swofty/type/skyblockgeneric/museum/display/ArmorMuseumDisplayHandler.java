package net.swofty.type.skyblockgeneric.museum.display;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.entity.metadata.other.InteractionMeta;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointMuseum;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplay;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplayEntityImpl;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplayEntityInformation;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplays;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArmorMuseumDisplayHandler extends MuseumDisplay {
    @Override
    public MuseumDisplayEntityInformation display(SkyBlockPlayer player, MuseumDisplays display, boolean empty, int position) {
        DatapointMuseum.MuseumData museumData = player.getMuseumData();
        Pos pos = display.getPositions().get(position);
        PlayerHolograms.ExternalPlayerHologram hologram;
        LivingEntity itemEntity;
        ArrayList<LivingEntity> entities = new ArrayList<>();

        if (empty) {
            hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .text(new String[]{
                            "<7>" + display + " Slot #" + (position + 1),
                            "<e><l>CLICK TO EDIT"
                    })
                    .pos(pos.add(0, 1, 0))
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);

            itemEntity = new MuseumDisplayEntityImpl(EntityType.ARMOR_STAND, display, position, true);
            itemEntity.setAutoViewable(false);
            itemEntity.setNoGravity(true);
            itemEntity.setInvisible(true);
            itemEntity.setInstance(player.getInstance(), pos);
            itemEntity.addViewer(player);
        } else {
            List<SkyBlockItem> items = museumData.getDisplayHandler().getItemsAtSlot(display, position);
            ArmorSetRegistry armorSetRegistry = ArmorSetRegistry.getArmorSet(
                    items.getFirst().getAttributeHandler().getPotentialType()
            );

            @Nullable SkyBlockItem helmet = items.stream().filter(item -> item.getAttributeHandler().getPotentialType() == armorSetRegistry.getHelmet()).findFirst().orElse(null);
            @Nullable SkyBlockItem chestplate = items.stream().filter(item -> item.getAttributeHandler().getPotentialType() == armorSetRegistry.getChestplate()).findFirst().orElse(null);
            @Nullable SkyBlockItem leggings = items.stream().filter(item -> item.getAttributeHandler().getPotentialType() == armorSetRegistry.getLeggings()).findFirst().orElse(null);
            @Nullable SkyBlockItem boots = items.stream().filter(item -> item.getAttributeHandler().getPotentialType() == armorSetRegistry.getBoots()).findFirst().orElse(null);

            String armorSetName = Text.of("<color:{}>{} Set",
                    helmet.getAttributeHandler().getRarity().getColor(),
                    armorSetRegistry.getDisplayName()).serialize();
            hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .text(new String[]{armorSetName})
                    .pos(pos.add(0, 1, 0))
                    .build();
            PlayerHolograms.addExternalPlayerHologram(hologram);

            itemEntity = new MuseumDisplayEntityImpl(EntityType.ARMOR_STAND, display, position, false);
            ArmorStandMeta armorStandMeta = (ArmorStandMeta) itemEntity.getEntityMeta();
            armorStandMeta.setHasArms(true);

            if (helmet != null) {
                itemEntity.setEquipment(EquipmentSlot.HELMET, helmet.getItemStack());
            }
            if (chestplate != null) {
                itemEntity.setEquipment(EquipmentSlot.CHESTPLATE, chestplate.getItemStack());
            }
            if (leggings != null) {
                itemEntity.setEquipment(EquipmentSlot.LEGGINGS, leggings.getItemStack());
            }
            if (boots != null) {
                itemEntity.setEquipment(EquipmentSlot.BOOTS, boots.getItemStack());
            }

            itemEntity.setAutoViewable(false);
            itemEntity.setInvisible(false);
            itemEntity.setInstance(player.getInstance(), pos.add(0, 1, 0));
            itemEntity.addViewer(player);

            LivingEntity interactableEntity = new MuseumDisplayEntityImpl(EntityType.INTERACTION, display, position, false);
            InteractionMeta interactionMeta = (InteractionMeta) interactableEntity.getEntityMeta();
            interactionMeta.setWidth(1);
            interactionMeta.setHeight(2);

            interactableEntity.setAutoViewable(false);
            interactableEntity.setInvisible(false);
            interactableEntity.setInstance(player.getInstance(), pos.add(0, 0.5, 0));
            interactableEntity.addViewer(player);
            entities.add(interactableEntity);
        }

        entities.add(itemEntity);
        return new MuseumDisplayEntityInformation(entities, hologram);
    }
}
