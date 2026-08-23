package net.swofty.type.ravengardgeneric.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RavengardItems {
    private static final Material BUTTON_MATERIAL = Material.LEATHER_CHESTPLATE;
    private static final ShadowColor LABEL_SHADOW = ShadowColor.shadowColor(4194303);
    private static final ShadowColor ICON_SHADOW = ShadowColor.shadowColor(0);

    private static final Set<DataComponent<?>> HIDDEN = Set.of(
            DataComponents.PAINTING_VARIANT,
            DataComponents.FIREWORKS,
            DataComponents.ATTRIBUTE_MODIFIERS,
            DataComponents.ENCHANTMENTS,
            DataComponents.STORED_ENCHANTMENTS,
            DataComponents.TRIM,
            DataComponents.CHARGED_PROJECTILES,
            DataComponents.JUKEBOX_PLAYABLE,
            DataComponents.MAP_ID,
            DataComponents.UNBREAKABLE,
            DataComponents.WRITTEN_BOOK_CONTENT,
            DataComponents.BANNER_PATTERNS,
            DataComponents.POTION_CONTENTS,
            DataComponents.DYED_COLOR);

    private RavengardItems() {
    }

    public static Builder button(RavengardSprite button) {
        return new Builder(button);
    }

    public static Component label(RavengardSprite button, Text text, int offset) {
        return label(button, text, offset, RavengardFont.DEFAULT);
    }

    public static Component label(RavengardSprite button, Text text, int offset, Key font) {
        return Component.text(button.icon() + RavengardFont.space(-1))
                .font(RavengardFont.RAVENGARD)
                .color(TextColor.color(offset))
                .shadowColor(ICON_SHADOW)
                .decoration(TextDecoration.ITALIC, false)
                .append(text.asComponent()
                        .shadowColor(LABEL_SHADOW)
                        .font(font)
                        .applyFallbackStyle(NamedTextColor.WHITE));
    }

    public static final class Builder {
        private final RavengardSprite button;
        private final List<Text> lore = new ArrayList<>();
        private Text text = Text.empty();
        private String tooltipStyle;
        private int amount = 1;
        private int originSlot;
        private Key font = RavengardFont.DEFAULT;
        private int hoverColorOverride = -1;
        private int footprintWidth = -1;
        private int footprintHeight = -1;

        private Builder(RavengardSprite button) {
            this.button = button;
        }

        /**
         * Renders the whole tooltip in the given font. Hypixel puts unreleased buttons in
         * {@link RavengardFont#ILLAGERALT} so their placeholder text reads as runes.
         */
        public Builder font(Key value) {
            this.font = value;
            return this;
        }

        /**
         * The slot the button's sprite starts at. For a button spanning several slots this stays
         * the top-left one on every copy, so the whole button shares a single hover glow.
         */
        public Builder origin(int slot) {
            this.originSlot = slot;
            return this;
        }

        /**
         * Overrides the computed offset with a captured one. Hypixel authors these per button per
         * menu -- they usually line up with the slot grid, but not always (the select-class statues
         * sit at a fixed height regardless of which row their click slot is on), so a captured
         * value always wins over {@link RavengardSprite#hoverColor(int)}.
         */
        public Builder hoverColor(int value) {
            this.hoverColorOverride = value;
            return this;
        }

        private int offset() {
            return hoverColorOverride >= 0 ? hoverColorOverride : button.hoverColor(originSlot);
        }

        public RavengardSprite sprite() {
            return button;
        }

        public Builder footprint(int width, int height) {
            this.footprintWidth = width;
            this.footprintHeight = height;
            return this;
        }

        public int[] coveredSlots(int slot) {
            if (footprintWidth < 0) {
                return button.coveredSlots(slot);
            }
            int[] slots = new int[footprintWidth * footprintHeight];
            int index = 0;
            for (int row = 0; row < footprintHeight; row++) {
                for (int column = 0; column < footprintWidth; column++) {
                    slots[index++] = slot + row * RavengardSprite.ROW_WIDTH + column;
                }
            }
            return slots;
        }

        public Builder label(String value) {
            this.text = Text.read(value);
            return this;
        }

        public Builder label(Text value) {
            this.text = value;
            return this;
        }

        public Builder amount(int value) {
            this.amount = value;
            return this;
        }

        public Builder tooltipStyle(String value) {
            this.tooltipStyle = value;
            return this;
        }

        public Builder lore(String... lines) {
            for (String line : lines) {
                lore.add(Text.read(line));
            }
            return this;
        }

        public Builder lore(Text... lines) {
            lore.addAll(List.of(lines));
            return this;
        }

        public Builder lore(List<Text> lines) {
            lore.addAll(lines);
            return this;
        }

        public Builder blankLine() {
            lore.add(Text.empty());
            return this;
        }

        public ItemStack build() {
            return toBuilder().build();
        }

        public ItemStack.Builder toBuilder() {
            ItemStack.Builder builder = ItemStack.builder(BUTTON_MATERIAL)
                    .amount(amount)
                    .set(DataComponents.ITEM_MODEL, button.itemModel())
                    .set(DataComponents.CUSTOM_NAME, RavengardItems.label(button, text, offset(), font))
                    // item.vsh forwards the dye colour as the button face's screen offset, the same
                    // encoding the hover glow reads off custom_name. Both must carry it: custom_name
                    // places the lit sprite, dyed_color places the unlit one.
                    .set(DataComponents.DYED_COLOR, TextColor.color(offset()))
                    .set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, HIDDEN));

            if (!lore.isEmpty()) {
                List<Component> lines = new ArrayList<>(lore.size());
                for (Text line : lore) {
                    lines.add(line.asComponent().decoration(TextDecoration.ITALIC, false).font(font));
                }
                builder.set(DataComponents.LORE, lines);
            }
            if (tooltipStyle != null) {
                builder.set(DataComponents.TOOLTIP_STYLE, tooltipStyle);
            }
            return builder;
        }
    }
}
