package net.swofty.type.skywarslobby.gui;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.kit.SkywarsKit;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI showing detailed information about a specific kit, including prestige levels and stats.
 */
public class GUIKitBreakdown extends HypixelInventoryGUI {
    private static final int[] PRESTIGE_THRESHOLDS = {0, 1000, 2500, 5000, 10000, 15000, 20000, 30000};
    private static final String[] PRESTIGE_NAMES = {"None", "I", "II", "III", "IV", "V", "VI", "VII"};

    private final SkywarsKit kit;
    private final String mode;

    public GUIKitBreakdown(SkywarsKit kit, String mode) {
        super(Text.of("{} Kit", kit.getName()), InventoryType.CHEST_6_ROW);
        this.kit = kit;
        this.mode = mode;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = handler.get(
                SkywarsDataHandler.Data.UNLOCKS,
                DatapointSkywarsUnlocks.class
        ).getValue();

        long coins = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue();
        boolean owned = unlocks.hasKit(kit.getId());
        boolean isFavorite = unlocks.isFavorite(kit.getId());
        int kitXP = unlocks.getKitXP(kit.getId());
        int prestigeLevel = unlocks.getKitPrestigeLevel(kit.getId());

        // Kit info display (slot 4)
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7>Rarity: {}", kit.getRarity().getFormattedName()));
                lore.add(Text.empty());
                lore.add(Text.of("<7>Starting Items ({}):", mode));
                lore.addAll(kit.getItemsLore(mode));

                if (kit.getSpecialAbility() != null && !kit.getSpecialAbility().isEmpty()) {
                    lore.add(Text.empty());
                    lore.add(Text.of("<6>Special: <e>{}", kit.getSpecialAbility()));
                }

                lore.add(Text.empty());
                if (owned) {
                    lore.add(Text.of("<a><l>OWNED"));
                    lore.add(Text.of("<7>Kit XP: <e>{}", kitXP));
                    lore.add(Text.of("<7>Prestige: <d>{}", PRESTIGE_NAMES[prestigeLevel]));
                } else {
                    lore.add(Text.of("<c><l>NOT OWNED"));
                    lore.add(Text.of("<7>Cost: {}", kit.getFormattedCost()));
                }

                Text name = Text.of((owned ? "<a>" : "<c>") + "{} Kit", kit.getName());
                if (kit.hasCustomTexture()) {
                    return ItemStackCreator.head(kit.getIconTexture(), name, lore);
                } else {
                    return ItemStackCreator.item(kit.getIconMaterial(), 1, name, lore);
                }
            }
        });

        // Prestige levels (slots 10-16)
        for (int i = 0; i < 7; i++) {
            final int level = i + 1;
            final int slot = 10 + i;
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    boolean unlocked = prestigeLevel >= level;
                    boolean isNextLevel = prestigeLevel == level - 1;
                    int xpRequired = PRESTIGE_THRESHOLDS[level];
                    int xpForPrevious = level > 1 ? PRESTIGE_THRESHOLDS[level - 1] : 0;

                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<7>Earn <d>SkyWars XP</d> with this kit to prestige it."));
                    lore.add(Text.empty());

                    // Progress bar
                    int progress = Math.max(0, kitXP - xpForPrevious);
                    int needed = xpRequired - xpForPrevious;
                    double percent = owned ? Math.min(100.0, (progress * 100.0) / needed) : 0;
                    int filled = (int) (percent / 10);

                    lore.add(Text.of("<7>Progress: <e>{}<6>%", (int) percent));
                    lore.add(Text.of("<7><m>                              </m><e> {:,}<6>/<e>{:,} XP",
                            owned ? progress : 0, needed));
                    lore.add(Text.empty());

                    // Rewards based on level
                    lore.add(Text.of("<7>Rewards:"));
                    switch (level) {
                        case 1 -> {
                            lore.add(Text.of(" <8>+<6>50,000 <7>SkyWars Coins"));
                            lore.add(Text.of(" <8>+<f>Silver <7>Particle Trail"));
                        }
                        case 2 -> {
                            lore.add(Text.of(" <8>+<6>100,000 <7>SkyWars Coins"));
                            lore.add(Text.of(" <8>+<2>Green <7>Particle Trail"));
                        }
                        case 3 -> {
                            lore.add(Text.of(" <8>+<6>250,000 <7>SkyWars Coins"));
                            lore.add(Text.of(" <8>+<9>Blue <7>Particle Trail"));
                        }
                        case 4 -> {
                            lore.add(Text.of(" <8>+<9>1 <7>Opal"));
                            lore.add(Text.of(" <8>+<5>Purple <7>Particle Trail"));
                        }
                        case 5 -> {
                            lore.add(Text.of(" <8>+<9>1 <7>Opal"));
                            lore.add(Text.of(" <8>+<6>Gold <7>Particle Trail"));
                        }
                        case 6 -> {
                            lore.add(Text.of(" <8>+<9>1 <7>Opal"));
                            lore.add(Text.of(" <8>+<d>Pink <7>Particle Trail"));
                        }
                        case 7 -> {
                            lore.add(Text.of(" <8>+<9>1 <7>Opal"));
                            lore.add(Text.of(" <8>+<c>R<6>a<e>i<a>n<b>b<9>o<5>w <7>Particle Trail"));
                            lore.add(Text.of(" <8>+<3>[<e>9<4>✯] <5>Prestige <7>Scheme"));
                        }
                    }
                    lore.add(Text.empty());
                    lore.add(Text.of("<8>Earn Coins, Opals, Movement Trails"));
                    lore.add(Text.of("<8>and an exclusive Prestige Scheme at"));
                    lore.add(Text.of("<8>max level."));

                    // Determine material and name color
                    Material mat;
                    TextColor nameColor;
                    if (unlocked) {
                        mat = Material.LIME_STAINED_GLASS_PANE;
                        nameColor = NamedTextColor.GREEN;
                    } else if (isNextLevel && owned) {
                        mat = Material.YELLOW_STAINED_GLASS_PANE;
                        nameColor = NamedTextColor.YELLOW;
                    } else {
                        mat = Material.RED_STAINED_GLASS_PANE;
                        nameColor = NamedTextColor.RED;
                    }

                    return ItemStackCreator.item(mat, level,
                            Text.of("<color:{}>Prestige {}", nameColor, PRESTIGE_NAMES[level]), lore);
                }
            });
        }

        // Kit Stats button (slot 30)
        set(new GUIClickableItem(30) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.item(Material.FILLED_MAP, 1, """
                        <a>Kit Stats
                        <7>Access your statistics and challenge
                        <7>completions for this Kit!

                        <e>Click to open!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitStats(kit, mode).open(player);
            }
        });

        // Kit Customizer (slot 32) - only if owned
        set(new GUIItem(32) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (owned) {
                    return ItemStackCreator.item(Material.BLAZE_POWDER, 1, """
                            <c>Kit Customizer
                            <7>Customize the layout of this kit.

                            <c><l>COMING SOON!""");
                } else {
                    return ItemStackCreator.item(Material.BLAZE_POWDER, 1, """
                            <c>Kit Customizer
                            <7>Customize the layout of this kit.

                            <c>You don't own this kit!""");
                }
            }
        });

        // Go Back (slot 48)
        set(new GUIClickableItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String modeName = mode.equals("NORMAL") ? "Normal" : "Insane";
                return ItemStackCreator.item(Material.ARROW, 1, """
                        <a>Go Back
                        <7>To {} Kits""", modeName);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIKitSelector(mode).open(player);
            }
        });

        // Coins display (slot 49)
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.item(Material.EMERALD, 1, """
                        <7>Total Coins: <6>{:,}
                        <6>https://store.hypixel.net""", coins);
            }
        });

        // Favorite toggle (slot 50)
        set(new GUIClickableItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (isFavorite) {
                    return ItemStackCreator.item(Material.LIME_DYE, 1, """
                            <a>Favorite Kit Toggle
                            <7>Kits that have been favorited show
                            <7>up at the top of the Kit Selection
                            <7>menu.

                            <a><l>FAVORITED
</l>
                            <e>Click to unfavorite!""");
                } else {
                    return ItemStackCreator.item(Material.GRAY_DYE, 1, """
                            <c>Favorite Kit Toggle
                            <7>Kits that have been favorited show
                            <7>up at the top of the Kit Selection
                            <7>menu.

                            <c><l>NOT FAVORITED
</l>
                            <e>Click to favorite!""");
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (!owned) {
                    player.sendMessage("<c>You must own this kit to favorite it!");
                    return;
                }

                unlocks.toggleFavorite(kit.getId());
                boolean nowFavorite = unlocks.isFavorite(kit.getId());
                if (nowFavorite) {
                    player.sendMessage("<e>★ <a>Favorited <e>{} <a>kit!", kit.getName());
                } else {
                    player.sendMessage("<7>☆ Unfavorited <e>{} <7>kit.", kit.getName());
                }
                // Refresh GUI
                new GUIKitBreakdown(kit, mode).open(player);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
