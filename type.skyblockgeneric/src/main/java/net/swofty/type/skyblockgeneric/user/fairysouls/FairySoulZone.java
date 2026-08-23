package net.swofty.type.skyblockgeneric.user.fairysouls;

import lombok.Getter;
import net.swofty.commons.ServerType;

@Getter
public enum FairySoulZone {
    HUB(ServerType.SKYBLOCK_HUB),
    DUNGEON_HUB(ServerType.SKYBLOCK_DUNGEON_HUB),
    GOLD_MINE(ServerType.SKYBLOCK_GOLD_MINE),
    DEEP_CAVERNS(ServerType.SKYBLOCK_DEEP_CAVERNS),
    DWARVEN_MINES(ServerType.SKYBLOCK_DWARVEN_MINES),
    THE_FARMING_ISLANDS(ServerType.SKYBLOCK_THE_FARMING_ISLANDS),
    THE_PARK(ServerType.SKYBLOCK_THE_PARK),
    GALATEA(ServerType.SKYBLOCK_GALATEA),
    BACKWATER_BAYOU(ServerType.SKYBLOCK_BACKWATER_BAYOU),
    SPIDERS_DEN(ServerType.SKYBLOCK_SPIDERS_DEN),
    THE_END(ServerType.SKYBLOCK_THE_END),
    CRIMSON_ISLE(ServerType.SKYBLOCK_CRIMSON_ISLE),
    JERRYS_WORKSHOP(ServerType.SKYBLOCK_JERRYS_WORKSHOP),
    LOTUS_ATOLL(null),
    SAFARI(null),
    THE_RIFT(null),
    MISC_DUNGEONS(null),
    MISC_FISHING(null),
    MISC_GARDEN(null),
    MISC_PLACEABLE(null),
    MISC_GLACITE_MINESHAFTS(null),

    ;

    private final ServerType serverType;

    FairySoulZone(ServerType serverType) {
        this.serverType = serverType;
    }
}
