package net.swofty.type.bedwarsgame.user;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.network.packet.server.play.DestroyEntitiesPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.tag.Tag;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsGameStat;
import net.swofty.type.game.game.GameParticipant;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointHypixelExperience;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Represents a player in the BedWars game mode.
 * This class extends HypixelPlayer with BedWars-specific state and presentation.
 */
public class BedWarsPlayer extends HypixelPlayer implements GameParticipant {

	@Setter
	private boolean shouldShowTrueIdentity = false;
	@Getter
	private final UUID fakeUuid;
	private final String hiddenUsername;

	public BedWarsPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
		super(playerConnection, gameProfile);
		getAttribute(Attribute.ATTACK_SPEED).setBaseValue(1000); // basically removes the attack indicator
		fakeUuid = UUID.randomUUID();
		hiddenUsername = "§k" + fakeUuid.toString().substring(0, 14);
	}

	@Override
	public @Nullable String getGameId() {
		return getTag(Tag.String("gameId"));
	}

	@Override
	public void setGameId(final @NotNull String gameId) {
		if (gameId == null) {
			removeTag(Tag.String("gameId"));
		} else {
			setTag(Tag.String("gameId"), gameId);
		}
	}

	public void reveal() {
		shouldShowTrueIdentity = true;
		for (Player viewer : getViewers()) refreshIdentityFor(viewer);
		refreshIdentityFor(this);
	}

	private void refreshIdentityFor(Player viewer) {
		if (viewer == this) {
			viewer.sendPackets(new PlayerInfoRemovePacket(fakeUuid), getPrivatePlayerInfo());
			return;
		}
		viewer.sendPackets(new DestroyEntitiesPacket(getEntityId()), new PlayerInfoRemovePacket(fakeUuid));
		updateNewViewer(viewer);
	}

	@Override
	public void updateNewViewer(@NonNull Player player) {
		if (player != this) player.sendPacket(getAddPlayerToList());
		super.updateNewViewer(player);
	}

	@Override
	protected SpawnEntityPacket getSpawnPacket() {
		return new SpawnEntityPacket(getEntityId(), fakeUuid, EntityType.PLAYER,
			getPosition(), 0, 0, Vec.ZERO);
	}

	@Override
	protected @NonNull PlayerInfoUpdatePacket getAddPlayerToList() {
		return new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, getPublicPlayerInfo());
	}

	@Override
	protected PlayerInfoRemovePacket getRemovePlayerToList() {
		return new PlayerInfoRemovePacket(fakeUuid);
	}

	private PlayerInfoUpdatePacket getPrivatePlayerInfo() {
		final PlayerSkin skin = getSkin();
		List<PlayerInfoUpdatePacket.Property> prop = skin != null ?
			List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
			List.of();
		byte hatIndex = ((MetadataDef.Entry.BitMask) MetadataDef.Player.IS_HAT_ENABLED).bitMask();

		return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
			List.of(new PlayerInfoUpdatePacket.Entry(getUuid(), getUsername(), prop,
				false, getLatency(), getGameMode(), getDisplayName(), null, 0, (getSettings().displayedSkinParts() & hatIndex) == hatIndex)));
	}

	private PlayerInfoUpdatePacket.Entry getPublicPlayerInfo() {
		if (shouldShowTrueIdentity) {
			final PlayerSkin skin = getSkin();
			List<PlayerInfoUpdatePacket.Property> properties = skin != null ?
				List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
				List.of();
			byte hatIndex = ((MetadataDef.Entry.BitMask) MetadataDef.Player.IS_HAT_ENABLED).bitMask();
			return new PlayerInfoUpdatePacket.Entry(
				fakeUuid,
				getUsername(),
				properties,
				false,
				getLatency(),
				getGameMode(),
				getDisplayName(),
				null,
				0,
				(getSettings().displayedSkinParts() & hatIndex) == hatIndex
			);
		}

		return new PlayerInfoUpdatePacket.Entry(
			fakeUuid,
			hiddenUsername,
			List.of(),
			false,
			0,
			GameMode.SURVIVAL,
			Text.legacy(hiddenUsername).asComponent(),
			null,
			1,
			false
		);
	}

	public Player getServerPlayer() {
		return this;
	}

	public BedWarsDataHandler getBedWarsDataHandler() {
		return BedWarsDataHandler.getUser(this.getUuid());
	}

	@Nullable
	public String getTeamName() {
		return getTag(Tag.String("team"));
	}

	public void setTeamName(@NotNull BedWarsMapsConfig.TeamKey teamKey) {
		setTag(Tag.String("team"), teamKey.name());
	}

	// TODO: Optional<TeamKey>
	@Nullable
	public BedWarsMapsConfig.TeamKey getTeamKey() {
		String teamName = getTeamName();
		return teamName == null ? null : BedWarsMapsConfig.TeamKey.valueOf(teamName);
	}

	public BedWarsGame getGame() {
		final String gameId = getTag(Tag.String("gameId"));
		return TypeBedWarsGameLoader.getGameById(gameId);
	}

	public boolean allowsPersistentProgress() {
		BedWarsGame game = getGame();
		return game != null && game.getGameType().allowsPersistentProgress();
	}

	public void xp(ExperienceCause cause) {
		getGame().getGameStats().add(getUuid(), BedWarsGameStat.BED_WARS_EXPERIENCE, cause.getExperience());

		sendMessage("<b>+{} Bed Wars XP ({})", cause.getExperience(), cause.getFormattedName());
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		dp.setValue(dp.getValue() + cause.getExperience());

		setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
		setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));
	}

	public void xp(ExperienceCause cause, long units) {
		long amount = cause.calculateXp(units);
		getGame().getGameStats().add(getUuid(), BedWarsGameStat.BED_WARS_EXPERIENCE, amount);

		sendMessage("<b>+{} Bed Wars XP ({})", amount, cause.getFormattedName());
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		dp.setValue(dp.getValue() + amount);

		setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
		setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));
	}

	public void hypixelXp(long amount) {
		getGame().getGameStats().add(getUuid(), BedWarsGameStat.HYPIXEL_EXPERIENCE, amount);
		sendMessage("<b>+{} Hypixel Experience", amount);
		DatapointHypixelExperience dp = getDataHandler().get(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE, DatapointHypixelExperience.class);
		dp.setValue(dp.getValue() + amount);
	}

	public void token(TokenCause cause) {
		getGame().getGameStats().add(getUuid(), BedWarsGameStat.TOKENS, cause.getExperience());
		sendMessage("<2>+{} Tokens ({})", cause.getExperience(), cause.getFormattedName());
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class);
		dp.setValue(dp.getValue() + cause.getExperience());
	}

	public long getCurrentBedWarsExperience() {
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		return dp.getValue();
	}

	public long getCurrentBedWarsLevel() {
		return BedwarsLevelUtil.calculateLevel(getCurrentBedWarsExperience());
	}
}
