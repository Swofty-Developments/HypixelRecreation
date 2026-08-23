package net.swofty.type.skyblockgeneric.minion.actions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.item.ItemQuantifiable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.minion.IslandMinionData;
import net.swofty.type.skyblockgeneric.minion.MinionAction;
import net.swofty.type.generic.utility.MathUtility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class MinionMineAction extends MinionAction {
    private final Block toMine;

    @Override
    public @NotNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island) {
        List<Pos> minePositions = MathUtility.getRangeExcludingSelf(
                minion.getPosition().sub(0, 1, 0), 2 + minion.getBonusRange()
        );

        boolean hasAir = minePositions.stream().anyMatch(pos -> island.getBlock(pos).air());

        List<Pos> possiblePositions = MathUtility.getRangeExcludingSelf(
                minion.getPosition().sub(0, 1, 0), 2 + minion.getBonusRange()
        ).stream().filter(pos -> !hasAir || island.getBlock(pos).air()).toList();

        event.setToLook(MathUtility.getRandomElement(possiblePositions));

        event.setAction(() -> {
            if (island.getBlock(event.getToLook()) == toMine || island.getBlock(event.getToLook()).air()) {
                if (hasAir) {
                    minion.getMinionEntity().placeAnimation();
                    island.setBlock(event.getToLook(), toMine);
                    return;
                }

                minion.getMinionEntity().swingAnimation();
                island.setBlock(event.getToLook(), Block.AIR);
            }
        });

        return hasAir ?
                new ArrayList<>() :
                List.of(new SkyBlockItem(Material.fromKey(toMine.key())));
    }

    @Override
    public boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island) {
        List<ItemQuantifiable> items = minion.getItemsInMinion();

        if (items.stream().noneMatch(item -> {
            return item.getItem().getMaterial() == Material.fromKey(toMine.key());
        })) {
            return true;
        }

        boolean shouldAllow = false;
        for (ItemQuantifiable item : items) {
            if (item.getItem().getMaterial() == Material.fromKey(toMine.key())) {
                if (item.getAmount() != 64) {
                    shouldAllow = true;
                }
            }
        }

        return !shouldAllow;
    }
}
