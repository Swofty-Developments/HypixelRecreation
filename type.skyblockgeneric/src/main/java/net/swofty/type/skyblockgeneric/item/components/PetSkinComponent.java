package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class PetSkinComponent extends SkyBlockItemComponent {
    private static final Map<ItemType, PetSkinComponent> SKINS = new EnumMap<>(ItemType.class);

    private final ItemType skinItemType;
    private final String skinName;
    private final ItemType applicablePet;
    private final int gemPrice;
    private final PetSkinType skinType;
    private final List<Variant> variants;
    private final int animationDurationTicks;
    private final String swapMessage;

    public PetSkinComponent(ItemType skinItemType, String skinName, ItemType applicablePet, int gemPrice,
                            PetSkinType skinType, List<Variant> variants, int animationDurationTicks,
                            @Nullable String swapMessage) {
        this.skinItemType = skinItemType;
        this.skinName = skinName;
        this.applicablePet = applicablePet;
        this.gemPrice = gemPrice;
        this.skinType = skinType;
        this.variants = List.copyOf(variants);
        this.animationDurationTicks = animationDurationTicks;
        this.swapMessage = swapMessage;

        validate();
        SKINS.put(skinItemType, this);

        addInheritedComponent(new SkullHeadComponent((item) -> getDefaultTexture()));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new ExtraRarityComponent("COSMETIC"));
        addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> getLore(player, item), (item, player) -> {
            Rarity rarity = item.getAttributeHandler().getRarity();
            return Text.of("<color:{}>{}", rarity.getColor(), skinName).serialize();
        }), false));
    }

    public static @Nullable PetSkinComponent get(ItemType skinItemType) {
        return SKINS.get(skinItemType);
    }

    public String getDefaultTexture() {
        return variants.getFirst().defaultTexture();
    }

    public String getItemTexture(SkyBlockItem pet) {
        return skinType == PetSkinType.SELECTABLE
                ? getSelectedVariant(pet).defaultTexture()
                : getDefaultTexture();
    }

    public Variant getSelectedVariant(SkyBlockItem pet) {
        if (skinType != PetSkinType.SELECTABLE) {
            return variants.getFirst();
        }

        String selected = pet.getAttributeHandler().getPetData().getSkinVariant();
        if (selected != null) {
            for (Variant variant : variants) {
                if (selected.equals(variant.name())) {
                    return variant;
                }
            }
        }
        return variants.getFirst();
    }

    public String getTexture(SkyBlockItem pet, long time) {
        Variant variant = switch (skinType) {
            case STATIC, ANIMATED, SELECTABLE -> getSelectedVariant(pet);
            case DAY_NIGHT -> isNight(time) ? variants.get(1) : variants.getFirst();
        };

        if (!variant.isAnimated()) {
            return variant.defaultTexture();
        }

        long cyclePosition = Math.floorMod(time, animationDurationTicks);
        int frame = (int) (cyclePosition * variant.textures().size() / animationDurationTicks);
        return variant.textures().get(frame);
    }

    public boolean isTimeDependent(SkyBlockItem pet) {
        return skinType == PetSkinType.DAY_NIGHT || getSelectedVariant(pet).isAnimated();
    }

    public String formatSwapMessage(Variant variant) {
        return "<l>" + variant.color() + swapMessage + " <r><a>You swapped to the "
                + variant.color() + variant.name() + " <a>skin!";
    }

    public boolean apply(SkyBlockPlayer player, SkyBlockItem skinItem, PetEntityImpl target) {
        if (target.getPlayer() != player) {
            return false;
        }

        SkyBlockItem pet = target.getPet();
        if (pet.getAttributeHandler().getPotentialType() != applicablePet) {
            player.sendMessage("<c>This skin cannot be applied to this pet.");
            return false;
        }

        pet.getAttributeHandler().getPetData().setSkinId(
                skinItem.getAttributeHandler().getPotentialType()
        );
        pet.getAttributeHandler().getPetData().setSkinVariant(
                skinType == PetSkinType.SELECTABLE ? variants.getFirst().name() : null
        );

        player.setItemInHand(null);
        player.sendMessage("<a>Your {} <a>has been applied!", skinItem.getDisplayNameText());
        target.refreshTexture();
        target.refreshName();
        player.playSound(Sound.sound()
                .type(Key.key("minecraft", "entity.experience_orb.pickup"))
                .volume(1f)
                .pitch(1f)
                .build());
        return true;
    }

    private void validate() {
        if (variants.isEmpty()) {
            throw new IllegalArgumentException("Pet skin must define at least one variant");
        }

        int largestFrameCount = 0;
        for (Variant variant : variants) {
            if (variant.textures().isEmpty() || variant.textures().stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Pet skin variants must define non-blank textures");
            }
            largestFrameCount = Math.max(largestFrameCount, variant.textures().size());
        }

        switch (skinType) {
            case STATIC -> {
                if (variants.size() != 1 || variants.getFirst().textures().size() != 1) {
                    throw new IllegalArgumentException("Static pet skins require exactly one texture");
                }
            }
            case ANIMATED -> {
                if (variants.size() != 1 || variants.getFirst().textures().size() < 2) {
                    throw new IllegalArgumentException("Animated pet skins require one variant with at least two textures");
                }
            }
            case SELECTABLE -> validateSelectable();
            case DAY_NIGHT -> {
                if (variants.size() != 2) {
                    throw new IllegalArgumentException("Day/night pet skins require exactly two variants");
                }
            }
        }

        if (largestFrameCount > 1
                && (animationDurationTicks <= 0 || animationDurationTicks < largestFrameCount)) {
            throw new IllegalArgumentException("Animated pet skins require a duration that can display every frame");
        }
    }

    private void validateSelectable() {
        if (swapMessage == null || swapMessage.isBlank()) {
            throw new IllegalArgumentException("Selectable pet skins require a swap message");
        }

        Set<String> names = new HashSet<>();
        for (Variant variant : variants) {
            if (variant.name() == null || variant.name().isBlank()) {
                throw new IllegalArgumentException("Selectable pet skin variants require names");
            }
            if (variant.color() == null || variant.color().isBlank()) {
                throw new IllegalArgumentException("Selectable pet skin variants require colors");
            }
            if (!names.add(variant.name())) {
                throw new IllegalArgumentException("Selectable pet skin variant names must be unique");
            }
        }
    }

    private static boolean isNight(long time) {
        long worldTime = Math.floorMod(time, 24_000);
        return worldTime >= 13_000 && worldTime < 23_000;
    }

    private List<String> getLore(@Nullable SkyBlockPlayer player, SkyBlockItem item) {
        List<String> lore = new ArrayList<>();
        String petDisplayName = applicablePet.getDisplayName();
        String petName = petDisplayName.endsWith(" Pet")
                ? petDisplayName.substring(0, petDisplayName.length() - " Pet".length())
                : petDisplayName;

        lore.add("<8>Consumed on use");

        lore.add(" ");
        lore.add("<7>Pet skins changes the look and");
        lore.add("<7>particle trail of your pet but only");
        lore.add("<7>one skin can be active at a time");

        lore.add(" ");
        lore.add("<7>This skin can only be applied to");
        lore.add(Text.of("<a>{} <7>pets.", petName).serialize());

        lore.add(" ");
        lore.add("<e>Right-click on your summoned pet to");
        lore.add("<e>apply this skin!");

        return lore;
    }

    public enum
    PetSkinType {
        STATIC,
        ANIMATED,
        SELECTABLE,
        DAY_NIGHT
    }

    public record Variant(@Nullable String name, @Nullable String color, List<String> textures) {
        public Variant {
            textures = List.copyOf(textures);
        }

        public String defaultTexture() {
            return textures.getFirst();
        }

        public boolean isAnimated() {
            return textures.size() > 1;
        }
    }
}
