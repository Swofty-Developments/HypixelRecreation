package net.swofty.type.skywarsgame.user;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.tag.Tag;
import net.swofty.type.game.game.GameParticipant;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class SkywarsPlayer extends HypixelPlayer implements GameParticipant {
    private static final Tag<String> GAME_ID_TAG = Tag.String("gameId");

    private boolean eliminated = false;
    private Pos cagePosition = null;
    private String selectedKit = "default";
    private Set<String> activePerks = new HashSet<>();
    private UUID lastDamager = null;
    private long lastDamageTime = 0;

    public SkywarsPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    @Override
    public String getGameId() {
        return getTag(GAME_ID_TAG);
    }

    @Override
    public void setGameId(String gameId) {
        if (gameId == null) {
            removeTag(GAME_ID_TAG);
            return;
        }
        setTag(GAME_ID_TAG, gameId);
    }

    @Override
    public SkywarsPlayer getServerPlayer() {
        return this;
    }

    public void setLastDamager(UUID damager) {
        this.lastDamager = damager;
        this.lastDamageTime = System.currentTimeMillis();
    }

    public UUID getAssistDamager() {
        if (lastDamager != null && System.currentTimeMillis() - lastDamageTime < 10000) {
            return lastDamager;
        }
        return null;
    }

    public void setupForSpectator() {
        getInventory().clear();
        getInventory().setItemStack(0,
                TypeSkywarsGameLoader.getItemHandler().getItem("spectator_compass").getItemStack());
        getInventory().setItemStack(7,
                TypeSkywarsGameLoader.getItemHandler().getItem("play_again").getItemStack());
        getInventory().setItemStack(8,
                TypeSkywarsGameLoader.getItemHandler().getItem("leave_game").getItemStack());

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(this);
        if (game != null) {
            for (SkywarsPlayer otherPlayer : game.getPlayers()) {
                if (!otherPlayer.equals(this) && !otherPlayer.isEliminated()) {
                    this.removeViewer(otherPlayer);
                    this.updateOldViewer(otherPlayer);
                }
            }
        }

        setGameMode(GameMode.ADVENTURE);
        setAllowFlying(true);
        setFlying(true);
    }

    public void resetGameState() {
        eliminated = false;
        cagePosition = null;
        activePerks = new HashSet<>();
        lastDamager = null;
        lastDamageTime = 0;
    }
}
