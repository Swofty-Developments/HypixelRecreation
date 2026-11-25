package net.swofty.type.generic.user;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.scoreboard.BelowNameTag;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointChatType;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.data.datapoints.DatapointLocale;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.quest.PlayerQuestHandler;
import net.swofty.type.generic.text.HypixelTextRenderer;
import net.swofty.type.generic.text.RenderContext;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.generic.user.categories.RankColor;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.generic.utility.Titles;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Objects;
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
    @Getter
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
		sendMessage(Text.key("general.not_implemented"));
	}

	public static Text getDisplayName(UUID uuid) {
		if (HypixelGenericLoader.getLoadedPlayers().stream().anyMatch(player -> player.getUuid().equals(uuid))) {
			return HypixelGenericLoader.getLoadedPlayers().stream().filter(player -> player.getUuid().equals(uuid)).findFirst().get().getFullDisplayName();
		} else {
			// Fallback for offline name display: use Hypixel account data (rank + ign)
			HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(uuid);
			return Text.of("{}", account.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue()
					.displayName(account.get(HypixelDataHandler.Data.IGN, DatapointString.class).getValue()));
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

	public Text getFullDisplayName() {
		return Text.of("{}", getRank().displayName(getRankColor(),
				isMvpPlusPlusAqua() ? NamedTextColor.AQUA : NamedTextColor.GOLD, getUsername()));
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


	@Override
	public void sendMessage(@NotNull String markup) {
		sendMessage(Text.read(markup));
	}

	public void sendMessage(@NotNull String markup, Object... arguments) {
		sendMessage(Text.of(markup, arguments));
	}

	@Override
	public void sendMessage(@NotNull Component message) {
		super.sendMessage(HypixelTextRenderer.render(message, RenderContext.of(this)));
	}

	@Override
	public void sendActionBar(@NotNull Component message) {
		super.sendActionBar(HypixelTextRenderer.render(message, RenderContext.of(this)));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
		if (value instanceof Component component) {
			super.sendTitlePart(part, (T) HypixelTextRenderer.render(component, RenderContext.of(this)));
			return;
		}
		super.sendTitlePart(part, value);
	}

	public void sendTitlePart(@NotNull TitlePart<Component> part, @NotNull Text value) {
		sendTitlePart(part, value.asComponent());
	}

	public void sendTitlePart(@NotNull TitlePart<Component> part, @NotNull String markup, Object... arguments) {
		sendTitlePart(part, Text.of(markup, arguments));
	}

	public void showTitle(@NotNull Text title, @NotNull Text subtitle, @NotNull Title.Times times) {
		showTitle(Titles.title(title, subtitle, times));
	}

	public void showTitle(@NotNull Text title, @NotNull Text subtitle) {
		showTitle(title, subtitle, Title.DEFAULT_TIMES);
	}

	public void showTitle(@NotNull String titleMarkup, @NotNull String subtitleMarkup, @NotNull Title.Times times,
	                      Object... arguments) {
		showTitle(Text.of(titleMarkup, arguments), Text.of(subtitleMarkup, arguments), times);
	}

	public void showTitle(@NotNull String titleMarkup, @NotNull String subtitleMarkup) {
		showTitle(titleMarkup, subtitleMarkup, Title.DEFAULT_TIMES);
	}

	@Override
	public void kick(@NotNull String markup) {
		kick(Text.read(markup));
	}

	public void kick(@NotNull String markup, Object... arguments) {
		kick(Text.of(markup, arguments));
	}

	public void kick(@NotNull Text reason) {
		kick(reason.asComponent());
	}

	public static BelowNameTag belowNameTag(String objective, Text display) {
		return new BelowNameTag(objective, display.asComponent());
	}

	public static BelowNameTag belowNameTag(String objective, String markup, Object... arguments) {
		return belowNameTag(objective, Text.of(markup, arguments));
	}

	public void setBelowNameTag(String objective, Text display) {
		setBelowNameTag(belowNameTag(objective, display));
	}

	public void setBelowNameTag(String objective, String markup, Object... arguments) {
		setBelowNameTag(belowNameTag(objective, markup, arguments));
	}

	public void setDisplayName(Text displayName) {
		setDisplayName(displayName.asComponent());
	}

	public void setDisplayName(String markup, Object... arguments) {
		setDisplayName(Text.of(markup, arguments));
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
