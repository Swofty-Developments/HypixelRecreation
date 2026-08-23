package net.swofty.type.lobby.game;

import lombok.Getter;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.lobby.ServerInfoCache;

import java.util.Arrays;
import java.util.List;

@Getter
public enum GameType {
    DISASTERS("Disasters <c><l>RELEASED TO ARCADE!", Material.LAVA_BUCKET, GameType.Category.CASUAL_GAMES,
        null, // arcade lobby
        "<7>From a zombie apocalypse to meteor",
        "<7>showers, work alone or with friends",
        "<7>to survive!"),

    WOOL_GAMES("Wool Games", Material.WHITE_WOOL, Category.COMPETITIVE,
        null, // wool games lobby
        "<7>A series of team-based PvP games",
        "<7>with wool as the theme!"),

    SKYBLOCK("SkyBlock <2><l>0.24.3 <b><l>ABIPHONE CONTACTS & JERRY'S WORKSHOP",
        ItemStacks.head("686718d85e25b006f2c8f160f619b23c8fd6ae75ddf1c06308ec0f539d931703", Text.empty(), List.of()),
        Category.PERSISTENT_GAME,
        ServerType.SKYBLOCK_ISLAND,
        "<7>SkyBlock has finally arrived on",
        "<7>Hypixel! Play with friends (or solo!),",
        "<7>build your private islands and",
        "<7>collect all the items!"),

    BED_WARS("Bed Wars",
        Material.RED_BED,
        Category.TEAM_SURVIVAL,
        ServerType.BEDWARS_LOBBY,
        "<7>Protect your bed along with your",
        "<7>teammates and destroy enemy beds",
        "<7>to win!"),

    SKYWARS("SkyWars <d><l>OLD EMBLEMS + QOL CHANGES",
        Material.ENDER_EYE,
        Category.SURVIVAL,
        ServerType.SKYWARS_LOBBY,
        "<7>Hypixel's take on the SkyWars",
        "<7>gamemode. Featuring the angel of",
        "<7>Death, Soul Well, and <c>INSANE MODE</c>!",
        "<7>Play on your own or in teams."
    ),

    MURDER_MYSTERY("Murder Mystery",
        Material.BOW,
        Category.TEAM_SURVIVAL,
        ServerType.MURDER_MYSTERY_LOBBY,
        "<7>1 Murderer. 1 Detective. And a whole",
        "<7>lot of Innocents. Can you survive",
        "<7>this tense social game of betrayal",
        "<7>and murder?"),

    HOUSING("Housing",
        Material.DARK_OAK_DOOR,
        Category.HOUSING,
        null,
        "<7>Customize and build on your own",
        "<7>personal plot, hang out with your",
        "<7>friends, visit other people's houses,",
        "<7>and more!"),

    THE_TNT_GAMES("The TNT Games",
        Material.TNT,
        Category.CASUAL_GAMES,
        null,
        "<7>Fun minigames with TNT involved!"),

    BUILD_BATTLE("Build Battle",
        Material.CRAFTING_TABLE,
        Category.CASUAL_GAMES,
        null,
        "<7>Create a build based on a theme in",
        "<7>just 5 minutes! Vote on competing",
        "<7>builds with ratings ranging from",
        "<7>\"Super-Poop\" to \"Legendary\". Get",
        "<7>the most votes out of 16 players to",
        "<7>win!"),

    DUELS("Duels", Material.FISHING_ROD, Category.COMPETITIVE,
        null,
        "<7>Quick paced 1v1, 2v2, 4v4!",
        "<f>∙ UHC Duels",
        "<f>∙ SkyWars Duels",
        "<f>∙ The Bridge",
        "<f>∙ Sumo Duels",
        "<f>∙ OP Duels",
        "<f>∙ Classic Duels",
        "<f>∙ NoDebuff Duels",
        "<f>∙ Blitz Duels",
        "<f>∙ Combo Duels",
        "<f>∙ Bow Duels",
        "<f>∙ Spleef Duels",
        "<f>∙ Mega Walls Duels",
        "<f>∙ Boxing Duels",
        "<f>∙ Parkour Duels",
        "<f>∙ Bed Wars Duels",
        "<f>∙ Quakecraft Duels"),

    PROTOTYPE("Prototype <c><l>DISASTERS RELEASED TO ARCADE!",
        Material.ANVIL,
        Category.CASUAL_GAMES,
        ServerType.PROTOTYPE_LOBBY,
        "<7>PTL is a place for testing fun and",
        "<7>creative new minigames and systems",
        "<7>on Hypixel.",
        " ",
        "<c>Everything in this lobby is currently",
        "<c>in development, and may be removed",
        "<c>at any time."),

    RAVENGARD("Ravengard",
        Material.IRON_AXE,
        Category.PERSISTENT_GAME,
        ServerType.RAVENGARD_LOBBY,
        "<7>Battle your way through dangerous",
        "<7>monsters and bosses. Collect",
        "<7>valuable loot and extract before the",
        "<7>dungeon is consumed by the curse!"),
    ;

    private final Text displayName;
    private final ItemStack.Builder item;
    private final Category category;
    private final ServerType lobbyType;
    private final List<Text> lore;

    GameType(String displayName, ItemStack.Builder item, Category category, ServerType lobbyType, String... lore) {
        this.displayName = Text.of(displayName);
        this.item = item;
        this.category = category;
        this.lobbyType = lobbyType;
        this.lore = Arrays.stream(lore).map(Text::of).toList();
    }

    GameType(String displayName, Material item, Category category, ServerType lobbyType, String... lore) {
        this.displayName = Text.of(displayName);
        this.item = ItemStacks.item(item, 1, Text.empty(), List.of());
        this.category = category;
        this.lobbyType = lobbyType;
        this.lore = Arrays.stream(lore).map(Text::of).toList();
    }

    /**
     * Check if this game type is implemented and playable.
     */
    public boolean isImplemented() {
        return lobbyType != null;
    }

    /**
     * Get the total player count for this game type.
     * For SkyBlock, counts all SkyBlock server types.
     * For other games, counts players in the lobby type.
     */
    public int getPlayerCount() {
        if (!isImplemented()) return 0;

        if (this == SKYBLOCK) {
            return ServerInfoCache.getTotalSkyBlockPlayers();
        }

        return ServerInfoCache.getTotalPlayersForType(lobbyType);
    }

    public enum Category {
        PROTOTYPE_GAME,
        PERSISTENT_GAME,
        SURVIVAL,
        TEAM_SURVIVAL,
        COMPETITIVE,
        CASUAL_GAMES,
        HOUSING
    }
}
