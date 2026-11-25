package net.swofty.type.skyblockgeneric.event.actions.player.region;

import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.foraging.ForagingTreeManager;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.CustomDropComponent;
import net.swofty.type.skyblockgeneric.item.components.RegionSelectorComponent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegenConfiguration;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.region.mining.MineableBlock;
import net.swofty.type.skyblockgeneric.region.mining.handler.SkyBlockMiningHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class ActionRegionBlockBreak implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockBreakEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        final ItemStack stack = event.getPlayer().getItemInMainHand();
        final SkyBlockItem item = new SkyBlockItem(stack);

        if (item.hasComponent(RegionSelectorComponent.class)) {
            RegionSelectorComponent.SelectedRegion region = RegionSelectorComponent.getPlayerRegionSelection().get(player);
            if (region == null) {
                region = new RegionSelectorComponent.SelectedRegion(null, null);
            }
            region.setPos1(event.getBlockPosition());
            RegionSelectorComponent.getPlayerRegionSelection().put(player, region);
            player.sendMessage("<a>Position 1 set to <e>{}<a>.", event.getBlockPosition());
            event.setCancelled(true);
            return;
        }

        // Skip if player has build bypass
        if (player.isBypassBuild()) return;

        event.setCancelled(true);
        SkyBlockRegion region = SkyBlockRegion.getRegionOfPosition(event.getBlockPosition());

        Block block = event.getBlock();
        Material material = Material.fromKey(block.name());
        boolean shouldItemDrop = false;

        // Leaves on public foraging islands are authored parts of their tree. Handle them before
        // region mineability validation so players can remove them and the tree can restore them.
        if (!HypixelConst.isIslandServer() && ForagingTreeManager.isTreeLeaf(block)
                && ForagingTreeManager.breakLeaf(player, event.getBlockPosition())) {
            return;
        }

        // Handle island server block breaks
        if (HypixelConst.isIslandServer()) {
            event.getInstance().setBlock(event.getBlockPosition(), Block.AIR);
            shouldItemDrop = true;
        }

        // Handle region-specific block breaks
        else if (region != null) {
            RegionType type = region.getType();
            SkyBlockRegenConfiguration mining = type.getMiningHandler();

            // Validate if block can be mined in this region
            if (material != null && mining != null && !mining.getMineableBlocks(player.getInstance(), event.getBlockPosition()).contains(material)) {
                event.setCancelled(true);
                return;
            }

            // Check if player's tool can break this block using the handler system
            MineableBlock mineableBlock = MineableBlock.get(block);
            SkyBlockItem heldItem = new SkyBlockItem(player.getItemInMainHand());
            if (mineableBlock != null) {
                SkyBlockMiningHandler handler = mineableBlock.getMiningHandler();

                // Check if tool can break this block (unless it breaks instantly)
                if (!handler.breaksInstantly() && !handler.canToolBreak(heldItem)) {
                    event.setCancelled(true);
                    return;
                }

                // Check breaking power requirement
                int playerBreakingPower = heldItem.getAttributeHandler().getBreakingPower();
                if (playerBreakingPower < handler.getMiningPowerRequirement()) {
                    event.setCancelled(true);
                    return;
                }
            }

            // Authored Park/Galatea trees are handled as a single tracked structure so Sweep can
            // harvest connected logs and the exact original blocks can grow back bottom-up.
            if (ForagingTreeManager.isTreeLog(block)) {
                List<ForagingTreeManager.HarvestedLog> harvested = ForagingTreeManager.breakLogs(
                        player, event.getBlockPosition(), heldItem
                );
                for (ForagingTreeManager.HarvestedLog log : harvested) {
                    processTreeLog(player, heldItem, region, log);
                }
                return;
            }

            // Queue block for mining if in valid region
            if (mining != null && material != null) {
                mining.addToQueue(player, event.getBlockPosition().asPos(), (SharedInstance) player.getInstance());
                shouldItemDrop = true;
            }
        }
        // Cancel if not in a region at all
        else {
            event.setCancelled(true);
            return;
        }

        // Handle item drops for valid breaks
        if (material != null && shouldItemDrop) {
            SkyBlockItem brokenBlockItem = new SkyBlockItem(material);

            // Determine which item(s) should be dropped using CustomDropComponent
            List<SkyBlockItem> customDrops = CustomDropComponent.simulateDrop(
                    brokenBlockItem,
                    player,
                    new SkyBlockItem(player.getItemInMainHand()),
                    region,
                    HypixelConst.getTypeLoader().getType(),
                    HypixelConst.isIslandServer()
            );

            // Call custom break event
            boolean playerPlaced = block.hasTag(Tag.Boolean("player_placed")) && block.getTag(Tag.Boolean("player_placed"));

            HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(
                    player, material, event.getBlockPosition(), customDrops, playerPlaced
            ));

            // Process each drop with fortune multiplier
            for (SkyBlockItem dropItem : customDrops) {
                // Calculate drop amount with fortune multiplier
                int dropAmount = dropItem.getAmount(); // Base amount from CustomDropComponent

                try {
                    MineableBlock mineableBlock = MineableBlock.get(block);
                    if (mineableBlock != null) {
                        double baseFortune = player.getStatistics().allStatistics().getOverall(mineableBlock.getBlockType().baseSkillFortune());
                        double specificFortune = player.getStatistics().allStatistics().getOverall(mineableBlock.getBlockType().specificBlockFortune());
                        double fortune = baseFortune + specificFortune;
                        double dropMultiplier = (1 + (fortune * 0.01));
                        dropAmount = (int) Math.ceil(dropAmount * dropMultiplier);
                    }
                } catch (NullPointerException e) {
                    // Keep base amount if fortune calculation fails
                }

                // Update the drop item amount
                dropItem.setAmount(dropAmount);
                player.giveLoot(dropItem, dropAmount, event.getBlockPosition().asPos());
            }
        }
    }

    private void processTreeLog(SkyBlockPlayer player, SkyBlockItem heldItem, SkyBlockRegion region,
                                ForagingTreeManager.HarvestedLog harvested) {
        Material originalMaterial = Material.fromKey(harvested.originalBlock().key());
        if (originalMaterial == null) return;

        SkyBlockItem brokenItem = harvested.dropType() == null
                ? new SkyBlockItem(originalMaterial)
                : new SkyBlockItem(harvested.dropType());
        List<SkyBlockItem> drops = CustomDropComponent.simulateDrop(
                brokenItem,
                player,
                heldItem,
                region,
                HypixelConst.getTypeLoader().getType(),
                false
        );

        double fortune = player.getStatistics().allStatistics().getOverall(
                net.swofty.commons.skyblock.statistics.ItemStatistic.FORAGING_FORTUNE
        );
        if (harvested.dropType() == ItemType.FIG_LOG) {
            fortune += player.getStatistics().allStatistics().getOverall(
                    net.swofty.commons.skyblock.statistics.ItemStatistic.FIG_FORTUNE
            );
        } else if (harvested.dropType() == ItemType.MANGROVE_LOG) {
            fortune += player.getStatistics().allStatistics().getOverall(
                    net.swofty.commons.skyblock.statistics.ItemStatistic.MANGROVE_FORTUNE
            );
        }

        double multiplier = 1 + fortune * 0.01;
        for (SkyBlockItem drop : drops) {
            drop.setAmount((int) Math.ceil(drop.getAmount() * multiplier));
        }

        // Fire after fortune is applied so collection gains match the items actually awarded.
        Material harvestedMaterial = Material.fromKey(brokenItem.getMaterial().key());
        HypixelEventHandler.callCustomEvent(new CustomBlockBreakEvent(
                player,
                harvestedMaterial == null ? originalMaterial : harvestedMaterial,
                harvested.position(),
                drops,
                false
        ));

        for (SkyBlockItem drop : drops) {
            player.giveLoot(drop, drop.getAmount(), harvested.position().asPos());
        }
    }
}
