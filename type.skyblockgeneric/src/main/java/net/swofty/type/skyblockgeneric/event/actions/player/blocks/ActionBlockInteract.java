package net.swofty.type.skyblockgeneric.event.actions.player.blocks;

import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.block.BlockType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.BlockInteractable;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.BlockDataToolComponent;
import net.swofty.type.skyblockgeneric.item.components.RegionSelectorComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class ActionBlockInteract implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInteract(PlayerBlockInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        ItemStack stack = event.getPlayer().getItemInMainHand();
        SkyBlockItem item = new SkyBlockItem(stack);

        if (item.hasComponent(RegionSelectorComponent.class)) {
            RegionSelectorComponent.SelectedRegion region = RegionSelectorComponent.getPlayerRegionSelection().get(player);
            if (region == null) {
                region = new RegionSelectorComponent.SelectedRegion(null, null);
            }
            region.setPos2(event.getBlockPosition());
            RegionSelectorComponent.getPlayerRegionSelection().put(player, region);
            player.sendMessage("<a>Position 2 set to <e>{}<a>.", event.getBlockPosition());
            event.setCancelled(true);
            return;
        }

        if (item.hasComponent(BlockDataToolComponent.class)) {
            player.sendMessage("<a>Block data for <e>{}<a>:", event.getBlockPosition());
            player.sendMessage("<e>- Block ID: <f>{}", event.getBlock().material());
            player.sendMessage("<e>- Block State: <f>{}", event.getBlock().state());
            event.setCancelled(true);
            return;
        }

        SkyBlockBlock block;
        if (SkyBlockBlock.isSkyBlockBlock(event.getBlock())) {
            block = new SkyBlockBlock(event.getBlock());
        } else if (Material.fromKey(event.getBlock().key()) == Material.ENCHANTING_TABLE) {
            block = new SkyBlockBlock(BlockType.ENCHANTMENT_TABLE);
        } else {
            return;
        }

        if (block.getGenericInstance() instanceof BlockInteractable interactable) {
            interactable.onInteract(event, block);
        }
    }
}
