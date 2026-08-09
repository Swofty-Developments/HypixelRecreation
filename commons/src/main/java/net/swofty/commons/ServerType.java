package net.swofty.commons;

import lombok.Getter;

@Getter
public enum ServerType {
	SKYBLOCK_ISLAND(true),
	SKYBLOCK_HUB(true),
	SKYBLOCK_SPIDERS_DEN(true),
	SKYBLOCK_THE_END(true),
	SKYBLOCK_CRIMSON_ISLE(true),
	SKYBLOCK_DUNGEON_HUB(true),
	SKYBLOCK_THE_FARMING_ISLANDS(true),
	SKYBLOCK_GOLD_MINE(true),
	SKYBLOCK_DEEP_CAVERNS(true),
	SKYBLOCK_DWARVEN_MINES(true),
	SKYBLOCK_THE_PARK(true),
	SKYBLOCK_GALATEA(true),
	SKYBLOCK_BACKWATER_BAYOU(true),
	SKYBLOCK_JERRYS_WORKSHOP(true),
	SKYBLOCK_GARDEN(true),
	PROTOTYPE_LOBBY(false),
	BEDWARS_LOBBY(false),
	BEDWARS_GAME(false),
	MAIN_LOBBY(false),
	BEDWARS_CONFIGURATOR(false),
	MURDER_MYSTERY_LOBBY(false),
	MURDER_MYSTERY_GAME(false),
	MURDER_MYSTERY_CONFIGURATOR(false),
	SKYWARS_LOBBY(false),
	SKYWARS_GAME(false),
	SKYWARS_CONFIGURATOR(false),
	RAVENGARD_LOBBY(false),
	RAVENGARD_DUNGEON(false),
	REPLAY_VIEWER(false)
	;

	private final boolean isSkyBlock;

	ServerType(boolean isSkyBlock) {
		this.isSkyBlock = isSkyBlock;
	}

	public static boolean isServerType(String type) {
		for (ServerType a : values())
			if (type.equalsIgnoreCase(a.name())) return true;

		return false;
	}

	public static ServerType getSkyblockServer(String name) {
		if (!name.startsWith("SKYBLOCK_")) {
			return valueOf("SKYBLOCK_" + name.toUpperCase());
		} else {
			return valueOf(name);
		}
	}

	public String formatName() {
		return StringUtility.toNormalCase(name());
	}

	public String skyblockName() {
		return name().replace("SKYBLOCK_", "");
	}
}
