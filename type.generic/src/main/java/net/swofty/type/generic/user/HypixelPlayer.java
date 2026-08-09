package net.swofty.type.generic.user;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.BelowNameTag;
import net.swofty.commons.ServerType;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.*;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.generic.quest.PlayerQuestHandler;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.user.categories.RankColor;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class HypixelPlayer extends Player {
	public long joined;
	@Setter
	@Getter
	private ServerType originServer;
	@Getter
	private boolean readyForEvents = false;
	@Getter
	private PlayerHookManager hookManager;
	protected BelowNameTag belowNameTag;

	@Getter
	private boolean spectating = false;

	public HypixelPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
		super(playerConnection, gameProfile);

		joined = System.currentTimeMillis();
		originServer = resolveInitialOriginServer();
		hookManager = new PlayerHookManager(this, new HashMap<>());
	}

	private static ServerType resolveInitialOriginServer() {
		if (HypixelConst.getTypeLoader() == null || HypixelConst.getTypeLoader().getType() == null) {
			return ServerType.PROTOTYPE_LOBBY;
		}
		return HypixelConst.getTypeLoader().getType();
	}

	public void notImplemented() {
		sendMessage(I18n.t("general.not_implemented"));
	}

	public static String getDisplayName(UUID uuid) {
		if (HypixelGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
			return HypixelGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getFullDisplayName();
		} else {
			// Fallback for offline name display: use Hypixel account data (rank + ign)
			HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(uuid);
			return account.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().getPrefix() +
					account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
		}
	}

	public static String getRawName(UUID uuid) {
		if (HypixelGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
			return HypixelGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getUsername();
		} else {
			HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(uuid);
			return account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue();
		}
	}

	public HypixelDataHandler getDataHandler() {
		return HypixelDataHandler.getUser(this.getUuid());
	}

	public Optional<HypixelDataHandler> getOptionalDataHandler() {
		try {
			return Optional.of(getDataHandler());
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}

	public Rank getRank() {
		return getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();
	}

	public RankColor getRankColor() {
		try {
			return RankColor.valueOf(getDataHandler().get(HypixelDataHandler.Data.RANK_COLOR, DatapointString.class).getValue());
		} catch (IllegalArgumentException e) {
			return RankColor.RED;
		}
	}

	public void setRankColor(RankColor color) {
		getDataHandler().get(HypixelDataHandler.Data.RANK_COLOR, DatapointString.class).setValue(color.name());
	}

	public boolean isMvpPlusPlusAqua() {
		return getDataHandler().get(HypixelDataHandler.Data.MVP_PLUS_PLUS_AQUA, DatapointBoolean.class).getValue();
	}

	public int getRanksGifted() {
		return getDataHandler().get(HypixelDataHandler.Data.RANKS_GIFTED, DatapointInteger.class).getValue();
	}

	public Component getRankPrefix() {
		return getRank().prefixComponent(getRankColor(), isMvpPlusPlusAqua() ? NamedTextColor.AQUA : NamedTextColor.GOLD);
	}

	public Component getRankTitle() {
		return getRank().titleComponent(getRankColor(), isMvpPlusPlusAqua() ? NamedTextColor.AQUA : NamedTextColor.GOLD);
	}

	public String getLegacyRankPrefix() {
		return LegacyComponentSerializer.legacySection().serialize(getRankPrefix());
	}

	public DatapointChatType.ChatType getChatType() {
		return getDataHandler().get(HypixelDataHandler.Data.CHAT_TYPE, DatapointChatType.class).getValue();
	}

	public <S> ViewSession<S> openView(View<S> view, S state) {
		return ViewNavigator.get(this).push(view, state);
	}

	public <S> ViewSession<S> openView(View<S> view) {
		Objects.requireNonNull(view, "View is null");
        switch (view) {
            case StatefulView<S> state -> {
                return ViewNavigator.get(this).push(view, state.initialState());
            }
            case StatelessView _ -> {
                return ViewNavigator.get(this).push(view, null);
            }
            default -> throw new IllegalArgumentException("View must be either StatefulView or StatelessView");
        }
	}

	public ProxyPlayer asProxyPlayer() {
		return new ProxyPlayer(this);
	}

	public void setReadyForEvents() {
		this.readyForEvents = true;
	}

	public LogHandler getLogHandler() {
		return new LogHandler(this);
	}

	public DatapointToggles.Toggles getToggles() {
		return getDataHandler().get(HypixelDataHandler.Data.TOGGLES, DatapointToggles.class).getValue();
	}

	@Override
	public void updateNewViewer(@NonNull Player player) {
		super.updateNewViewer(player);
		if (belowNameTag != null) {
			belowNameTag.addViewer(player);
		}
	}

	/**
	 * Changes the tag below the name.
	 *
	 * @param belowNameTag The new below name tag
	 */
	@Override
	public void setBelowNameTag(@Nullable BelowNameTag belowNameTag) {
		if (this.belowNameTag == belowNameTag) return;

		if (this.belowNameTag != null) {
			this.belowNameTag.removeViewer(this);
		}

		this.belowNameTag = belowNameTag;

		if (belowNameTag != null)
			getViewers().forEach(this.belowNameTag::addViewer); // this is missing from the super method (bug most likely)
	}

	public String getFullDisplayName() {
		return getLegacyRankPrefix() + getUsername();
	}

    public Component getRankDisplayName() {
        return getRankPrefix().appendSpace().append(Component.text(getUsername()));
    }

	public Component getColouredName() {
		Rank rank = getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();
		return Component.text(getUsername(), rank.getTextColor());
	}

	public AntiCheatHandler getAntiCheatHandler() {
		return new AntiCheatHandler(this);
	}

	public PlayerAchievementHandler getAchievementHandler() {
		return new PlayerAchievementHandler(this);
	}

	public PlayerExperienceHandler getExperienceHandler() {
		return new PlayerExperienceHandler(this);
	}

	public PlayerQuestHandler getQuestHandler() {
		return new PlayerQuestHandler(this);
	}

	public void sendTranslated(String key) {
		sendMessage(I18n.t(key));
	}

	public void sendTranslated(String key, ComponentLike... placeholders) {
		sendMessage(I18n.t(key, placeholders));
	}

	public PlayerSkin getPlayerSkin() {
		String texture = getDataHandler().get(HypixelDataHandler.Data.SKIN_TEXTURE, DatapointString.class).getValue();
		String signature = getDataHandler().get(HypixelDataHandler.Data.SKIN_SIGNATURE, DatapointString.class).getValue();
		return new PlayerSkin(texture, signature);
	}

	public void playTeleportSound() {
		playSound(Sound.sound().type(Key.key("entity.ender_pearl.throw")).volume(0.5f).pitch(
			ThreadLocalRandom.current().nextFloat((float) (1.0 / 3.0), 0.5f)
		).build());
		ScheduleUtility.nextTick(() -> playSound(Sound.sound().type(Key.key("entity.player.teleport")).volume(0.5f).pitch(1).build()));
	}

	public void sendTo(ServerType type) {
		sendTo(type, false);
	}

	public void sendTo(ServerType type, boolean force) {
		ProxyPlayer player = asProxyPlayer();

		if (type == HypixelConst.getTypeLoader().getType() && !force) {
			this.teleport(HypixelConst.getTypeLoader().getLoaderValues().spawnPosition().apply(this.getOriginServer()));
			return;
		}

		HypixelConst.getTypeLoader().getTablistManager().nullifyCache(this);

		player.transferTo(type);
	}

	public void updateLocale(DatapointLocale.SupportedLocale locale) {
		getDataHandler().get(HypixelDataHandler.Data.LOCALE, DatapointLocale.class).getValue().switchTo(locale);
		super.setLocale(locale.getLocale());
	}

	@Override
	public void spectate(@NonNull Entity entity) {
		super.spectate(entity);
		spectating = true;
	}

	@Override
	public void stopSpectating() {
		super.stopSpectating();
		spectating = false;
	}

}
