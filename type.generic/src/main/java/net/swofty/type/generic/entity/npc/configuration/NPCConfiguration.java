package net.swofty.type.generic.entity.npc.configuration;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface NPCConfiguration {

    default String[] holograms(HypixelPlayer player) {
        return new String[0];
    }

    default List<Text> hologramTexts(HypixelPlayer player) {
        return Arrays.stream(holograms(player))
                .map(Text::read)
                .toList();
    }

    Pos position(HypixelPlayer player);

    default boolean looking(HypixelPlayer player) {
        return false;
    }

    default boolean visible(HypixelPlayer player) {
        return true;
    }

    @Nullable
    default String chatName(HypixelPlayer player) {
        return null;
    }

    @Nullable
    default Text chatNameText(HypixelPlayer player) {
        String name = chatName(player);
        return name == null ? null : Text.read(name);
    }

    default Instance instance() {
        return HypixelConst.getInstanceContainer();
    }

    default EntityPose pose(HypixelPlayer player) {
        return EntityPose.STANDING;
    }

    default boolean shouldDisplayHolograms(HypixelPlayer player) {
        return true;
    }

    @Nullable
    default Map<EquipmentSlot, ItemStack> equipment(HypixelPlayer player) {
        return null;
    }
}
