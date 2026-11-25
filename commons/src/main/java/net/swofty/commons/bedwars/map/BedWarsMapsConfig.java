package net.swofty.commons.bedwars.map;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.mc.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuppressWarnings("unused")
public class BedWarsMapsConfig {
    private List<MapEntry> maps;

    @Getter
    @Setter
    public static class MapEntry {
        private String id;
        private String name;
        private MapConfiguration configuration;

        @Getter
        @Setter
        public static class MapConfiguration {
            private List<BedWarsGameType> types;
            private GeneratorSpeed generatorSpeed;
            private MapBounds bounds;
            private Map<TeamKey, MapTeam> teams;
            private MapLocations locations;
            private Map<GlobalGeneratorKey, GlobalGenerator> globalGenerator;
            private List<SprayPosition> sprays;

            @Getter
            @Setter
            public static class MapLocations {
                private HypixelPosition waiting;
                private HypixelPosition spectator;
                private CuboidPosition waitingLobby;
            }

            @Getter
            @Setter
            public static class MapBounds {
                private MinMax x;
                private MinMax y;
                private MinMax z;
            }

            @Getter
            @Setter
            public static class GlobalGenerator {
                private List<HypixelPosition> locations; // maybe Vec3i
            }

        }
    }

    public record TwoBlockPosition(Vec3i feet, Vec3i head) {
    }

    public record CuboidPosition(Vec3i min, Vec3i max) {
    }

    public record SprayPosition(int x, int y, int z, String facing) {
    }

    public record MinMax(double min, double max) {
    }

    public enum GlobalGeneratorKey {
        IRON, GOLD, DIAMOND, EMERALD
    }

    public enum TeamKey {
        RED("Red", NamedTextColor.RED, 0xFF5555, Material.RED_BED, Material.RED_WOOL),
        BLUE("Blue", NamedTextColor.BLUE, 0x5555FF, Material.BLUE_BED, Material.BLUE_BED),
        GREEN("Green", NamedTextColor.GREEN, 0x55FF55, Material.LIME_BED, Material.LIME_WOOL),
        YELLOW("Yellow", NamedTextColor.YELLOW, 0xFFFF55, Material.YELLOW_BED, Material.YELLOW_WOOL),
        AQUA("Aqua", NamedTextColor.AQUA, 0x00AAAA, Material.LIGHT_BLUE_BED, Material.LIGHT_BLUE_WOOL),
        WHITE("White", NamedTextColor.WHITE, 0xFFFFFF, Material.WHITE_BED, Material.WHITE_WOOL),
        PINK("Pink", NamedTextColor.LIGHT_PURPLE, 0xFF55FF, Material.PINK_BED, Material.PINK_WOOL),
        GRAY("Gray", NamedTextColor.GRAY, 0xAAAAAA, Material.GRAY_BED, Material.GRAY_WOOL),
        ;

        @Getter
        private final String name;
        private final TextColor chatColor;
        private final int rgb;
        @NotNull
        private final Material bedMaterial;
        @NotNull
        private final Material woolMaterial;

        TeamKey(@NotNull String name, @NotNull TextColor chatColor, int rgb, @NotNull Material bedMaterial, @NotNull Material woolMaterial) {
            this.name = name;
            this.chatColor = chatColor;
            this.rgb = rgb;
            this.bedMaterial = bedMaterial;
            this.woolMaterial = woolMaterial;
        }

        @NotNull
        public TextColor chatColor() {
            return chatColor;
        }

        public int rgb() {
            return rgb;
        }

        @NotNull
        public Material bedMaterial() {
            return bedMaterial;
        }

        @NotNull
        public Material woolMaterial() {
            return woolMaterial;
        }
    }

    public enum GeneratorSpeed {
        // Base speed: 2 iron every 3 seconds (0.666... per second)
        SLOW(2, 60, 1, 160),           // 2 iron/3s, 1 gold/8s
        MEDIUM(4, 60, 2, 160),         // 4 iron/3s, 2 gold/8s (2x speed)
        FAST(6, 60, 3, 160),           // 6 iron/3s, 3 gold/8s (3x speed)
        SUPER_FAST(7, 60, 3, 160);     // 7 iron/3s, 3 gold/8s (3.3x speed)

        @Getter
        private final int ironAmount;      // items per drop
        @Getter
        private final int ironDelayTicks;  // ticks between drops (60 ticks = 3 seconds)
        @Getter
        private final int goldAmount;      // items per drop
        @Getter
        private final int goldDelayTicks;  // ticks between drops (160 ticks = 8 seconds)

        // These values are consistent among maps
        public final int diamondMax = 4;
        public final int emeraldMax = 2;

        GeneratorSpeed(int ironAmount, int ironDelayTicks, int goldAmount, int goldDelayTicks) {
            this.ironAmount = ironAmount;
            this.ironDelayTicks = ironDelayTicks;
            this.goldAmount = goldAmount;
            this.goldDelayTicks = goldDelayTicks;
        }

        public int getIronDelaySeconds() {
            return ironDelayTicks / 20;
        }

        public int getGoldDelaySeconds() {
            return goldDelayTicks / 20;
        }
    }

    @Getter
    @Setter
    public static class MapTeam {
        private Shops shop;
        private HypixelPosition spawn;
        private TwoBlockPosition bed;
        private HypixelPosition generator;

        public record Shops(HypixelPosition item, HypixelPosition team) {
        }
    }

}
