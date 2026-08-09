package net.swofty.type.skyblockgeneric.item.handlers.interactable;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.GUISkyBlockMenu;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;
import net.swofty.type.skyblockgeneric.utility.JerryInformation;

import java.util.HashMap;
import java.util.Map;

public class InteractableRegistry {
    private static final Map<String, InteractableItemConfig> REGISTERED_HANDLERS = new HashMap<>();

    static {
        register("SKYBLOCK_MENU_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).leftClickHandler(
                ((skyBlockPlayer, skyBlockItem) -> skyBlockPlayer.openView(new GUISkyBlockMenu()))
        ).inventoryInteractHandler(
                ((skyBlockPlayer, skyBlockItem) -> {
                    skyBlockPlayer.openView(new GUISkyBlockMenu());
                    return false;
                })
        ).build());
        register("MOVE_JERRY_INTERACT", InteractableItemConfig.builder().rightClickHandler(
                ((player, skyBlockItem) -> {
                    if (!HypixelConst.isIslandServer() && !HypixelConst.isGarden()) {
                        player.sendMessage("§cYou can't move Jerry here! He doesn't belong here!");
                        return;
                    }

                    Point position = player.getTargetBlockPosition(5);
                    if (position == null) {
                        return;
                    }

                    // Move up one block and center jerry
                    position = position.add(0.5, 1, 0.5);

                    SkyBlockIsland island = player.getSkyBlockIsland();
                    JerryInformation jerryInformation = island.getJerryInformation();

                    ServerHolograms.ExternalHologram hologram = jerryInformation.getHologram();
                    ServerHolograms.removeExternalHologram(hologram);

                    jerryInformation.setJerryPosition(new Pos(position).withLookAt(player.getPosition()));
                    jerryInformation.getJerry().teleport(jerryInformation.getJerryPosition());
                    jerryInformation.getJerry().lookAt(player.getPosition().add(0, 1.4, 0));

                    hologram = ServerHolograms.ExternalHologram.builder()
                            .text(hologram.getText())
                            .instance(hologram.getInstance())
                            .pos(jerryInformation.getJerryPosition().add(0, 1, 0))
                            .build();

                    ServerHolograms.addExternalHologram(hologram);
                    jerryInformation.setHologram(hologram);
                    skyBlockItem.setAmount(0);
                })
        ).build());
        register("MOVE_SAM_INTERACT", InteractableItemConfig.builder().rightClickHandler(
            ((player, skyBlockItem) -> {
                if (!HypixelConst.isIslandServer()) {
                    player.sendMessage("§cYou can't move Sam here! She doesn't belong here!");
                    return;
                }

                Point position = player.getTargetBlockPosition(5);
                if (position == null || player.getSkyBlockIsland() == null) {
                    return;
                }

                position = position.add(0.5, 1, 0.5);
                player.getSkyBlockIsland().setSamPosition(new Pos(position).withLookAt(player.getPosition()));
                HypixelNPC.updateForPlayer(player);
                skyBlockItem.setAmount(0);
            })
        ).build());
    }

    public static void register(String id, InteractableItemConfig handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static InteractableItemConfig getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}
