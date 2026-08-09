package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
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
import java.util.Map;

public class GUIManageChips extends StatelessView {
    private static final int[] CHIP_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Manage Chips", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return ItemStackCreator.getStackHead(
                "§aManage Chips",
                "2a4a77840449437d21f9d5f047f518413cb2f69e3ecbfb99386649c997ca1e91",
                1,
                "§7Upgrade your §9Chips §7to gain powerful",
                "§7buffs!",
                "",
                "§2✿ Sowdust",
                "§7Sowdust is dropped from harvesting",
                "§7crops in §aThe Garden §7and is used to",
                "§7upgrade the Chips you've unlocked!",
                "",
                "§7Sowdust: §2" + StringUtility.commaify(GardenGuiSupport.personal(player).getSowdust())
            );
        });

        List<Map<String, Object>> chips = GardenServices.chips().getChips();
        for (int index = 0; index < Math.min(chips.size(), CHIP_SLOTS.length); index++) {
            Map<String, Object> chip = chips.get(index);
            int slot = CHIP_SLOTS[index];
            layout.slot(slot, (s, c) -> buildChip((SkyBlockPlayer) c.player(), chip), (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                String chipId = GardenConfigRegistry.getString(chip, "id", "");
                GardenData.GardenChipProgress progress = GardenGuiSupport.getOrCreateChipProgress(player, chipId);

                if (click.click() instanceof Click.RightShift || click.click() instanceof Click.LeftShift) {
                    int redeemed = redeemChip(player, chipId, Integer.MAX_VALUE);
                    if (redeemed > 0) {
                        c.session(Object.class).refresh();
                    } else {
                        player.sendMessage("§cYou don't have any of that Chip to redeem.");
                    }
                    return;
                }

                if (click.click() instanceof Click.Right) {
                    int redeemed = redeemChip(player, chipId, 1);
                    if (redeemed > 0) {
                        c.session(Object.class).refresh();
                    } else {
                        player.sendMessage("§cYou don't have any of that Chip to redeem.");
                    }
                    return;
                }

                int maxLevel = GardenServices.chips().getMaxLevel(progress.getRarity());
                if (progress.getLevel() <= 0 || maxLevel <= 0) {
                    player.sendMessage("§cRedeem a Chip first.");
                    return;
                }
                if (progress.getLevel() >= maxLevel) {
                    player.sendMessage("§eThat Chip is already at its current rarity cap.");
                    return;
                }

                long cost = GardenServices.chips().getLevelCost(progress.getLevel() + 1);
                if (GardenGuiSupport.personal(player).getSowdust() < cost) {
                    player.sendMessage("§cYou don't have enough Sowdust.");
                    return;
                }

                GardenGuiSupport.personal(player).setSowdust(GardenGuiSupport.personal(player).getSowdust() - cost);
                progress.setLevel(progress.getLevel() + 1);
                player.playSuccessSound();
                c.session(Object.class).refresh();
            });
        }

        layout.slot(50, ItemStackCreator.getStack(
            "§aChip Information",
            net.minestom.server.item.Material.REDSTONE_TORCH,
            1,
            "§7Chips can be upgraded using §2Sowdust§7.",
            "§7To increase a Chip's §9maximum level§7,",
            "§7redeem more copies of the same Chip.",
            "",
            "§7• §9§lRARE §7Chips max at §aLevel 10",
            "§7• §5§lEPIC §7Chips max at §aLevel 15",
            "§7• §6§lLEGENDARY §7Chips max at §aLevel 20"
        ));
    }

    private net.minestom.server.item.ItemStack.Builder buildChip(SkyBlockPlayer player, Map<String, Object> chip) {
        String chipId = GardenConfigRegistry.getString(chip, "id", "");
        String displayName = GardenConfigRegistry.getString(chip, "display_name", StringUtility.toNormalCase(chipId));
        GardenData.GardenChipProgress progress = GardenGuiSupport.getOrCreateChipProgress(player, chipId);
        String rarity = GardenServices.chips().resolveRarity(progress.getConsumed());
        progress.setRarity(rarity);
        if (progress.getConsumed() > 0 && progress.getLevel() == 0) {
            progress.setLevel(1);
        }

        int maxLevel = GardenServices.chips().getMaxLevel(rarity);
        Map<String, Object> effects = GardenConfigRegistry.getSection(chip, "effects");
        double perLevel = switch (rarity) {
            case "LEGENDARY" -> GardenConfigRegistry.getDouble(effects, "legendary_per_level", 0D);
            case "EPIC" -> GardenConfigRegistry.getDouble(effects, "epic_per_level", 0D);
            case "RARE" -> GardenConfigRegistry.getDouble(effects, "rare_per_level", 0D);
            default -> 0D;
        };
        double currentValue = perLevel * progress.getLevel();
        double nextValue = perLevel * Math.min(maxLevel, progress.getLevel() + 1);
        long nextCost = progress.getLevel() >= maxLevel ? 0L : GardenServices.chips().getLevelCost(progress.getLevel() + 1);
        ItemType itemType = ItemType.get(chipId);
        int availableToRedeem = itemType == null ? 0 : player.countItem(itemType);

        List<String> lore = new ArrayList<>(List.of(
            "§7Level " + progress.getLevel() + "§8/" + maxLevel,
            "",
            "§7Acquisition: §b" + GardenConfigRegistry.getString(chip, "acquisition", "Unknown"),
            "",
            "§6Ability",
            "§7Current bonus: §a" + StringUtility.decimalify(currentValue, 2),
            ""
        ));
        if (progress.getLevel() > 0 && progress.getLevel() < maxLevel) {
            lore.add("§a§l=====[ UPGRADE ]=====");
            lore.add("§7Next bonus: §a" + StringUtility.decimalify(nextValue, 2));
            lore.add("");
            lore.add("§7Cost");
            lore.add("§2" + StringUtility.commaify(nextCost) + " Sowdust");
            lore.add("");
            lore.add("§eClick to level up!");
        } else if (progress.getLevel() >= maxLevel) {
            lore.add("§a§lMAXED FOR CURRENT RARITY");
        } else {
            lore.add("§c§lLOCKED");
        }

        lore.add("");
        lore.add("§7Redeemable copies: §e" + availableToRedeem);
        lore.add("§eRight-Click to redeem!");
        lore.add("§eShift-Click to redeem all!");
        lore.add("");
        lore.add(GardenGuiSupport.colorForRarity(rarity) + "§l" + rarity + " GARDEN CHIP");

        String color = GardenGuiSupport.colorForRarity(rarity);
        return GardenGuiSupport.itemWithLore(
            chipId,
            color + displayName,
            lore
        );
    }

    private int redeemChip(SkyBlockPlayer player, String chipId, int maxToRedeem) {
        ItemType itemType = ItemType.get(chipId);
        if (itemType == null) {
            return 0;
        }
        int available = player.countItem(itemType);
        int toRedeem = Math.min(available, maxToRedeem);
        if (toRedeem <= 0 || player.takeItem(itemType, toRedeem) == null) {
            return 0;
        }

        GardenData.GardenChipProgress progress = GardenGuiSupport.getOrCreateChipProgress(player, chipId);
        progress.setConsumed(progress.getConsumed() + toRedeem);
        progress.setRarity(GardenServices.chips().resolveRarity(progress.getConsumed()));
        if (progress.getLevel() == 0) {
            progress.setLevel(1);
        }
        player.playSuccessSound();
        return toRedeem;
    }
}
