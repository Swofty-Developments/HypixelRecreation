package net.swofty.type.skywarsgame.manager;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.ArrayList;
import java.util.List;

public class CageManager {
    private final Instance instance;
    private final List<Pos> cagePositions;
    private final List<Pos> usedCages = new ArrayList<>();

    public CageManager(Instance instance, List<Pos> cagePositions) {
        this.instance = instance;
        this.cagePositions = new ArrayList<>(cagePositions);
    }

    public Pos assignCage(SkywarsPlayer player) {
        if (cagePositions.isEmpty()) {
            return new Pos(0, 100, 0);
        }

        for (Pos pos : cagePositions) {
            if (!usedCages.contains(pos)) {
                usedCages.add(pos);
                player.setCagePosition(pos);
                return pos;
            }
        }

        throw new IllegalStateException("No SkyWars cage is available");
    }

    public void releaseCage(SkywarsPlayer player) {
        Pos cage = player.getCagePosition();
        if (cage != null) {
            usedCages.remove(cage);
            player.setCagePosition(null);
        }
    }

    public void openAllCages() {
        for (Pos cagePos : cagePositions) {
            removeCageBlocks(cagePos);
        }
    }

    private void removeCageBlocks(Pos center) {
        int radius = 4;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = instance.getBlock(center.add(x, y, z));
                    if (block.name().toLowerCase().contains("glass")) {
                        instance.setBlock(center.add(x, y, z), Block.AIR);
                    }
                }
            }
        }
    }

    public int getAvailableCages() {
        return cagePositions.size() - usedCages.size();
    }

    public void reset() {
        usedCages.clear();
    }
}
