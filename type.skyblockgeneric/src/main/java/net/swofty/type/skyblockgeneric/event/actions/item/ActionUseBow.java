package net.swofty.type.skyblockgeneric.event.actions.item;

import net.minestom.server.property.ServerProperties;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.item.Material;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BowComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionUseBow implements HypixelEventClass {
    private static final double MIN_POWER = 0.1;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerCancelItemUseEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        SkyBlockItem item = new SkyBlockItem(event.getItemStack());

        if (item.getItemStack().material() != Material.BOW) return;
        if (item.hasComponent(ItemRequirementsComponent.class)
                && !item.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return;

        // Calculate power based on charge time (vanilla formula)
        double power = getBowPower(event.getUseDuration());
        if (power < MIN_POWER) return;

        if (item.hasComponent(BowComponent.class)) {
            BowComponent bow = item.getComponent(BowComponent.class);
            if (bow.isShouldBeArrow()) {
                bow.onBowShoot(player, item, power);
                player.getInventory().update();
            }
        }
    }

    /**
     * Calculates bow power based on charge time (vanilla formula)
     * @param ticks How long the bow was held
     * @return Power from 0.0 to 1.0
     */
    public static double getBowPower(long ticks) {
        double seconds = ticks / (double) ServerProperties.SERVER_TICKS_PER_SECOND.get();
        double power = (seconds * seconds + seconds * 2.0) / 3.0;
        return Math.min(power, 1.0);
    }
}
