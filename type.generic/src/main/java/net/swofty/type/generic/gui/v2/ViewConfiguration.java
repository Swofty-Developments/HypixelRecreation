package net.swofty.type.generic.gui.v2;

import lombok.Getter;
import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.context.ViewContext;

import java.util.function.BiFunction;

@Getter
public class ViewConfiguration<S> {

    private final BiFunction<S, ViewContext, Text> titleFunction;
    private final InventoryType inventoryType;

    public ViewConfiguration(String titleMarkup, InventoryType type, Object... arguments) {
        Text title = Text.of(titleMarkup, arguments);
        this.titleFunction = (_, _) -> title;
        this.inventoryType = type;
    }

    public ViewConfiguration(Text title, InventoryType type) {
        this.titleFunction = (_, _) -> title;
        this.inventoryType = type;
    }

    public ViewConfiguration(StringTitle<S> title, InventoryType type) {
        this.titleFunction = (s, c) -> Text.of(title.getTitle(s, c));
        this.inventoryType = type;
    }

    public ViewConfiguration(TextTitle<S> title, InventoryType type) {
        this.titleFunction = title::getTitle;
        this.inventoryType = type;
    }

    public static <S> ViewConfiguration<S> withString(StringTitle<S> title, InventoryType type) {
        return new ViewConfiguration<>(title, type);
    }

    public static <S> ViewConfiguration<S> withText(TextTitle<S> title, InventoryType type) {
        return new ViewConfiguration<>(title, type);
    }

    public static <S> ViewConfiguration<S> translatable(String titleKey, InventoryType type) {
        return new ViewConfiguration<>((TextTitle<S>) (_, _) -> Text.key(titleKey), type);
    }

    @FunctionalInterface
    public interface StringTitle<S> {
        String getTitle(S state, ViewContext ctx);
    }

    @FunctionalInterface
    public interface TextTitle<S> {
        Text getTitle(S state, ViewContext ctx);
    }
}
