package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
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

public class GardenVisitorOfferView extends StatelessView {
    private final String visitorId;

    public GardenVisitorOfferView(String visitorId) {
        this.visitorId = visitorId;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        String displayName = GardenConfigRegistry.getString(
            net.swofty.type.garden.GardenServices.visitors().getVisitor(visitorId),
            "display_name",
            StringUtility.toNormalCase(visitorId)
        );
        return new ViewConfiguration<>(displayName, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenVisitorState active = GardenGuiSupport.visitors(player).getActiveVisitors().stream()
            .filter(visitor -> visitor.getVisitorId().equalsIgnoreCase(visitorId))
            .findFirst()
            .orElse(null);
        Map<String, Object> definition = net.swofty.type.garden.GardenServices.visitors().getVisitor(visitorId);
        String displayName = GardenConfigRegistry.getString(definition, "display_name", StringUtility.toNormalCase(visitorId));
        String rarity = GardenConfigRegistry.getString(definition, "rarity", active == null ? "UNCOMMON" : active.getRarity());

        List<String> profileLore = new ArrayList<>(List.of(
            GardenGuiSupport.colorForRarity(rarity) + "§l" + rarity,
            "",
            "§7Times Visited: §a" + GardenGuiSupport.visitors(player).getVisitCounts().getOrDefault(visitorId, 0),
            "§7Offers Accepted: §a" + GardenGuiSupport.visitors(player).getServedCounts().getOrDefault(visitorId, 0)
        ));
        layout.slot(13, GardenGuiSupport.visitorIcon(definition, displayName, rarity, profileLore));

        if (active == null) {
            layout.slot(31, ItemStackCreator.getStack(
                "§cNo Active Offer",
                net.minestom.server.item.Material.BARRIER,
                1,
                "§7This visitor is not currently on",
                "§7your Garden."
            ));
            return;
        }

        boolean canAccept = canAccept(player, active);
        List<String> acceptLore = new ArrayList<>();
        acceptLore.add("§7Items Required:");
        for (GardenData.GardenRequest request : active.getRequests()) {
            acceptLore.add(" §7" + StringUtility.toNormalCase(request.getItemId()) + " §8x" + StringUtility.commaify(request.getAmount()));
        }
        acceptLore.add("");
        acceptLore.add("§7Rewards:");
        acceptLore.add(" §8+§3" + StringUtility.commaify(active.getFarmingXp()) + " §7Farming XP");
        acceptLore.add(" §8+§2" + active.getGardenXp() + " §7Garden Experience");
        acceptLore.add(" §8+§c" + active.getCopper() + " Copper");
        if (active.getBits() > 0) {
            acceptLore.add(" §8+§b" + active.getBits() + " Bits");
        }
        acceptLore.addAll(GardenGuiSupport.describeRewards(active.getGuaranteedRewards(), " §8+§6"));
        acceptLore.addAll(GardenGuiSupport.describeRewards(active.getBonusRewards(), " §8+§d"));
        acceptLore.add("");
        acceptLore.add(canAccept ? "§eClick to accept!" : "§cMissing items to accept!");

        layout.slot(29, ItemStackCreator.getStack(
            "§aAccept Offer",
            net.minestom.server.item.Material.GREEN_TERRACOTTA,
            1,
            acceptLore
        ), (click, c) -> {
            if (!GardenGuiSupport.acceptVisitor(player, visitorId)) {
                player.sendMessage("§cYou don't have the required items.");
                return;
            }
            player.closeInventory();
        });

        layout.slot(33, ItemStackCreator.getStack(
            "§cRefuse Offer",
            net.minestom.server.item.Material.RED_TERRACOTTA,
            1,
            "§7" + displayName + " §7will leave your §aGarden",
            "§7and maybe come back later.",
            "",
            "§eClick to refuse!"
        ), (click, c) -> {
            GardenGuiSupport.refuseVisitor(player, visitorId);
            player.closeInventory();
        });
    }

    private boolean canAccept(SkyBlockPlayer player, GardenData.GardenVisitorState visitor) {
        for (GardenData.GardenRequest request : visitor.getRequests()) {
            net.swofty.commons.skyblock.item.ItemType itemType = net.swofty.commons.skyblock.item.ItemType.get(request.getItemId());
            if (itemType == null) {
                return false;
            }
            int available = player.getAmountInInventory(itemType) + player.getSackItems().getAmount(itemType);
            if (available < request.getAmount()) {
                return false;
            }
        }
        return true;
    }
}
