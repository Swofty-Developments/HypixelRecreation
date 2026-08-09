package net.swofty.type.theend.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.theend.dragon.EndDragonManager;
import net.swofty.type.theend.service.EndRaceService;

public class ActionEndInteractions implements HypixelEventClass {
    private static final Pos RACE_START = new Pos(-494.5, 121, -241.5);
    private static final Pos DRAGON_ALTAR = new Pos(-671, 9, -276);
    private static final Pos VOID_SEPULTURE_ENTRANCE = new Pos(-562, 7, -314);
    private static final Pos VOID_SEPULTURE_EXIT = new Pos(-576, 7, -317);

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockInteractEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;

        if (event.getBlock().registry().material().name().contains("PRESSURE_PLATE")
                && event.getBlockPosition().distanceSquared(RACE_START) <= 16) {
            event.setCancelled(true);
            EndRaceService.start(player);
            return;
        }

        if (event.getBlockPosition().distanceSquared(DRAGON_ALTAR) > 16
                || !event.getBlock().compare(Block.END_PORTAL_FRAME, Block.Comparator.ID)) return;

        ItemStack stack = player.getItemInMainHand();
        ItemType heldType = new SkyBlockItem(stack).getAttributeHandler().getPotentialType();
        if (heldType != ItemType.SUMMONING_EYE) return;

        event.setCancelled(true);
        if (!EndDragonManager.placeEye(player, event.getBlockPosition())) return;

        player.setItemInMainHand(stack.amount() > 1 ? stack.withAmount(stack.amount() - 1) : ItemStack.AIR);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerMoveEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;

        boolean wasOnPlate = event.getPlayer().getPosition().distanceSquared(RACE_START) <= 4;
        boolean isOnPlate = event.getNewPosition().distanceSquared(RACE_START) <= 4;
        if (!wasOnPlate && isOnPlate) EndRaceService.start(player);

        teleportAt(event, VOID_SEPULTURE_ENTRANCE, VOID_SEPULTURE_EXIT);
        teleportAt(event, VOID_SEPULTURE_EXIT, VOID_SEPULTURE_ENTRANCE);
    }

    private void teleportAt(PlayerMoveEvent event, Pos trigger, Pos destination) {
        if (event.getPlayer().getPosition().distanceSquared(trigger) <= 2.25
                || event.getNewPosition().distanceSquared(trigger) > 2.25) return;
        event.getPlayer().teleport(destination);
    }
}
