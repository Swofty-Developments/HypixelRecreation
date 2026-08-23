package net.swofty.type.ravengardgeneric.item.components;

import lombok.Getter;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.sound.SoundEvent;
import net.swofty.type.ravengardgeneric.item.RavengardItemComponent;
import net.swofty.type.ravengardgeneric.item.RavengardItemType;

import java.util.List;
import java.util.Map;

/** Marks what kind of item this is; armour types also give the slot it equips into. */
@Getter
public class StandardItemComponent implements RavengardItemComponent {
    private String standardItemType;

    @Override
    public String id() {
        return "STANDARD_ITEM";
    }

    @Override
    public void configure(Map<String, Object> config) {
        Object value = config.get("standard_item_type");
        this.standardItemType = value == null ? null : value.toString().toUpperCase();
    }

    /** Pack tag glyph for this item kind, drawn on the tooltip's first line. */
    public int tagGlyph() {
        if (standardItemType == null) {
            return 0;
        }
        return switch (standardItemType) {
            case "HELMET" -> 0xE20F;
            case "CHESTPLATE" -> 0xE202;
            case "LEGGINGS" -> 0xE212;
            case "BOOTS" -> 0xE209;
            case "SWORD" -> 0xE21A;
            case "SHIELD" -> 0xE219;
            case "BOW" -> 0xE201;
            case "CROSSBOW" -> 0xE206;
            case "DAGGER" -> 0xE207;
            case "GREATAXE" -> 0xE20B;
            case "GREATSWORD" -> 0xE20C;
            case "HALBERD" -> 0xE20D;
            case "HAMMER" -> 0xE20E;
            case "MACE" -> 0xE213;
            case "KNIFE" -> 0xE222;
            case "NECKLACE" -> 0xE216;
            case "EARRING" -> 0xE22B;
            case "BELT" -> 0xE22C;
            case "RING" -> 0xE22D;
            case "TRINKET" -> 0xE21B;
            case "STAFF" -> 0xF237;
            case "ORB" -> 0xF234;
            case "CONSUMABLE" -> 0xE205;
            default -> 0xE224;
        };
    }

    public boolean isShield() {
        return "SHIELD".equals(standardItemType);
    }

    public boolean isTwoHanded() {
        return "HALBERD".equals(standardItemType);
    }

    @Override
    public ItemStack.Builder apply(ItemStack.Builder builder, RavengardItemType type) {
        if (!isShield()) {
            return builder;
        }
        return builder.set(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                0.25f, 1.0f, List.of(), BlocksAttacks.ItemDamageFunction.DEFAULT, null,
                SoundEvent.ITEM_SHIELD_BLOCK, SoundEvent.ITEM_SHIELD_BREAK));
    }

    public EquipmentSlot equipmentSlot() {
        if (standardItemType == null) {
            return null;
        }
        return switch (standardItemType) {
            case "HELMET" -> EquipmentSlot.HELMET;
            case "CHESTPLATE" -> EquipmentSlot.CHESTPLATE;
            case "LEGGINGS" -> EquipmentSlot.LEGGINGS;
            case "BOOTS" -> EquipmentSlot.BOOTS;
            default -> null;
        };
    }
}
