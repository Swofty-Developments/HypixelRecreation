package net.swofty.type.skyblockgeneric.warps;

import lombok.Getter;
import lombok.NonNull;
import net.swofty.commons.ServerType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Getter
public enum TravelScrollIslands {
	PRIVATE_ISLAND("home", "§bPrivate Island",
			"f151cffdaf303673531a7651b36637cad912ba485643158e548d59b2ead5011",
			(unused) -> "Your very own chunk of SkyBlock. Nice housing for your minions.", ServerType.SKYBLOCK_ISLAND),
    GARDEN("garden", "§aThe Garden",
        "7a7d8f7a5f36b6e8d6fbfe2f3ed5de1a260a6ef3c928b5b70d7f0a4f1fb2f6f0",
        (unused) -> "A private farming island with editable plots, visitors, pests, and Garden progression.", ServerType.SKYBLOCK_GARDEN),
	SKYBLOCK_HUB("hub", "§bSkyBlock Hub", "9c465a5d348c53d473f8115ed8923be416f35149f73ebaf5f2b05e13401e814f", (unused) -> "Where everything happens and anything is possible.",
			ServerType.SKYBLOCK_HUB, List.of(TravelScrollType.HUB_CASTLE, TravelScrollType.HUB_MUSEUM, TravelScrollType.HUB_CRYPTS, TravelScrollType.HUB_DARK_AUCTION)),
	GOLD_MINE("gold", "§aGold Mine §7- §bSpawn",
			"d8573ed917444316b0b28dd9927fd68e56f6625fcfa73ad80b8770d5139891b1",
			(unused) -> "Your first stop for extended mining related activities and home to SkyBlock's local janitor Rusty", ServerType.SKYBLOCK_GOLD_MINE, SkillCategories.MINING, 1),
	DEEP_CAVERNS("deep", "§aDeep Caverns §7- §bSpawn",
			"74213dc6dc4b1641defd333f4a4732cc714dd677718fa10f140a6939c12aa32b",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DEEP_CAVERNS, SkillCategories.MINING, 2),
	DWARVEN_MINES("dwarven", "§aDwarven Mines §7- §bSpawn",
			"6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5",
			(unused) -> "An island that gets progressively deeper and contains 6 layers of dangerous mobs and new resources.", ServerType.SKYBLOCK_DWARVEN_MINES, SkillCategories.MINING, 2)
	;

    private final String internalName;
    private final String descriptiveName;
    private final String texture;
    private final Function<Boolean, String> description;
    private final @NonNull ServerType serverType;
    private final List<TravelScrollType> associatedScrolls = new ArrayList<>();

	private final SkillCategories associatedSkill;
	private final Integer islandTier;

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType, SkillCategories associatedSkill, Integer islandTier) {
		this.internalName = internalName;
		this.descriptiveName = descriptiveName;
		this.texture = texture;
		this.description = description;
		this.serverType = serverType;
		this.associatedSkill = associatedSkill;
		this.islandTier = islandTier;
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType) {
		this.internalName = internalName;
		this.descriptiveName = descriptiveName;
		this.texture = texture;
		this.description = description;
		this.serverType = serverType;
		this.associatedSkill = null;
		this.islandTier = null;
	}

	TravelScrollIslands(String internalName, String descriptiveName, String texture, Function<Boolean, String> description, @NonNull ServerType serverType, List<TravelScrollType> associatedScrolls) {
		this.internalName = internalName;
		this.description = description;
		this.serverType = serverType;
		this.texture = texture;
		this.descriptiveName = descriptiveName;
		this.associatedScrolls.addAll(associatedScrolls);
		this.associatedSkill = null;
		this.islandTier = null;
	}

	public static @Nullable TravelScrollIslands getFromType(ServerType type) {
		for (TravelScrollIslands island : values()) {
			if (island.getServerType().equals(type)) {
				return island;
			}
		}
		return null;
	}

	public static @Nullable TravelScrollIslands getFromInternalName(String internalName) {
		for (TravelScrollIslands island : values()) {
			if (island.getInternalName().equalsIgnoreCase(internalName)) {
				return island;
			}
		}
		return null;
	}

	public static @Nullable TravelScrollIslands getFromTravelScroll(TravelScrollType type) {
		for (TravelScrollIslands island : values()) {
			if (island.getAssociatedScrolls().contains(type)) {
				return island;
			}
		}
		return null;
	}

}
