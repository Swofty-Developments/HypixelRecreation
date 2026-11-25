package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GUIComposter extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Composter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenGuiSupport.syncComposter(player);
        GardenData.GardenComposterData data = GardenGuiSupport.composter(player);
        int organicMatterCap = GardenServices.composter().getOrganicMatterCapacity(data.getUpgrades().getOrDefault("organic_matter_cap", 0));
        int fuelCap = GardenServices.composter().getFuelCapacity(data.getUpgrades().getOrDefault("fuel_cap", 0));

        String organicMatterBar = GardenGuiSupport.progressBar(data.getOrganicMatter(), organicMatterCap)
            + " §e" + StringUtility.commaify(Math.round(data.getOrganicMatter() * 10D) / 10D)
            + "§6/§e" + StringUtility.commaify(organicMatterCap);
        String fuelBar = GardenGuiSupport.progressBar(data.getFuel(), fuelCap)
            + " §e" + StringUtility.commaify(Math.round(data.getFuel()))
            + "§6/§e" + StringUtility.commaify(fuelCap);

        layout.slot(1, buildMeterItem("§eOrganic Matter", organicMatterFill(data.getOrganicMatter(), organicMatterCap), organicMatterBar));
        layout.slot(7, buildMeterItem("§2Fuel", fuelFill(data.getFuel(), fuelCap), fuelBar));
        layout.slot(10, buildMeterItem("§eOrganic Matter", organicMatterFill(data.getOrganicMatter(), organicMatterCap), organicMatterBar));
        layout.slot(16, buildMeterItem("§2Fuel", fuelFill(data.getFuel(), fuelCap), fuelBar));
        layout.slot(19, buildMeterItem("§eOrganic Matter", organicMatterFill(data.getOrganicMatter(), organicMatterCap), organicMatterBar));
        layout.slot(25, buildMeterItem("§2Fuel", fuelFill(data.getFuel(), fuelCap), fuelBar));
        layout.slot(28, buildMeterItem("§eOrganic Matter", organicMatterFill(data.getOrganicMatter(), organicMatterCap), organicMatterBar));
        layout.slot(34, buildMeterItem("§2Fuel", fuelFill(data.getFuel(), fuelCap), fuelBar));
        layout.slot(37, buildMeterItem("§eOrganic Matter", organicMatterFill(data.getOrganicMatter(), organicMatterCap), organicMatterBar));
        layout.slot(43, buildMeterItem("§2Fuel", fuelFill(data.getFuel(), fuelCap), fuelBar));

        layout.slot(13, (s, c) -> ItemStackCreator.getStack(
            "§eCollect Compost",
            data.getCompostAvailable() > 0 ? Material.GREEN_TERRACOTTA : Material.RED_TERRACOTTA,
            1,
            "§7Compost Available: §a" + data.getCompostAvailable(),
            "",
            data.getCompostAvailable() > 0 ? "§eClick to collect!" : "§cNo Compost to collect"
        ), (click, c) -> {
            if (data.getCompostAvailable() <= 0) {
                return;
            }
            int available = data.getCompostAvailable();
            data.setCompostAvailable(0);
            player.addAndUpdateItem(ItemType.COMPOST, available);
            player.playSuccessSound();
            c.session(Object.class).refresh();
        });

        layout.slot(22, ItemStackCreator.getStack(
            "§aComposter Upgrades",
            Material.HOPPER,
            1,
            "§7Upgrade your composter to increase",
            "§7your compost production.",
            "",
            "§7Speed: §e" + data.getUpgrades().getOrDefault("speed", 0),
            "§7Multi Drop: §e" + data.getUpgrades().getOrDefault("multi_drop", 0),
            "§7Fuel Cap: §e" + data.getUpgrades().getOrDefault("fuel_cap", 0),
            "§7Organic Matter Cap: §e" + data.getUpgrades().getOrDefault("organic_matter_cap", 0),
            "§7Cost Reduction: §e" + data.getUpgrades().getOrDefault("cost_reduction", 0),
            "",
            "§eClick to view upgrades!"
        ));

        layout.slot(39, ItemStackCreator.getStack(
            "§aInsert Crops from Sacks",
            Material.CAULDRON,
            1,
            "§7Grab as many crops that will fit into",
            "§7the composter from your sacks.",
            "",
            "§7In your sacks: §e" + StringUtility.commaify(getTotalOrganicMatterInSacks(player)) + " Organic Matter",
            "",
            "§eLeft-click to grab from sacks!"
        ), (click, c) -> {
            int moved = insertOrganicMatter(player, true);
            if (moved > 0) {
                player.playSuccessSound();
                c.session(Object.class).refresh();
            } else {
                player.sendMessage("§cNo organic matter items could be inserted from sacks.");
            }
        });

        layout.slot(41, ItemStackCreator.getStack(
            "§aInsert Fuel from Sacks",
            Material.CAULDRON,
            1,
            "§7Grab as much fuel that will fit into",
            "§7the composter from your sacks.",
            "",
            "§7In your sacks: §e" + StringUtility.commaify(getTotalFuelInSacks(player)) + " Fuel",
            "",
            "§eLeft-click to grab from sacks!"
        ), (click, c) -> {
            int moved = insertFuel(player, true);
            if (moved > 0) {
                player.playSuccessSound();
                c.session(Object.class).refresh();
            } else {
                player.sendMessage("§cNo fuel items could be inserted from sacks.");
            }
        });

        layout.slot(48, ItemStackCreator.getStackHead(
            "§aInsert Crops from Inventory",
            "ef835b8941fe319931749b87fe8e84c5d1f4a271b5fbce5e700a60004d881f79",
            1,
            "§7Grab as many crops that will fit into",
            "§7the composter from your inventory.",
            "",
            "§7In your inventory: §e" + StringUtility.commaify(getTotalOrganicMatterInInventory(player)) + " Organic Matter",
            "",
            "§eLeft-click to grab from inventory!"
        ), (click, c) -> {
            int moved = insertOrganicMatter(player, false);
            if (moved > 0) {
                player.playSuccessSound();
                c.session(Object.class).refresh();
            } else {
                player.sendMessage("§cNo organic matter items could be inserted from your inventory.");
            }
        });

        layout.slot(50, ItemStackCreator.getStack(
            "§aInsert Fuel from Inventory",
            Material.GREEN_DYE,
            1,
            "§7Grab as much fuel that will fit into",
            "§7the composter from your inventory.",
            "",
            "§7In your inventory: §e" + StringUtility.commaify(getTotalFuelInInventory(player)) + " Fuel",
            "",
            "§eLeft-click to grab from inventory!"
        ), (click, c) -> {
            int moved = insertFuel(player, false);
            if (moved > 0) {
                player.playSuccessSound();
                c.session(Object.class).refresh();
            } else {
                player.sendMessage("§cNo fuel items could be inserted from your inventory.");
            }
        });

        layout.slot(46, ItemStackCreator.getStack(
            "§eCrop Meter",
            Material.POTATO,
            1,
            organicMatterBar,
            "",
            "§7Fill your composter with §acrops§7, like",
            "§fWheat §7or §aEnchanted Potato§7, to turn",
            "§7them into §eOrganic Matter§7. Organic",
            "§7Matter is used to make §6Compost§7.",
            "",
            "§7The composter must have §b4,000",
            "§7organic matter stored to start",
            "§7making compost."
        ));

        layout.slot(52, ItemStackCreator.getStackHead(
            "§2Fuel Meter",
            "d5d2750595477ecc13869580b12ffc3b13fc2b3ac3e5035ecfc9aafa036722a2",
            1,
            fuelBar,
            "",
            "§7Fill your composter with §2machine fuel§7,",
            "§7like §9Biofuel§7 to power the composter",
            "§7to turn Organic Matter into §6Compost§7.",
            "",
            "§7The composter must have §22,000 Fuel",
            "§7stored to start making compost."
        ));
    }

    private net.minestom.server.item.ItemStack.Builder buildMeterItem(String name, Material material, String bar) {
        return ItemStackCreator.getStack(name, material, 1, bar);
    }

    private int insertOrganicMatter(SkyBlockPlayer player, boolean fromSacks) {
        return insertValues(
            player,
            GardenServices.composter().getOrganicMatterCapacity(GardenGuiSupport.composter(player).getUpgrades().getOrDefault("organic_matter_cap", 0)),
            GardenGuiSupport.composter(player).getOrganicMatter(),
            false,
            fromSacks
        );
    }

    private int insertFuel(SkyBlockPlayer player, boolean fromSacks) {
        return insertValues(
            player,
            GardenServices.composter().getFuelCapacity(GardenGuiSupport.composter(player).getUpgrades().getOrDefault("fuel_cap", 0)),
            GardenGuiSupport.composter(player).getFuel(),
            true,
            fromSacks
        );
    }

    private int insertValues(SkyBlockPlayer player, double capacity, double current, boolean fuel, boolean fromSacks) {
        GardenData.GardenComposterData data = GardenGuiSupport.composter(player);
        double remainingCapacity = capacity - current;
        if (remainingCapacity <= 0D) {
            return 0;
        }

        List<Map.Entry<String, Double>> values = (fuel
            ? GardenServices.composter().getFuelValues()
            : GardenServices.composter().getOrganicMatterValues())
            .entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
            .toList();

        int insertedStacks = 0;
        double insertedValue = 0D;
        Map<String, Long> tracker = fuel ? data.getInsertedFuel() : data.getInsertedMatter();
        for (Map.Entry<String, Double> entry : values) {
            if (remainingCapacity <= 0D) {
                break;
            }

            ItemType type = ItemType.get(entry.getKey());
            double perItem = entry.getValue();
            if (type == null || perItem <= 0D) {
                continue;
            }

            int available = fromSacks ? player.getSackItems().getAmount(type) : player.getAmountInInventory(type);
            if (available <= 0) {
                continue;
            }

            int insertable = (int) Math.min(available, Math.floor(remainingCapacity / perItem));
            if (insertable <= 0) {
                continue;
            }

            if (fromSacks) {
                player.getSackItems().decrease(type, insertable);
            } else if (player.takeItem(type, insertable) == null) {
                continue;
            }

            insertedStacks += insertable;
            double valueAdded = insertable * perItem;
            insertedValue += valueAdded;
            remainingCapacity -= valueAdded;
            tracker.merge(entry.getKey(), (long) insertable, Long::sum);
        }

        if (fuel) {
            data.setFuel(data.getFuel() + insertedValue);
        } else {
            data.setOrganicMatter(data.getOrganicMatter() + insertedValue);
        }
        return insertedStacks;
    }

    private double getTotalOrganicMatterInSacks(SkyBlockPlayer player) {
        return GardenServices.composter().getOrganicMatterValues().entrySet().stream()
            .mapToDouble(entry -> {
                ItemType type = ItemType.get(entry.getKey());
                return type == null ? 0D : player.getSackItems().getAmount(type) * entry.getValue();
            })
            .sum();
    }

    private double getTotalFuelInSacks(SkyBlockPlayer player) {
        return GardenServices.composter().getFuelValues().entrySet().stream()
            .mapToDouble(entry -> {
                ItemType type = ItemType.get(entry.getKey());
                return type == null ? 0D : player.getSackItems().getAmount(type) * entry.getValue();
            })
            .sum();
    }

    private double getTotalOrganicMatterInInventory(SkyBlockPlayer player) {
        return GardenServices.composter().getOrganicMatterValues().entrySet().stream()
            .mapToDouble(entry -> {
                ItemType type = ItemType.get(entry.getKey());
                return type == null ? 0D : player.getAmountInInventory(type) * entry.getValue();
            })
            .sum();
    }

    private double getTotalFuelInInventory(SkyBlockPlayer player) {
        return GardenServices.composter().getFuelValues().entrySet().stream()
            .mapToDouble(entry -> {
                ItemType type = ItemType.get(entry.getKey());
                return type == null ? 0D : player.getAmountInInventory(type) * entry.getValue();
            })
            .sum();
    }

    private Material organicMatterFill(double current, double cap) {
        double ratio = cap <= 0D ? 0D : current / cap;
        if (ratio >= 0.75D) {
            return Material.LIME_STAINED_GLASS_PANE;
        }
        if (ratio >= 0.25D) {
            return Material.YELLOW_STAINED_GLASS_PANE;
        }
        return Material.RED_STAINED_GLASS_PANE;
    }

    private Material fuelFill(double current, double cap) {
        double ratio = cap <= 0D ? 0D : current / cap;
        if (ratio >= 0.75D) {
            return Material.LIME_STAINED_GLASS_PANE;
        }
        if (ratio >= 0.25D) {
            return Material.YELLOW_STAINED_GLASS_PANE;
        }
        return Material.RED_STAINED_GLASS_PANE;
    }
}
