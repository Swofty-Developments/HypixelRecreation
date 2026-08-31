package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.Material;
import net.minestom.server.item.MaterialTags;
import net.minestom.server.registry.TagKey;
import net.swofty.commons.RegistryUtil;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public final class ArmorDyeComponent extends SkyBlockItemComponent {
    private static final Map<EquipmentSlot, TagKey<Material>> ARMOR_TAGS = Map.of(
            EquipmentSlot.HELMET, MaterialTags.HEAD_ARMOR,
            EquipmentSlot.CHESTPLATE, MaterialTags.CHEST_ARMOR,
            EquipmentSlot.LEGGINGS, MaterialTags.LEG_ARMOR,
            EquipmentSlot.BOOTS, MaterialTags.FOOT_ARMOR
    );
    private static final Map<EquipmentSlot, ItemType> LEATHER_TYPES = Map.of(
            EquipmentSlot.HELMET, ItemType.LEATHER_HELMET,
            EquipmentSlot.CHESTPLATE, ItemType.LEATHER_CHESTPLATE,
            EquipmentSlot.LEGGINGS, ItemType.LEATHER_LEGGINGS,
            EquipmentSlot.BOOTS, ItemType.LEATHER_BOOTS
    );

    private final String fromColor;
    private final String toColor;
    private final long animationPeriod;
    private final String displayColor;
    private final boolean specialParticleEffect;

    public ArmorDyeComponent(String itemId, String fromColor, String toColor, long animationPeriod,
                             String displayColor, boolean specialParticleEffect) {
        this.fromColor = normalize(fromColor);
        this.toColor = toColor == null ? null : normalize(toColor);
        this.animationPeriod = animationPeriod;
        this.displayColor = displayColor == null || displayColor.isBlank() ? "<7>" : displayColor;
        this.specialParticleEffect = specialParticleEffect;

        AnvilCombineRegistry.register(itemId, new AnvilCombineHandler(
                (armor, dye) -> {
                    ItemType leatherType = LEATHER_TYPES.get(armor.getMaterial().equipmentSlot());
                    if (leatherType != null && armor.getAttributeHandler().getPotentialType() != leatherType) {
                        armor.getAttribute("item_type").setValue(leatherType.name());
                    }
                    armor.getAttributeHandler().setDyeColor(serializedColor());
                },
                (player, armor, dye) -> isVanillaArmor(armor)
                        && !serializedColor().equals(armor.getAttributeHandler().getDyeColor()),
                (armor, dye, player) -> 0,
                (player, armor, dye) -> {
                    if (player.getBits() < 100) {
                        player.sendMessage("<c>You need at least <b>100 Bits <c>to apply this dye!");
                        return false;
                    }
                    player.removeBits(100);
                    player.sendMessage("<a>Dye applied! <7>(<b>-100 Bits<7>)");
                    return true;
                }
        ));
        addInheritedComponent(new AnvilCombinableComponent(itemId));
        addInheritedComponent(new ExtraRarityComponent("DYE"));
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig(
                (item, player) -> lore(), null), false));
    }

    private List<String> lore() {
        List<String> lore = new ArrayList<>();
        if (toColor == null) {
            lore.add("<7>Can be applied to any vanilla Armor");
            lore.add("<7>piece by combining the two items");
            lore.add("<7>in an Anvil to change the hex");
            lore.add("<7>color of that piece to " + displayColor + fromColor + "<7>.");
        } else {
            lore.add("<7>Can be applied to any vanilla Armor");
            lore.add("<7>piece by combining the two items");
            lore.add("<7>in an Anvil to change the hex");
            lore.add("<7>color of that piece to animate");
            lore.add("<7>between " + displayColor + fromColor + " <7>and "
                    + displayColor + toColor + "<7>.");
        }
        lore.add("");
        lore.add("<7>Costs <b>100 Bits <7>to combine.");
        if (specialParticleEffect) {
            lore.add("");
            lore.add("This dye also displays a <6>special");
            lore.add("<6>particle effect <7>when equipped!");
        }
        return lore;
    }

    private String serializedColor() {
        if (toColor == null) return fromColor;
        return "animated:" + fromColor + ":" + toColor + ":" + animationPeriod;
    }

    private static boolean isVanillaArmor(net.swofty.type.skyblockgeneric.item.SkyBlockItem item) {
        Material material = item.getMaterial();
        ItemType vanillaType = vanillaArmorType(material);
        if (vanillaType == null) return false;

        ItemType potentialType = item.getAttributeHandler().getPotentialType();
        if (potentialType != null) return potentialType == vanillaType;

        String rawType = item.getAttributeHandler().getTypeAsString();
        return rawType.equalsIgnoreCase(material.key().asString())
                || rawType.equalsIgnoreCase(material.key().value());
    }

    private static ItemType vanillaArmorType(Material material) {
        EquipmentSlot slot = material.equipmentSlot();
        TagKey<Material> armorTag = ARMOR_TAGS.get(slot);
        if (armorTag == null || !RegistryUtil.inMaterial(armorTag, material)) return null;
        return ItemType.fromMaterial(material);
    }

    private static String normalize(String color) {
        return color.startsWith("#") ? color.toUpperCase() : "#" + color.toUpperCase();
    }
}
