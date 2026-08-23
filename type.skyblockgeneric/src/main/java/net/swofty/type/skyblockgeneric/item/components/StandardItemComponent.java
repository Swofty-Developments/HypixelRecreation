package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.commons.skyblock.item.PotatoType;
import net.swofty.commons.skyblock.item.reforge.ReforgeType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.List;

@Getter
public class StandardItemComponent extends SkyBlockItemComponent {
    private final StandardItemType type;

    public StandardItemComponent(String type) {
        this.type = StandardItemType.valueOf(type);

        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new ExtraRarityComponent(this.type.getRarityDisplay()));
        addInheritedComponent(new ReforgableComponent(this.type.getReforgeType()));
        addInheritedComponent(new EnchantableComponent(this.type.getEnchantGroups(), true));
        if (this.type.getRuneApplicableTo() != null)
            addInheritedComponent(new RuneableComponent(this.type.getRuneApplicableTo()));
        if (this.type.potatoType != null)
            addInheritedComponent(new HotPotatoableComponent(this.type.getPotatoType()));
    }

    @Getter
    public enum StandardItemType {
        SWORD("SWORD", ReforgeType.SWORDS, List.of(EnchantItemGroups.SWORD), RuneableComponent.RuneApplicableTo.WEAPONS, PotatoType.WEAPONS),
        BOW("BOW", ReforgeType.BOWS, List.of(EnchantItemGroups.BOW), RuneableComponent.RuneApplicableTo.WEAPONS, PotatoType.WEAPONS),
        PICKAXE("PICKAXE", ReforgeType.PICKAXES, List.of(EnchantItemGroups.TOOLS), RuneableComponent.RuneApplicableTo.WEAPONS, null),
        HOE("HOE", ReforgeType.HOES, List.of(EnchantItemGroups.TOOLS), RuneableComponent.RuneApplicableTo.HOES, null),
        HELMET("HELMET", ReforgeType.ARMOR, List.of(EnchantItemGroups.ARMOR), RuneableComponent.RuneApplicableTo.HELMETS, PotatoType.ARMOR),
        CHESTPLATE("CHESTPLATE", ReforgeType.ARMOR, List.of(EnchantItemGroups.ARMOR), RuneableComponent.RuneApplicableTo.CHESTPLATES, PotatoType.ARMOR),
        LEGGINGS("LEGGINGS", ReforgeType.ARMOR, List.of(EnchantItemGroups.ARMOR), RuneableComponent.RuneApplicableTo.LEGGINGS, PotatoType.ARMOR),
        BOOTS("BOOTS", ReforgeType.ARMOR, List.of(EnchantItemGroups.ARMOR), RuneableComponent.RuneApplicableTo.BOOTS, PotatoType.ARMOR),
        NECKLACE("NECKLACE", ReforgeType.EQUIPMENT, List.of(), null, null),
        CLOAK("CLOAK", ReforgeType.EQUIPMENT, List.of(), null, null),
        BELT("BELT", ReforgeType.EQUIPMENT, List.of(), null, null),
        GLOVES("GLOVES", ReforgeType.EQUIPMENT, List.of(), null, null),
        BRACELET("BRACELET", ReforgeType.EQUIPMENT, List.of(), null, null);

        private final String rarityDisplay;
        private final ReforgeType reforgeType;
        private final List<EnchantItemGroups> enchantGroups;
        private final RuneableComponent.RuneApplicableTo runeApplicableTo;
        private final PotatoType potatoType;

        StandardItemType(String rarityDisplay, ReforgeType reforgeType, List<EnchantItemGroups> enchantGroups,
                         RuneableComponent.RuneApplicableTo runeApplicableTo, PotatoType potatoType) {
            this.rarityDisplay = rarityDisplay;
            this.reforgeType = reforgeType;
            this.enchantGroups = enchantGroups;
            this.runeApplicableTo = runeApplicableTo;
            this.potatoType = potatoType;
        }

        public boolean isArmor() {
            return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
        }

        public boolean isEquipment() {
            return this == NECKLACE || this == CLOAK || this == BELT || this == GLOVES || this == BRACELET;
        }
    }
}
