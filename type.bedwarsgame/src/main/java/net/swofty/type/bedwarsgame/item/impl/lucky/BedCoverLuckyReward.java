package net.swofty.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

public class BedCoverLuckyReward extends LuckyReward {
    public BedCoverLuckyReward() {
        super("Protective Bed Cover");
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        BedWarsMapsConfig.MapTeam team = player.getGame().getMapEntry().getConfiguration().getTeams().get(player.getTeamKey());
        if (team == null || team.getBed() == null || team.getBed().feet() == null || !player.getGame().isBedAlive(player.getTeamKey())) {
            player.sendMessage("<c>Your bed cannot be covered.");
            return;
        }
        Pos bed = new Pos(team.getBed().feet().x(), team.getBed().feet().y(), team.getBed().feet().z());
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 2; z++) {
                    if (x == 0 && y == 0 && (z == 0 || z == 1)) continue;
                    Pos point = bed.add(x, y, z);
                    if (!player.getInstance().getBlock(point).air()) continue;
                    Block block = y == 0 ? Block.END_STONE : y == 1 ? player.getTeamKey().bedMaterial().block() : Block.OAK_PLANKS;
                    player.getInstance().setBlock(point, block.withTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG, true));
                }
            }
        }
        player.sendMessage("<a>Your bed was covered.");
    }
}
