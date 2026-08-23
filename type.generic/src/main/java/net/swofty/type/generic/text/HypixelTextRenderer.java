package net.swofty.type.generic.text;

import net.kyori.adventure.text.BlockNBTComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.EntityNBTComponent;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.ScoreComponent;
import net.kyori.adventure.text.SelectorComponent;
import net.kyori.adventure.text.StorageNBTComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.renderer.AbstractComponentRenderer;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class HypixelTextRenderer {

    private static final Renderer RENDERER = new Renderer();

    private HypixelTextRenderer() {
    }

    public static ComponentRenderer<RenderContext> renderer() {
        return RENDERER;
    }

    public static Component render(Component input, RenderContext context) {
        return input == null ? null : RENDERER.render(input, context);
    }

    public static ItemStack renderStack(ItemStack input, RenderContext context) {
        if (input == null || input.isAir()) {
            return input;
        }

        Component name = input.get(DataComponents.CUSTOM_NAME);
        Component renderedName = name == null ? null : RENDERER.render(name, context);

        List<Component> lore = input.get(DataComponents.LORE);
        List<Component> renderedLore = null;
        if (lore != null && !lore.isEmpty()) {
            List<Component> mapped = new ArrayList<>(lore.size());
            boolean loreChanged = false;
            for (Component line : lore) {
                Component rendered = RENDERER.render(line, context);
                loreChanged |= rendered != line;
                mapped.add(rendered);
            }
            if (loreChanged) {
                renderedLore = mapped;
            }
        }

        if (renderedName == name && renderedLore == null) {
            return input;
        }

        ItemStack.Builder builder = input.builder();
        if (renderedName != name) {
            builder.set(DataComponents.CUSTOM_NAME, renderedName);
        }
        if (renderedLore != null) {
            builder.set(DataComponents.LORE, renderedLore);
        }
        return builder.build();
    }

    private static final class Renderer extends AbstractComponentRenderer<RenderContext> {

        @Override
        protected Component renderObject(ObjectComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderBlockNbt(BlockNBTComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderEntityNbt(EntityNBTComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderStorageNbt(StorageNBTComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderKeybind(KeybindComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderScore(ScoreComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderSelector(SelectorComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderText(TextComponent component, RenderContext context) {
            return descend(component, context);
        }

        @Override
        protected Component renderTranslatable(TranslatableComponent component, RenderContext context) {
            String key = component.key();
            if (key.startsWith(Text.CONTEXT_KEY_PREFIX)) {
                return contextValue(component, key.substring(Text.CONTEXT_KEY_PREFIX.length()), context);
            }

            List<TranslationArgument> arguments = component.arguments();
            TranslatableComponent resolved = component;
            if (!arguments.isEmpty()) {
                List<TranslationArgument> mapped = new ArrayList<>(arguments.size());
                boolean changed = false;
                for (TranslationArgument argument : arguments) {
                    if (argument.value() instanceof Component value && !(value instanceof VirtualComponent)) {
                        Component rendered = render(value, context);
                        changed |= rendered != value;
                        mapped.add(TranslationArgument.component(rendered));
                        continue;
                    }
                    mapped.add(argument);
                }
                if (changed) {
                    resolved = component.toBuilder().arguments(mapped).build();
                }
            }
            return descend(resolved, context);
        }

        private Component contextValue(TranslatableComponent placeholder, String tag, RenderContext context) {
            Component resolved = context.resolve(tag);
            if (resolved == null) {
                resolved = Component.text("<missing:" + tag + ">", NamedTextColor.GRAY);
            }
            resolved = resolved.applyFallbackStyle(placeholder.style());

            List<Component> children = placeholder.children();
            if (children.isEmpty()) {
                return resolved;
            }
            List<Component> rendered = new ArrayList<>(children.size());
            for (Component child : children) {
                rendered.add(render(child, context));
            }
            return resolved.append(rendered);
        }

        private Component descend(Component component, RenderContext context) {
            Component result = component;

            HoverEvent<?> hover = result.hoverEvent();
            if (hover != null) {
                HoverEvent<?> renderedHover = hover.withRenderedValue(this, context);
                if (renderedHover != hover) {
                    result = result.hoverEvent(renderedHover);
                }
            }

            List<Component> children = result.children();
            if (children.isEmpty()) {
                return result;
            }

            List<Component> rendered = new ArrayList<>(children.size());
            boolean changed = false;
            for (Component child : children) {
                Component out = render(child, context);
                changed |= out != child;
                rendered.add(out);
            }
            return changed ? result.children(rendered) : result;
        }
    }
}
