package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class JediForce implements LuckyBlockConsumable {

    private static final double PUSH_POWER = 100.0;
    private static final double MAX_RANGE = 10.0;

    @Override
    public String getId() {
        return "jedi_force";
    }

    @Override
    public String getDisplayName() {
        return "Jedi Force";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.BLAZE_ROD, """
                <b><l>Jedi Force</l>
                <7>Use the Force to push
                <7>your enemies away!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        Vec lookDirection = player.getPosition().direction();
        SkywarsPlayer target = null;
        double closestDistance = Double.MAX_VALUE;

        for (SkywarsPlayer other : game.getPlayers()) {
            if (other.equals(player) || other.isEliminated()) continue;

            Vec toOther = Vec.fromPoint(other.getPosition().sub(player.getPosition()));
            double distance = toOther.length();

            if (distance > MAX_RANGE) continue;

            Vec normalizedToOther = toOther.normalize();
            double dot = lookDirection.dot(normalizedToOther);

            if (dot > 0.7 && distance < closestDistance) {
                closestDistance = distance;
                target = other;
            }
        }

        if (target == null) {
            player.sendMessage("<c>No target in range!");
            return;
        }

        Vec pushDirection = Vec.fromPoint(target.getPosition().sub(player.getPosition())).normalize();
        target.setVelocity(pushDirection.mul(PUSH_POWER));

        player.sendMessage("<b>The Force is with you!");
        target.sendMessage("<c>You were pushed by the Force!");
    }
}
