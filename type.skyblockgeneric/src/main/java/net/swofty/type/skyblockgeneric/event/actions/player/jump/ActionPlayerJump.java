package net.swofty.type.skyblockgeneric.event.actions.player.jump;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionPlayerJump implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY, order = -1)
    public void run(PlayerMoveEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        Pos newPosition = event.getNewPosition();
        Pos currentPosition = player.getPosition();

        if (player.isFlying() || player.getGameMode().equals(GameMode.CREATIVE) || player.isInLaunchpad()) {
            player.setFallHeight(currentPosition.blockY());
            return;
        }

        Integer currentHeight = player.getFallHeight();
        if (currentHeight == null) {
            currentHeight = currentPosition.blockY();
        }

        if (newPosition.y() > currentPosition.y() && currentHeight < newPosition.blockY()) {
            player.setFallHeight(newPosition.blockY());
            player.getPetData().dispatch(new PetEvent.Jump(player, player.getPetData().getEnabledPet()));
        }
    }
}
