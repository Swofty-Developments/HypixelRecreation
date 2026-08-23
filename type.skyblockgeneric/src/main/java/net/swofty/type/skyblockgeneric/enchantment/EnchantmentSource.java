package net.swofty.type.skyblockgeneric.enchantment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import org.jetbrains.annotations.Nullable;

/**
 * The source of the enchantment, for the enchanting guide
 */
@RequiredArgsConstructor
@Getter
public class EnchantmentSource {

    public final String source;
    public final int minLevel, maxLevel;
    @Nullable
    public SourceType sourceType;

    public EnchantmentSource(SourceType sourceType, int minLevel, int maxLevel) {
        this(sourceType.toString(), minLevel, maxLevel);
        this.sourceType = sourceType;
    }
  
		/* It's here you want to use it later, or just delete it and keep using the string constructor
        public EnchantmentSource(Collection collection, int minLevel, int maxLevel) {
            this(collection.getName()+" Collection", minLevel, maxLevel);
        } */

    @Override
    public String toString() {
        String levelString = minLevel == maxLevel
                ? StringUtility.getAsRomanNumeral(minLevel)
                : StringUtility.getAsRomanNumeral(minLevel) + "-" + StringUtility.getAsRomanNumeral(maxLevel);
        return Text.of(" <7>- {} <7>(<a>{}</a>)", source, levelString).serialize();
    }

    /**
     * The most common ways to get enchants used in {@link EnchantmentSource}
     */
    public enum SourceType {
        ENCHANTMENT_TABLE,
        BAZAAR,
        FISHING,
        EXPERIMENTS,
        DARK_AUCTION,
        COMMUNITY_SHOP,
        SKYMART,
        CATACOMBS,
        KUUDRA,
        SEASON_OF_JERRY,
        SCORPIUS;

        @Override
        public String toString() {
            return StringUtility.toNormalCase(name()
                    .replace("[", "")
                    .replace("]", ""));
        }
    }

}