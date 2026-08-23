package net.swofty.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.shop.TeamUpgrade;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class RandomTeamUpgradeLuckyReward extends LuckyReward {
    public RandomTeamUpgradeLuckyReward() {
        super("Random Team Upgrade");
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        List<TeamUpgrade> upgrades = TypeBedWarsGameLoader.getTeamShopManager().getUpgrades().stream()
            .filter(upgrade -> upgrade.getNextTier(player.getGame(), player.getTeamKey()) != null)
            .toList();
        if (upgrades.isEmpty()) {
            player.sendMessage("<c>Your team has maxed every upgrade.");
            return;
        }
        TeamUpgrade upgrade = upgrades.get(ThreadLocalRandom.current().nextInt(upgrades.size()));
        int nextLevel = Objects.requireNonNull(upgrade.getNextTier(player.getGame(), player.getTeamKey())).getLevel();
        player.getGame().getTeam(player.getTeamKey().name()).ifPresent(team -> team.setUpgradeLevel(upgrade.getId(), nextLevel));
        upgrade.applyEffect(player.getGame(), player.getTeamKey(), nextLevel);
        player.getGame().getPlayersOnTeam(player.getTeamKey()).forEach(teammate -> {
            teammate.setTag(upgrade.getId().levelTag(), nextLevel);
            teammate.sendMessage("<a>Lucky Block upgraded <6>{} {}<a>!", upgrade.getName(), nextLevel);
        });
    }
}
