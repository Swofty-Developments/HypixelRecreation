package net.swofty.type.skyblockgeneric.gui.inventories.banker;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.bank.BankAccountTier;
import net.swofty.type.skyblockgeneric.bank.PersonalBankService;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBankData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIBankUpgrades extends HypixelInventoryGUI {
    private static final int[] TIER_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final String LUXURIOUS_TEXTURE =
        "2b3b73ee2c9c725d807f35a988cb743732b75d7390796621324c207a8c407a90";
    private static final String PALATIAL_TEXTURE =
        "3366a9633a88d038db2771e32ed851845cb1d88b0ac8b7be8ac07299b5f2050";
    private static final Text CLICK_TO_UPGRADE = Text.of("<e>Click to upgrade!");

    public GUIBankUpgrades() {
        super("Bank Account Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void setItems(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");

        for (BankAccountTier tier : BankAccountTier.values()) {
            set(new GUIClickableItem(TIER_SLOTS[tier.ordinal()]) {
                @Override
                public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                    tryUpgrade((SkyBlockPlayer) p, tier);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return createTierItem((SkyBlockPlayer) p, tier);
                }
            });
        }

        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent event, HypixelPlayer p) {
                new GUIBanker().open((SkyBlockPlayer) p);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, """
                        <a>Go Back
                        <7>To Bank""");
            }
        });
    }

    private ItemStack.Builder createTierItem(SkyBlockPlayer player, BankAccountTier tier) {
        DatapointBankData.BankData data = PersonalBankService.data(player);
        List<Text> lore = new ArrayList<>();
        TextColor color = tier.getColor();

        lore.add(Text.of(tier == BankAccountTier.STARTER ? "<8>Not upgraded" : "<8>Bank Upgrade"));
        lore.add(Text.empty());
        lore.add(Text.of("<color:{0}>><m>------- </m><r>  <6>Interest Tranches<color:{0}>  <m>-------</m><color:{0}>\\<",
            color));
        addInterestTranches(lore, tier);
        lore.add(Text.empty());

        double multiplier = 1D + Math.clamp(data.getMuseumMilestone(), 0, 30) * 0.02D;
        lore.add(Text.of(" <7>Max interest: <6>{:,}", tier.getBaseMaximumInterest() * multiplier));
        lore.add(Text.of(" <8> (With {:,} balance)", interestBalance(tier)));
        lore.add(Text.of("<color:{0}>><m>---------------------------------</m><color:{0}>\\<", color));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Max balance: <6>{}", balanceLabel(tier)));
        lore.add(Text.empty());
        addCostAndRequirements(lore, tier);
        lore.add(status(player, data, tier));

        Text name = Text.of("<color:{0}>{1} Account", tier.getColor(), tier.getDisplayName());
        return switch (tier) {
            case LUXURIOUS -> ItemStacks.head(LUXURIOUS_TEXTURE, name, lore);
            case PALATIAL -> ItemStacks.head(PALATIAL_TEXTURE, name, lore);
            default -> ItemStacks.item(tierMaterial(tier), 1, name, lore);
        };
    }

    private void addCostAndRequirements(List<Text> lore, BankAccountTier tier) {
        if (tier == BankAccountTier.STARTER) {
            lore.add(Text.of("<7>Cost: <a>Complimentary"));
            lore.add(Text.empty());
            return;
        }

        lore.add(Text.of("<7>Cost"));
        lore.add(Text.of("<6>{:,} Coins", tier.getCoinCost()));
        lore.add(Text.of("<9>Enchanted Gold Block <8>x{}", tier.getEnchantedGoldBlocks()));
        lore.add(Text.empty());

        if (tier.getMuseumMilestone() > 0)
            lore.add(Text.of("<c>Requires Museum Milestone {}!", tier.getMuseumMilestone()));
        if (tier.getGoldCollection() > 0)
            lore.add(Text.of("<c>Requires {} gold collection!", compactNumber(tier.getGoldCollection())));
        lore.add(Text.empty());
    }

    private Text status(SkyBlockPlayer player, DatapointBankData.BankData data, BankAccountTier tier) {
        BankAccountTier current = data.getAccountTier();
        if (tier.ordinal() < current.ordinal()) return Text.of("<c>You have a better account!");
        if (tier == current) return Text.of("<a>This is your account!");
        if (tier.ordinal() > current.ordinal() + 1) return Text.of("<c>Need previous upgrade!");
        if (data.getMuseumMilestone() < tier.getMuseumMilestone())
            return Text.of("<c>Museum Milestone too low!");
        if (player.getCollection().get(ItemType.GOLD_INGOT) < tier.getGoldCollection())
            return Text.of("<c>Gold collection too low!");
        if (player.getCoins() < tier.getCoinCost()) return Text.of("<c>Not enough coins!");
        if (player.getAmountInInventory(ItemType.ENCHANTED_GOLD_BLOCK) < tier.getEnchantedGoldBlocks())
            return Text.of("<c>Not enough Enchanted Gold Blocks!");
        return CLICK_TO_UPGRADE;
    }

    private void tryUpgrade(SkyBlockPlayer player, BankAccountTier selected) {
        DatapointBankData.BankData data = PersonalBankService.data(player);
        if (data.getAccountTier().next() != selected) return;

        Text status = status(player, data, selected);
        if (!status.equals(CLICK_TO_UPGRADE)) {
            player.sendMessage(status);
            return;
        }

        player.removeCoins(selected.getCoinCost());
        player.takeItem(ItemType.ENCHANTED_GOLD_BLOCK, selected.getEnchantedGoldBlocks());
        data.setAccountTier(selected);
        player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.BANK_DATA, DatapointBankData.class)
            .setValue(data);
        player.sendMessage("<a>Upgraded your bank account to <color:{0}>{1}<a>!",
            selected.getColor(), selected.getDisplayName());
        new GUIBankUpgrades().open(player);
    }

    private void addInterestTranches(List<Text> lore, BankAccountTier tier) {
        lore.add(Text.of(" <e>First <6>10M <e>coins yields <b>2% <e>interest."));
        lore.add(Text.of(" <e>From <6>10M <e>to <6>{} <e>coins yields <b>1% <e>interest.",
            tier == BankAccountTier.STARTER ? "15M" : "20M"));
        if (tier.ordinal() >= BankAccountTier.DELUXE.ordinal())
            lore.add(Text.of(" <e>From <6>20M <e>to <6>30M <e>coins yields <b>0.5% <e>interest."));
        if (tier.ordinal() >= BankAccountTier.SUPER_DELUXE.ordinal())
            lore.add(Text.of(" <e>From <6>30M <e>to <6>50M <e>coins yields <b>0.2% <e>interest."));
        if (tier.ordinal() >= BankAccountTier.PREMIER.ordinal())
            lore.add(Text.of(" <e>From <6>50M <e>to <6>160M <e>coins yields <b>0.1% <e>interest."));
        if (tier.ordinal() >= BankAccountTier.LUXURIOUS.ordinal())
            lore.add(Text.of(" <e>From <6>160M <e>to <6>5.2B <e>coins yields <b>0.01% <e>interest."));
        if (tier.ordinal() >= BankAccountTier.PALATIAL.ordinal())
            lore.add(Text.of(" <e>From <6>5.2B <e>to <6>55.2B <e>coins yields <b>0.001% <e>interest."));
    }

    private Material tierMaterial(BankAccountTier tier) {
        return switch (tier) {
            case STARTER -> Material.WHEAT_SEEDS;
            case GOLD -> Material.GOLD_NUGGET;
            case DELUXE -> Material.GOLD_INGOT;
            case SUPER_DELUXE -> Material.GOLDEN_CHESTPLATE;
            case PREMIER -> Material.GOLDEN_HORSE_ARMOR;
            default -> throw new IllegalArgumentException("Tier uses a custom head: " + tier);
        };
    }

    private double interestBalance(BankAccountTier tier) {
        return switch (tier) {
            case STARTER -> 15_000_000D;
            case GOLD -> 20_000_000D;
            case DELUXE -> 30_000_000D;
            case SUPER_DELUXE -> 50_000_000D;
            case PREMIER -> 160_000_000D;
            case LUXURIOUS -> 5_160_000_000D;
            case PALATIAL -> 55_160_000_000D;
        };
    }

    private String balanceLabel(BankAccountTier tier) {
        return switch (tier) {
            case STARTER -> "50 Million Coins";
            case GOLD -> "100 Million Coins";
            case DELUXE -> "250 Million Coins";
            case SUPER_DELUXE -> "500 Million Coins";
            case PREMIER -> "1 Billion Coins";
            case LUXURIOUS -> "6 Billion Coins";
            case PALATIAL -> "60 Billion Coins";
        };
    }

    private String compactNumber(int number) {
        if (number % 1_000_000 == 0) return number / 1_000_000 + "M";
        if (number % 1_000 == 0) return number / 1_000 + "K";
        return String.valueOf(number);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }
}
