package net.swofty.type.bedwarsgame.game.v2;

import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.game.game.team.SimpleGameTeam;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
public class BedWarsTeam extends SimpleGameTeam {
    private final TeamKey teamKey;
    @Setter
    private boolean bedAlive = false;

    private final Map<TeamUpgradeId, Integer> upgrades = new EnumMap<>(TeamUpgradeId.class);

    private final List<TrapId> traps = new ArrayList<>();

    public BedWarsTeam(TeamKey teamKey) {
        super(teamKey.name(), teamKey.getName(), teamKey.chatColor());
        this.teamKey = teamKey;
    }

    public boolean canRespawn() {
        return bedAlive;
    }

    public String firstLetter() {
        return teamKey == TeamKey.GRAY ? "S" : getName().substring(0, 1);
    }

    /**
     * Gets the level of an upgrade.
     *
     * @return The upgrade level, or 0 if not purchased
     */
    public int getUpgradeLevel(TeamUpgradeId upgradeId) {
        return upgrades.getOrDefault(upgradeId, 0);
    }

    public void setUpgradeLevel(TeamUpgradeId upgradeId, int level) {
        upgrades.put(upgradeId, level);
    }

    public void destroyBed() {
        this.bedAlive = false;
    }

    public void addTrap(TrapId trapId) {
        traps.add(trapId);
    }

    public void removeTrap(TrapId trapId) {
        traps.remove(trapId);
    }

    public boolean hasTrap(TrapId trapId) {
        return traps.contains(trapId);
    }

    public int getTrapCount() {
        return traps.size();
    }

    public void swapState(BedWarsTeam other) {
        boolean previousBedAlive = bedAlive;
        bedAlive = other.bedAlive;
        other.bedAlive = previousBedAlive;

        Map<TeamUpgradeId, Integer> previousUpgrades = new EnumMap<>(upgrades);
        upgrades.clear();
        upgrades.putAll(other.upgrades);
        other.upgrades.clear();
        other.upgrades.putAll(previousUpgrades);

        List<TrapId> previousTraps = new ArrayList<>(traps);
        traps.clear();
        traps.addAll(other.traps);
        other.traps.clear();
        other.traps.addAll(previousTraps);
    }
}
