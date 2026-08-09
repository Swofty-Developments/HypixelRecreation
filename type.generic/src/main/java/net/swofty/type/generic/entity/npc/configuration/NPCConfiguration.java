package net.swofty.type.generic.entity.npc.configuration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;

public interface NPCConfiguration {

    default String[] holograms(HypixelPlayer player) {
        return new String[0];
    }

    /**
     * Component-native holograms. Existing NPCs can continue implementing
     * {@link #holograms(HypixelPlayer)} while new NPCs avoid legacy formatting.
     */
    default Component[] hologramComponents(HypixelPlayer player) {
        return Arrays.stream(holograms(player))
                .map(line -> LegacyComponentSerializer.legacySection().deserialize(line))
                .toArray(Component[]::new);
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
    default Component chatNameComponent(HypixelPlayer player) {
        String name = chatName(player);
        return name == null ? null : LegacyComponentSerializer.legacySection().deserialize(name);
    }

    default Instance instance() {
        return HypixelConst.getInstanceContainer();
    }

    default Instance instance(HypixelPlayer player) {
        return instance();
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
