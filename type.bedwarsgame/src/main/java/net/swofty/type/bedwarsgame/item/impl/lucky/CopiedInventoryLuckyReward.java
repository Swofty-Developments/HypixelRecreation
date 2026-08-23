package net.swofty.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CopiedInventoryLuckyReward extends LuckyReward {
    public CopiedInventoryLuckyReward() {
        super("Copied Inventory");
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        List<BedWarsPlayer> candidates = player.getGame().getPlayers().stream()
            .filter(other -> other != player)
            .toList();
        if (candidates.isEmpty()) {
            player.sendMessage("<c>No inventory to copy.");
            return;
        }
        BedWarsPlayer target = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            player.getInventory().setItemStack(i, target.getInventory().getItemStack(i));
        }
        player.sendMessage("<a>Copied {}'s inventory.", target.getUsername());
    }
}
