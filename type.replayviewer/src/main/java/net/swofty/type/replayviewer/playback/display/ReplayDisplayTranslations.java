package net.swofty.type.replayviewer.playback.display;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.swofty.type.generic.i18n.HypixelTranslator;

@Deprecated // deprecated on arrival, we want to use Components everywhere eventually without rendering them prematurely
final class ReplayDisplayTranslations {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private ReplayDisplayTranslations() {
    }

    static String toLegacy(ComponentLike component) {
        return LEGACY.serialize(GlobalTranslator.render(component.asComponent(), HypixelTranslator.defaultLocale));
    }
}
