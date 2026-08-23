package net.swofty.type.generic.collectibles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.text.Text;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectibleDescriptionService {

    public static List<Text> resolveLore(CollectibleDefinition definition) {
        String categoryDescriptionKey = definition.categoryDescriptionKey();
        if (categoryDescriptionKey != null && !categoryDescriptionKey.isBlank()) {
            try {
                return Text.keyLines(categoryDescriptionKey, definition.name()).stream()
                    .map(line -> Text.of("<7>{}", line))
                    .toList();
            } catch (IllegalStateException exception) {
                if (!isMissingIterableKey(exception)) {
                    throw exception;
                }

                return List.of(Text.of("<7>{}", Text.key(categoryDescriptionKey, definition.name())));
            }
        }

        if (definition.description().isEmpty()) {
            return List.of();
        }

        return definition.description().stream()
            .map(line -> Text.of("<7>{}", line))
            .toList();
    }

    private static boolean isMissingIterableKey(IllegalStateException exception) {
        String message = exception.getMessage();
        return message != null && message.startsWith("Missing dialogue translation key in en_US:");
    }
}
