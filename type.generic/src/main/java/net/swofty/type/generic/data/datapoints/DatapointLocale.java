package net.swofty.type.generic.data.datapoints;

import lombok.Getter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.generic.data.Datapoint;

import java.util.Locale;

public class DatapointLocale extends Datapoint<DatapointLocale.LocaleType> {
    private static final Serializer<LocaleType> serializer = new Serializer<>() {
        @Override
        public String serialize(LocaleType value) {
            return value.currentLocale.name();
        }

        @Override
        public LocaleType deserialize(String json) {
            return new LocaleType(SupportedLocale.valueOf(json));
        }

        @Override
        public LocaleType clone(LocaleType value) {
            return new LocaleType(value.currentLocale);
        }
    };

    public DatapointLocale(String key, LocaleType value) {
        super(key, value, serializer);
    }

    public DatapointLocale(String key) {
        super(key, null, serializer);
    }

    @Getter
    public static class LocaleType {
        private SupportedLocale currentLocale;

        public LocaleType(SupportedLocale currentLocale) {
            this.currentLocale = currentLocale;
        }

        public void switchTo(SupportedLocale locale) {
            this.currentLocale = locale;
        }
    }

    @Getter
    public enum SupportedLocale {
        UNSET(null, Locale.US, null),
        ENGLISH("English", Locale.US, "4cac9774da1217248532ce147f7831f67a12fdcca1cf0cb4b3848de6bc94b4"),
        PORTUGUESE_BRAZIL("Português do Brasil", Locale.forLanguageTag("pt-BR"), "9668a1fb6af81b231bbcc4de5f7f95803bbd194f5827da027fa70321cf47c"),
        CHINESE_SIMPLIFIED("简体中文", Locale.forLanguageTag("zh-CN"), "7f9bc035cdc80f1ab5e1198f29f3ad3fdd2b42d9a69aeb64de990681800b98dc"),
        CHINESE_TRADITIONAL("繁體中文", Locale.forLanguageTag("zh-TW"), "225b30589fa6f728add52719062da84e8f2f63f7e4889915a29ecd7150214f"),
        CZECH("Čeština", Locale.forLanguageTag("cs-CZ"), "48152b7334d7ecf335e47a4f35defbd2eb6957fc7bfe94212642d62f46e61e"),
        DANISH("Dansk", Locale.forLanguageTag("da-DK"), "10c23055c392606f7e531daa2676ebe2e348988810c15f15dc5b3733998232"),
        DUTCH("Nederlands", Locale.forLanguageTag("nl-NL"), "c23cf210edea396f2f5dfbced69848434f93404eefeabf54b23c073b090adf"),
        FINNISH("Suomi", Locale.forLanguageTag("fi-FI"), "59f2349729a7ec8d4b1478adfe5ca8af96479e983fbad238ccbd81409b4ed"),
        FRENCH("Français", Locale.FRANCE, "9b3495e9dbd5a426e1446e6627bf8dd55d9612ce3b55a8596e112b28db9ea3a"),
        GERMAN("Deutsch", Locale.GERMANY, "5e7899b4806858697e283f084d9173fe487886453774626b24bd8cfecc77b3f"),
        HUNGARIAN("Magyar", Locale.forLanguageTag("hu-HU"), "4a9c3c4b6c5031332dd2bfece5e31e999f8deff55474065cc86993d7bdcdbd0"),
        ITALIAN("Italiano", Locale.ITALY, "a56c5cc17319a6c9ec847252e4d274552d97da95e1085072dba49d117cf3"),
        JAPANESE("日本語", Locale.JAPAN, "d640ae466162a47d3ee33c4076df1cab96f11860f07edb1f0832c525a9e33323"),
        KOREAN("한국어", Locale.KOREA, "fc1be5f12f45e413eda56f3de94e08d90ede8e339c7b1e8f32797390e9a5f"),
        NORWEGIAN("Norsk", Locale.forLanguageTag("nb-NO"), "e0596e165ec3f389b59cfdda93dd6e363e97d9c6456e7c2e123973fa6c5fda"),
        PIRATE("Pirate Speak", Locale.forLanguageTag("en-PT"), "778828ac3a61d8712de5271bb35c4c7146a6b36c6b4e576f5eb8d178da7dfd34"),
        POLISH("Polski", Locale.forLanguageTag("pl-PL"), "921b2af8d2322282fce4a1aa4f257a52b68e27eb334f4a181fd976bae6d8eb"),
        PORTUGUESE("Português", Locale.forLanguageTag("pt-PT"), "ebd51f4693af174e6fe1979233d23a40bb987398e3891665fafd2ba567b5a53a"),
        ROMANIAN("Română", Locale.forLanguageTag("ro-RO"), "dceb1708d5404ef326103e7b60559c9178f3dce729007ac9a0b498bdebe46107"),
        RUSSIAN("Русский", Locale.forLanguageTag("ru-RU"), "16eafef980d6117dabe8982ac4b4509887e2c4621f6a8fe5c9b735a83d775ad"),
        SPANISH("Español", Locale.forLanguageTag("es-ES"), "32bd4521983309e0ad76c1ee29874287957ec3d96f8d889324da8c887e485ea8");

        private final String name;
        private final Locale locale;
        private final String icon;

        SupportedLocale(String name, Locale locale, String icon) {
            this.name = name;
            this.locale = locale;
            this.icon = icon;
        }

        public static SupportedLocale fromLocale(Locale locale) {
            if (locale == null) {
                return ENGLISH;
            }
            for (SupportedLocale sl : values()) {
                if (locale.equals(sl.locale)) {
                    return sl;
                }
            }
            for (SupportedLocale sl : values()) {
                if (sl.locale != null && sl.locale.getLanguage().equals(locale.getLanguage())) {
                    return sl;
                }
            }
            return ENGLISH;
        }
    }
}
