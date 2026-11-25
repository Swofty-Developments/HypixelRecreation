package io.github.term4.polyp.mechanics.attribute;

import io.github.term4.polyp.mechanics.attribute.catalog.enchant.AquaAffinity;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Efficiency;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Worn/held enchant modifiers push and clear off {@code EntityEquipEvent} (mob lifecycle). */
class ArmorEnchantTest extends HeadlessServerTest {

    private static ItemStack aquaHelmet() {
        return enchanted(Material.DIAMOND_HELMET, AquaAffinity.KEY, 1);
    }

    private static boolean hasAquaModifier(LivingEntity e) {
        return e.getAttribute(Attribute.SUBMERGED_MINING_SPEED).modifiers().stream()
                .anyMatch(m -> m.operation() == AttributeOperation.ADD_MULTIPLIED_TOTAL && Math.abs(m.amount() - 4.0) < 1e-9);
    }

    @Test
    void aquaAffinityPushesWhileWornAndClearsOnRemoval() {
        LivingEntity e = zombie(new Pos(0, 64, 90));
        assertFalse(hasAquaModifier(e), "no helmet yet");
        e.setEquipment(EquipmentSlot.HELMET, aquaHelmet());
        assertTrue(hasAquaModifier(e), "Aqua Affinity should push submerged_mining_speed while worn");
        e.setEquipment(EquipmentSlot.HELMET, ItemStack.AIR);
        assertFalse(hasAquaModifier(e), "removing the helmet should clear the push");
    }

    @Test
    void plainHelmetPushesNothing() {
        LivingEntity e = zombie(new Pos(0, 64, 91));
        e.setEquipment(EquipmentSlot.HELMET, ItemStack.of(Material.DIAMOND_HELMET));
        assertFalse(hasAquaModifier(e), "an unenchanted helmet contributes nothing");
    }

    private static boolean hasMiningEfficiency(LivingEntity e) {
        var inst = e.getAttribute(Attribute.MINING_EFFICIENCY);
        return inst != null && inst.modifiers().stream()
                .anyMatch(m -> m.operation() == AttributeOperation.ADD_VALUE && Math.abs(m.amount() - 10.0) < 1e-9);
    }

    @Test
    void efficiencyPushesMiningEfficiencyWhileHeld() {
        LivingEntity e = zombie(new Pos(0, 64, 92));
        ItemStack pick = enchanted(Material.DIAMOND_PICKAXE, Efficiency.KEY, 3);
        assertFalse(hasMiningEfficiency(e), "no tool yet");
        e.setEquipment(EquipmentSlot.MAIN_HAND, pick);
        assertTrue(hasMiningEfficiency(e), "Efficiency III pushes mining_efficiency (3²+1) while the tool is held");
        e.setEquipment(EquipmentSlot.MAIN_HAND, ItemStack.AIR);
        assertFalse(hasMiningEfficiency(e), "switching off the tool clears it");
    }
}
