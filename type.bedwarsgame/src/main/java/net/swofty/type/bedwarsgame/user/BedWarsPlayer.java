package net.swofty.type.bedwarsgame.user;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.chunk.ChunkCache;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.pvp.player.CombatPlayer;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
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
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

/**
 * Represents a player in the BedWars game mode.
 * This class extends HypixelPlayer and implements CombatPlayer for combat-related functionalities.
 * CombatPlayer implementation based on <a href="https://github.com/TogAr2/MinestomPvP/blob/master/src/main/java/io/github/togar2/pvp/player/CombatPlayerImpl.java">CombatPlayerImpl</a>
 */
@SuppressWarnings("UnstableApiUsage")
public class BedWarsPlayer extends HypixelPlayer implements CombatPlayer, GameParticipant {

	private boolean velocityUpdate = false;
	private PhysicsResult previousPhysicsResult = null;
	@Getter
	private long xpThisGame = 0;
	@Getter
	private long tokensThisGame = 0;
	@Getter
	private long hypixelXpThisGame = 0;
	@Getter
	@Setter
	private boolean shouldShowTrueIdentity = false;
	@Getter
	private UUID fakeUuid;

	public BedWarsPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
		super(playerConnection, gameProfile);
		getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(1.0);
		getAttribute(Attribute.ATTACK_SPEED).setBaseValue(1000); // basically removes the attack indicator
		fakeUuid = UUID.randomUUID();
	}

	@Override
	public @Nullable String getGameId() {
		return getTag(Tag.String("gameId"));
	}

	@Override
	public void setGameId(final @NotNull String gameId) {
		if (gameId == null) {
			removeTag(Tag.String("gameId"));
			resetTrackable();
		} else {
			setTag(Tag.String("gameId"), gameId);
		}
	}

	public void reveal() {
		shouldShowTrueIdentity = true;
		setSkin(getSkin());
	}

	public void updateBelowTag() {
		if (belowNameTag == null)
			setBelowNameTag("health", "<c>❤");
		int health = (int) (getHealth() + getAdditionalHearts());
		belowNameTag.updateScore(this, health);

		BedWarsGame game = getGame();
		if (game != null && game.getReplayManager().isRecording()) {
			game.getReplayManager().recordBelowNameTag(this, health);
		}
	}

	@Override
	public void updateNewViewer(@NonNull Player player) {
		if (!shouldShowTrueIdentity) {
			player.sendPackets(
				new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
					new PlayerInfoUpdatePacket.Entry(
						fakeUuid,
						"§k" + fakeUuid.toString().substring(0, new Random().nextInt(10) + 4),
						List.of(),
						false,
						0,
						GameMode.SURVIVAL,
						Component.text(fakeUuid.toString().substring(0, 12)),
						null,
						1, false)),
				new SpawnEntityPacket(this.getEntityId(), fakeUuid, EntityType.PLAYER,
					getPosition(),
					(float) 0,
					0,
					Vec.ZERO),
				new EntityHeadLookPacket(getEntityId(), getPosition().yaw())
			);
			return;
		}
		super.updateNewViewer(player);
	}

	@Override
	protected @NonNull PlayerInfoUpdatePacket getAddPlayerToList() {
		if (!shouldShowTrueIdentity) {
			return new PlayerInfoUpdatePacket(EnumSet.of(
				PlayerInfoUpdatePacket.Action.ADD_PLAYER),
				List.of(new PlayerInfoUpdatePacket.Entry(fakeUuid, "§k" + fakeUuid.toString().substring(0, 14), List.of(),
					false, getLatency(), getGameMode(), Component.text(fakeUuid.toString().substring(0, 12)), null, 0, false)));
		}
		final PlayerSkin skin = getSkin();
		List<PlayerInfoUpdatePacket.Property> prop = skin != null ?
			List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
			List.of();
		byte hatIndex = ((MetadataDef.Entry.BitMask) MetadataDef.Player.IS_HAT_ENABLED).bitMask();

		return new PlayerInfoUpdatePacket(EnumSet.of(PlayerInfoUpdatePacket.Action.ADD_PLAYER, PlayerInfoUpdatePacket.Action.UPDATE_LISTED),
			List.of(new PlayerInfoUpdatePacket.Entry(getUuid(), getUsername(), prop,
				false, getLatency(), getGameMode(), getDisplayName(), null, 0, (getSettings().displayedSkinParts() & hatIndex) == hatIndex)));
	}

	public void resetTrackable() {
		xpThisGame = 0;
		tokensThisGame = 0;
		hypixelXpThisGame = 0;
	}

	@Override
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
		return BedWarsMapsConfig.TeamKey.valueOf(getTeamName());
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
		xpThisGame += cause.getExperience();

		sendMessage("<b>+{} Bed Wars XP ({})", cause.getExperience(), cause.getFormattedName());
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		dp.setValue(dp.getValue() + cause.getExperience());

		setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
		setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));
	}

	public void xp(ExperienceCause cause, long units) {
		long amount = cause.calculateXp(units);
		xpThisGame += amount;

		sendMessage("<b>+{} Bed Wars XP ({})", amount, cause.getFormattedName());
		DatapointLeaderboardLong dp = getBedWarsDataHandler().get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class);
		dp.setValue(dp.getValue() + amount);

		setLevel(BedwarsLevelUtil.calculateLevel(dp.getValue()));
		setExp((float) BedwarsLevelUtil.calculateExperienceSinceLastLevel(dp.getValue()) / BedwarsLevelUtil.calculateMaxExperienceFromExperience(dp.getValue()));
	}

	public void hypixelXp(long amount) {
		hypixelXpThisGame += amount;
		sendMessage("<b>+{} Hypixel Experience", amount);
		DatapointHypixelExperience dp = getDataHandler().get(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE, DatapointHypixelExperience.class);
		dp.setValue(dp.getValue() + amount);
	}

	public void token(TokenCause cause) {
		tokensThisGame += cause.getExperience();
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

	@Override
	public void setVelocity(@NotNull Vec velocity) {
		EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
		EventDispatcher.callCancellable(entityVelocityEvent, () -> {
			this.velocity = entityVelocityEvent.getVelocity();
			velocityUpdate = true;
		});
	}

	@Override
	public void setVelocityNoUpdate(Function<Vec, Vec> function) {
		velocity = function.apply(velocity);
	}

	@Override
	public void sendImmediateVelocityUpdate() {
		if (velocityUpdate) {
			velocityUpdate = false;
			sendPacketToViewersAndSelf(getVelocityPacket());
		}
	}

	public boolean isOnGroundAfterTicks(int ticks) {
		if (vehicle != null) return false;

		final double tps = ServerProperties.SERVER_TICKS_PER_SECOND.get();
		Vec velocity = this.velocity.div(tps);
		Pos position = this.position;

		// Slow falling effect
		Aerodynamics aerodynamics = getAerodynamics();
		if (velocity.y() < 0 && hasEffect(PotionEffect.SLOW_FALLING))
			aerodynamics = aerodynamics.withGravity(0.01);

		// Do movementTick() calculations for the given amount of ticks
		PhysicsResult prevPhysicsResult = previousPhysicsResult;
		for (int i = 0; i < ticks; i++) {
			final Block.Getter chunkCache = new ChunkCache(instance, currentChunk, Block.STONE);
			PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity, boundingBox,
				instance.getWorldBorder(), chunkCache, aerodynamics, hasNoGravity(), hasPhysics, onGround, isFlying(), prevPhysicsResult);
			prevPhysicsResult = physicsResult;

			if (physicsResult.isOnGround()) return true;

			velocity = physicsResult.newVelocity();
			position = physicsResult.newPosition();

			// Levitation effect
			TimedPotion levitation = getEffect(PotionEffect.LEVITATION);
			if (levitation != null) {
				velocity = velocity.withY(
						((0.05 * (double) (levitation.potion().amplifier() + 1) - (velocity.y())) * 0.2)
				);
			}
		}

		return false;
	}

	@Override
	protected void movementTick() {
		this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
		if (vehicle != null) return;

		final double tps = ServerProperties.SERVER_TICKS_PER_SECOND.get();

		// Slow falling effect
		Aerodynamics aerodynamics = getAerodynamics();
		if (velocity.y() < 0 && hasEffect(PotionEffect.SLOW_FALLING))
			aerodynamics = aerodynamics.withGravity(0.01);

		final Block.Getter chunkCache = new ChunkCache(instance, currentChunk, Block.STONE);
		PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity.div(tps), boundingBox,
			instance.getWorldBorder(), chunkCache, aerodynamics, hasNoGravity(), hasPhysics, onGround, isFlying(), previousPhysicsResult);
		this.previousPhysicsResult = physicsResult;

		Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
		if (!ChunkUtils.isLoaded(finalChunk)) return;

		velocity = physicsResult.newVelocity().mul(tps);
		//onGround = physicsResult.isOnGround();

		// Levitation effect
		TimedPotion levitation = getEffect(PotionEffect.LEVITATION);
		if (levitation != null) {
			velocity = velocity.withY(
					((0.05 * (double)
							(levitation.potion().amplifier() + 1)
							- (velocity.y() / tps)) * 0.2) * tps
			);
		}

		//TODO
		//if (!PlayerUtils.isSocketClient(this)) {
		//	refreshPosition(physicsResult.newPosition(), true, true);
		//}
		sendImmediateVelocityUpdate();
	}
}
