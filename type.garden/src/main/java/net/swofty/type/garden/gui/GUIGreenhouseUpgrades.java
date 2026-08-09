package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIGreenhouseUpgrades extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Greenhouse Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenGreenhouseData greenhouse = GardenGuiSupport.greenhouse(player);

        layout.slot(4, ItemStackCreator.getStack(
            "§aGreenhouse Progress",
            Material.WHITE_STAINED_GLASS,
            1,
            "§7Blueprint unlocked: " + (greenhouse.isBlueprintUnlocked() ? "§aYes" : "§cNo"),
            "§7Unlocked Greenhouses: §e" + greenhouse.getUnlockedGreenhouses() + "§7/§a" + net.swofty.type.garden.GardenServices.greenhouse().getMaxGreenhouses(),
            "§7Unlocked crop slots: §e" + greenhouse.getUnlockedSlots().size(),
            "§7Spent Ethereal Vines: §d" + greenhouse.getSpentEtherealVines(),
            "",
            "§7DNA milestone progress: §b" + greenhouse.getDnaMilestone() + "§7/§a" + net.swofty.type.garden.GardenServices.greenhouse().getDnaMilestones().size()
        ));

        layout.slot(11, buildGreenhouseUnlock(player, greenhouse, 1), (click, c) -> tryUnlockGreenhouse(player, greenhouse, 1, c));
        layout.slot(13, buildGreenhouseUnlock(player, greenhouse, 2), (click, c) -> tryUnlockGreenhouse(player, greenhouse, 2, c));
        layout.slot(15, buildGreenhouseUnlock(player, greenhouse, 3), (click, c) -> tryUnlockGreenhouse(player, greenhouse, 3, c));

        layout.slot(21, ItemStackCreator.getStack(
            "§6Crop Slots",
            Material.COARSE_DIRT,
            1,
            "§7Unlocked slots: §a" + greenhouse.getUnlockedSlots().size(),
            "§7Tracked plot unlocks: §e" + greenhouse.getUnlockedPlots(),
            "",
            "§7Use §5Ethereal Vines §7to unlock crop",
            "§7slots once your Greenhouse is ready.",
            "",
            "§7The slot grid and mutation data are",
            "§7now persisted in Garden profile data."
        ));

        layout.slot(23, ItemStackCreator.getStack(
            "§5Mutation Analyzer",
            Material.SPYGLASS,
            1,
            "§7Analyzed mutations: §a" + greenhouse.getAnalyzedMutations().size(),
            "§7Tracked harvest counts: §e" + greenhouse.getMutationHarvests().size(),
            "",
            "§7Known mutations: §d" + net.swofty.type.garden.GardenServices.greenhouse().getMutations().size(),
            "§7DNA milestones: §b" + net.swofty.type.garden.GardenServices.greenhouse().getDnaMilestones().size()
        ));

        layout.slot(25, ItemStackCreator.getStack(
            "§aHarvest Bounty",
            Material.MOSS_BLOCK,
            1,
            "§7Supported Greenhouse crops: §a" + net.swofty.type.garden.GardenServices.greenhouse().getCrops().size(),
            "§7Harvest bounty drops: §e" + net.swofty.type.garden.GardenServices.greenhouse().getHarvestBounty().size(),
            "",
            "§7Crop, mutation, and bounty tables are",
            "§7now loaded from §aconfiguration/skyblock/garden§7."
        ));
    }

    private net.minestom.server.item.ItemStack.Builder buildGreenhouseUnlock(
        SkyBlockPlayer player,
        GardenData.GardenGreenhouseData greenhouse,
        int greenhouseIndex
    ) {
        int unlockCost = net.swofty.type.garden.GardenServices.greenhouse().getUnlockCostForGreenhouse(greenhouseIndex);
        boolean unlocked = greenhouse.getUnlockedGreenhouses() >= greenhouseIndex;
        boolean previousUnlocked = greenhouseIndex == 1 || greenhouse.getUnlockedGreenhouses() >= greenhouseIndex - 1;
        List<String> lore = new ArrayList<>();
        lore.add("§7Unlock greenhouse #" + greenhouseIndex + " for");
        lore.add("§7expanded mutation farming space.");
        lore.add("");
        lore.add("§7Unlock cost: §5" + StringUtility.commaify(unlockCost) + " Ethereal Vines");
        lore.add("§7Your vines: §d" + StringUtility.commaify(player.getAmountInInventory(ItemType.ETHEREAL_VINE) + player.getSackItems().getAmount(ItemType.ETHEREAL_VINE)));
        lore.add("");

        if (unlocked) {
            lore.add("§a§lUNLOCKED");
        } else if (!greenhouse.isBlueprintUnlocked()) {
            lore.add("§cRequires the Greenhouse Blueprint");
        } else if (!previousUnlocked) {
            lore.add("§cUnlock the previous Greenhouse first");
        } else {
            lore.add(unlockCost == 0 ? "§eClick to unlock!" : "§eClick to spend Ethereal Vines!");
        }

        return ItemStackCreator.getStack(
            unlocked ? "§aGreenhouse " + greenhouseIndex : "§eGreenhouse " + greenhouseIndex,
            unlocked ? Material.LIME_STAINED_GLASS : Material.YELLOW_STAINED_GLASS,
            greenhouseIndex,
            lore
        );
    }

    private void tryUnlockGreenhouse(
        SkyBlockPlayer player,
        GardenData.GardenGreenhouseData greenhouse,
        int greenhouseIndex,
        ViewContext ctx
    ) {
        if (greenhouse.getUnlockedGreenhouses() >= greenhouseIndex) {
            player.sendMessage("§eThat Greenhouse is already unlocked.");
            return;
        }
        if (!greenhouse.isBlueprintUnlocked()) {
            player.sendMessage("§cYou need the Greenhouse Blueprint first.");
            return;
        }
        if (greenhouseIndex > 1 && greenhouse.getUnlockedGreenhouses() < greenhouseIndex - 1) {
            player.sendMessage("§cUnlock the previous Greenhouse first.");
            return;
        }

        int unlockCost = net.swofty.type.garden.GardenServices.greenhouse().getUnlockCostForGreenhouse(greenhouseIndex);
        if (unlockCost > 0 && !player.removeItemFromPlayer(ItemType.ETHEREAL_VINE, unlockCost)) {
            player.sendMessage("§cYou don't have enough Ethereal Vines.");
            return;
        }

        greenhouse.setBlueprintUnlocked(true);
        greenhouse.setGreenhouseUnlocked(true);
        greenhouse.setUnlockedGreenhouses(Math.max(greenhouse.getUnlockedGreenhouses(), greenhouseIndex));
        greenhouse.setSpentEtherealVines(greenhouse.getSpentEtherealVines() + unlockCost);
        player.playSuccessSound();
        ctx.session(Object.class).refresh();
    }
}
