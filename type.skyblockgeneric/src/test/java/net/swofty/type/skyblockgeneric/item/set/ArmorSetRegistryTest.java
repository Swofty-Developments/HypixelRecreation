package net.swofty.type.skyblockgeneric.item.set;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.i18n.HypixelTranslator;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArmorSetRegistryTest {
    @Test
    void everyRegistryEntryCreatesAnArmorSet() {
        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            assertNotNull(registry.create(), registry.name());
        }
    }

    @Test
    void resolvesOverlappingMonsterSets() {
        assertEquals(ArmorSetRegistry.MONSTER_HUNTER, ArmorSetRegistry.getArmorSet(
                ItemType.SPIDER_BOOTS, ItemType.CREEPER_LEGGINGS,
                ItemType.GUARDIAN_CHESTPLATE, ItemType.SKELETON_HELMET));
        assertEquals(ArmorSetRegistry.MONSTER_RAIDER, ArmorSetRegistry.getArmorSet(
                ItemType.TARANTULA_BOOTS, ItemType.CREEPER_LEGGINGS,
                ItemType.GUARDIAN_CHESTPLATE, ItemType.SKELETON_HELMET));
        assertTrue(ArmorSetRegistry.getArmorSets(ItemType.TARANTULA_BOOTS).contains(ArmorSetRegistry.TARANTULA));
        assertTrue(ArmorSetRegistry.getArmorSets(ItemType.TARANTULA_BOOTS).contains(ArmorSetRegistry.MONSTER_RAIDER));
    }

    @Test
    void modelsFullTieredAndPieceAbilitiesSeparately() {
        var sorrow = ArmorSetRegistry.SORROW.create().getEffects().getFirst();
        var sorrowContext = ArmorSetContext.preview(ArmorSetRegistry.SORROW, Set.of());
        assertEquals(ArmorSetBonusType.FULL_SET, sorrow.getType());
        assertEquals(4, sorrow.getRequiredPieces(sorrowContext));

        var mineral = ArmorSetRegistry.MINERAL.create().getEffects().getFirst();
        assertEquals(ArmorSetBonusType.TIERED, mineral.getType());
        assertEquals(1, mineral.getRequiredPieces(ArmorSetContext.preview(ArmorSetRegistry.MINERAL, Set.of())));

        var doubleJump = ArmorSetRegistry.TARANTULA.create().getEffects().stream()
                .filter(effect -> effect.getName().equals("Double Jump"))
                .findFirst().orElseThrow();
        assertEquals(ArmorSetBonusType.ABILITY, doubleJump.getType());
        assertTrue(doubleJump.isRelevantTo(ItemType.TARANTULA_BOOTS));
        assertFalse(doubleJump.isRelevantTo(ItemType.TARANTULA_HELMET));
    }

    @Test
    void usesCurrentYoungBloodValues() {
        String description = ArmorSetRegistry.YOUNG_DRAGON.create().getDescription().getFirst();
        assertTrue(description.contains("<sbstat:speed:+150>"));
        assertTrue(description.contains("Speed cap by 100"));
    }

    @Test
    void rendersOnlyTheCurrentSnorkelingTierAsAStatisticTag() {
        var effect = ArmorSetRegistry.SNORKELING.getEffects().getFirst();
        var context = ArmorSetContext.preview(ArmorSetRegistry.SNORKELING, Set.of(
                ItemType.SNORKELING_BOOTS, ItemType.SNORKELING_LEGGINGS,
                ItemType.SNORKELING_CHESTPLATE, ItemType.SNORKELING_HELMET));

        assertEquals(2, effect.getRequiredPieces(context));
        assertEquals(List.of("Grants <sbstat:respiration:+10>."), effect.getDescription(context));
    }

    @Test
    void everyRegisteredEffectDescriptionUsesValidMiniMessage() {
        MiniMessage miniMessage = MiniMessage.builder().tags(TagResolver.builder()
                .resolver(TagResolver.standard())
                .resolver(HypixelTranslator.SKYBLOCK_STAT_TAG_RESOLVER)
                .build()).build();

        for (ArmorSetRegistry registry : ArmorSetRegistry.values()) {
            var context = ArmorSetContext.preview(registry, Set.copyOf(registry.getItemTypes()));
            for (var effect : registry.create().getEffects()) {
                for (String line : effect.getDescription(context)) {
                    assertDoesNotThrow(() -> miniMessage.deserialize(line), registry + ": " + effect.getName());
                }
            }
        }
    }
}
