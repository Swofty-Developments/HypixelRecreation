package net.swofty.type.bedwarsgame.shop;

import lombok.Getter;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.TooltipDisplay;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.util.BedWarsInventoryManipulator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
public abstract class TeamUpgrade {

    private final TeamUpgradeId id;
    private final String name;
    private final String description;
    private final ItemStack displayItem;
    private final List<TeamUpgradeTier> tiers;

    public TeamUpgrade(TeamUpgradeId id, String name, String description, ItemStack displayItem, List<TeamUpgradeTier> tiers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.displayItem = displayItem;
        this.tiers = tiers;
    }

    public int getCurrentLevel(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey) {
        return game.getTeam(teamKey.name())
            .map(team -> team.getUpgradeLevel(id))
            .orElse(0);
    }

    public TeamUpgradeTier getNextTier(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey) {
        int currentLevel = getCurrentLevel(game, teamKey);
        if (currentLevel >= tiers.size()) {
            return null;
        }
        return tiers.get(currentLevel);
    }

    public boolean hasEnoughCurrency(Player player, TeamUpgradeTier tier) {
        return Arrays.stream(player.getInventory().getItemStacks())
            .filter(stack -> stack.material() == tier.getCurrency().getMaterial())
            .mapToInt(ItemStack::amount)
            .sum() >= tier.getPrice();
    }

    public void purchase(BedWarsGame game, BedWarsPlayer player) {
        BedWarsMapsConfig.TeamKey teamKey = player.getTeamKey();
        if (teamKey == null) {
            player.sendMessage("<c>You are not on a team. Report this to the administration.");
            return;
        }

        TeamUpgradeTier nextTier = getNextTier(game, teamKey);
        if (nextTier == null) {
            player.sendMessage("<c>Your team has already maxed out this upgrade.");
            return;
        }

        if (!hasEnoughCurrency(player, nextTier)) {
            player.sendMessage("<c>You do not have enough {} to purchase this.", nextTier.getCurrency().getName());
            return;
        }

        BedWarsInventoryManipulator.removeItems(player, nextTier.getCurrency().getMaterial(), nextTier.getPrice());

        // Set upgrade level on team
        game.getTeam(teamKey.name()).ifPresent(team -> team.setUpgradeLevel(id, nextTier.getLevel()));
        applyEffect(game, teamKey, nextTier.getLevel());

        // Notify all team members
        game.getPlayers().stream()
            .filter(p -> teamKey.equals(p.getTeamKey()))
            .forEach(p -> {
                p.sendMessage(Text.of("<color:{}> ", teamKey.chatColor()).append("<a>purchased <6>{} {}!", name, nextTier.getLevel()));
                p.setTag(id.levelTag(), nextTier.getLevel());
            });
    }

    /**
     * Apply the effect of the upgrade to the team.
     * This can be an instant effect or a permanent change.
     *
     * @param game    The game instance.
     * @param teamKey The team that purchased the upgrade.
     * @param level   The new level of the upgrade.
     */
    public abstract void applyEffect(BedWarsGame game, BedWarsMapsConfig.TeamKey teamKey, int level);

    public ItemStack getDisplayItem() {
        return displayItem.with(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, Set.of()));
    }

}
