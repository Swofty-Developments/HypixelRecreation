package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;
import java.util.Random;

public class PlayerTeleport implements LuckyBlockItem {

    private static final Random RANDOM = new Random();

    @Override
    public String getId() {
        return "player_teleport";
    }

    @Override
    public String getDisplayName() {
        return "Player Teleport";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.ENDER_PEARL, """
                <5><l>Player Teleport</l>
                <7>Teleport to a random
                <7>alive player!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(holder);
        if (game == null) {
            holder.sendMessage("<c>You are not in a game!");
            return false;
        }

        List<SkywarsPlayer> alivePlayers = game.getAlivePlayers().stream()
                .filter(p -> !p.getUuid().equals(holder.getUuid()))
                .toList();

        if (alivePlayers.isEmpty()) {
            holder.sendMessage("<c>No other players to teleport to!");
            return false;
        }

        SkywarsPlayer target = alivePlayers.get(RANDOM.nextInt(alivePlayers.size()));
        holder.teleport(target.getPosition());
        holder.sendMessage("<d>Teleported to <6>{}<d>!", target.getUsername());

        return true;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }
}
