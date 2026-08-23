package net.swofty.type.skyblockgeneric.warps;

import lombok.AccessLevel;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.warps.unlocks.ScrollUnlockCustomRecipe;
import net.swofty.type.skyblockgeneric.warps.unlocks.ScrollUnlockDarkAuction;
import net.swofty.type.skyblockgeneric.warps.unlocks.ScrollUnlockPurchase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public enum TravelScrollType {
    HUB_CASTLE("castle", "<b>Hub <7>- <b>Castle",
            "Spawn near the ruined castle of the past in the hub.",
            "f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378",
            new ScrollUnlockPurchase("Lonely Philosopher"),
            new Pos(-250, 130, 45, -90, 0),
            Rank.MVP_PLUS),
    HUB_MUSEUM("museum", "<b>Hub <7>- <b>Museum",
            "Spawn in the <9>Museum <7>at the foot of the <b>mountain <7>in the hub.",
            "f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378",
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Museum Rewards."),
            new Pos(-76.5, 76, 80, -90, 0),
            Rank.DEFAULT),
    HUB_DARK_AUCTION("da", "<b>Hub <7>- <5>Dark Auction",
            "Spawn in front of <5>Sirius<7>, the Dark Auction master.",
            "67ae3e2539a442518130370d3b2d3b1837a491ee23d5aff20f015ae284a28c90",
            new ScrollUnlockDarkAuction(),
            new Pos(93.2, 75, 172.8, 22, 0),
            Rank.DEFAULT),
    HUB_CRYPTS("crypts", "<b>Hub <7>- <b>Crypts",
            "Spawn in the <6>crypts <7>below the <c>graveyard <7>in the hub.",
            "f4559d75464b2e40a518e4de8e6cf3085f0a3ca0b1b7012614c4cd96fed60378",
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Recipe Unlock."),
            new Pos(-190, 74, -89, -90, 0),
            Rank.MVP_PLUS),
    THE_PARK("park", "<a>Foraging <7>- <a>Travel Scroll I",
            "Spawn in the Birch Park",
            "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b",
            new ScrollUnlockCustomRecipe("Unlock this scroll's recipe through Foraging Skill Rewards."),
            new Pos(200, 70, 200, 0, 0),
            Rank.DEFAULT)
    ;

    private final @NotNull String internalName;
    @Getter(AccessLevel.NONE)
    private final @NotNull String displayName;
    @Getter(AccessLevel.NONE)
    private final @NotNull String description;
    private final @NotNull String headTexture;
    private final @NotNull ScrollUnlockReason unlockReason;
    private final @NotNull Pos location;
    private final @NotNull Rank requiredRank;

    TravelScrollType(@NotNull String internalName, @NotNull String displayName, @NotNull String description,
                     @NotNull String headTexture, @NotNull ScrollUnlockReason unlockReason, @NotNull Pos location) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
        this.headTexture = headTexture;
        this.unlockReason = unlockReason;
        this.location = location;
        this.requiredRank = Rank.DEFAULT;
    }

    TravelScrollType(@NotNull String internalName, @NotNull String displayName, @NotNull String description,
                     @NotNull String headTexture, @NotNull ScrollUnlockReason unlockReason, @NotNull Pos location,
                     @NotNull Rank requiredRank) {
        this.internalName = internalName;
        this.displayName = displayName;
        this.description = description;
        this.headTexture = headTexture;
        this.unlockReason = unlockReason;
        this.location = location;
        this.requiredRank = requiredRank;
    }

    public @NotNull Text getDisplayName() {
        return Text.of(displayName);
    }

    public @NotNull Text getDescription() {
        return Text.of(description);
    }

    public static @Nullable TravelScrollType getFromInternalName(String internalName) {
        for (TravelScrollType scroll : values()) {
            if (scroll.getInternalName().equalsIgnoreCase(internalName)) {
                return scroll;
            }
        }
        return null;
    }
}
