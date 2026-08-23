package net.swofty.type.lobby.gui;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointAchievementData;
import net.swofty.type.generic.data.datapoints.DatapointHypixelExperience;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.experience.HypixelExperience;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class GUIParty extends HypixelInventoryGUI implements RefreshingGUI {
    private static int[] COLOURED_PANE_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17
    };

    private SortMode sortMode = SortMode.DEFAULT;
    private boolean reverseSorting = false;

    public GUIParty() {
        super("Party", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        refreshItems(e.player());
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        items.clear();

        PlayerExperienceHandler xpHandler = player.getExperienceHandler();
        int level = xpHandler.getLevel();
        int achievementPoints = player.getAchievementHandler().getTotalPoints();

        for (int slot : COLOURED_PANE_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.named(Material.BLUE_STAINED_GLASS_PANE, "");
                }
            });
        }

        setTopRowItems(player, level, achievementPoints);

        boolean inParty = PartyManager.isInParty(player);

        if (!inParty) {
            setNotInPartyItems(player);
        } else {
            FullParty party = PartyManager.getPartyFromPlayer(player);
            if (party != null) {
                setInPartyItems(player, party);
            }
        }
    }

    private void setTopRowItems(HypixelPlayer player, int level, int achievementPoints) {
        set(new GUIClickableItem(2) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head(
                        player.getSkin(),
                        player.getFullDisplayName(),
                        Text.of("""
                                <7>Hypixel Level: <6>{}
                                <7>Achievement Points: <e>{:,}
                                <7>Guild: <b>None

                                <e>Click to go back!""", level, achievementPoints).lines()
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIMyProfile().open(player);
            }
        });

        set(new GUIClickableItem(3) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84", """
                        <a>Friends
                        <7>View your Hypixel friends' profiles,
                        <7>and interact with your online friends!

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIFriends().open(player);
            }
        });

        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.enchanted(ItemStacks.head("667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1", """
                        <a>Party
                        <7>Create a party and join up with
                        <7>other players to play games
                        <7>together!

                        <e>Currently viewing!"""));
            }
        });

        set(new GUIItem(5) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("fe8b59f8cce510809427c3843cf575fae8fe6a8b7d1560dd46958d148563815", """
                        <a>Guild
                        <7>Form a guild with other Hypixel
                        <7>players to conquer game modes and
                        <7>work towards common Hypixel
                        <7>rewards.""");
            }
        });

        set(new GUIItem(6) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("9993a356809532d696841a37a0549b81b159b79a7b2919cff4e5abdfea83d66", """
                        <a>Recent Players
                        <7>View players you have played recent
                        <7>games with.""");
            }
        });
    }

    private void setNotInPartyItems(HypixelPlayer player) {
        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.MAGENTA_TERRACOTTA, """
                        <a>Create Party

                        <e>Click to invite a player to your
                        <e>party""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();
                new HypixelSignGUI(player).open(new String[]{"Enter player", "name:"}).thenAccept(name -> {
                    if (name != null && !name.isEmpty()) {
                        PartyManager.invitePlayer(player, name);
                    }
                });
            }
        });
    }

    private void setInPartyItems(HypixelPlayer player, FullParty party) {
        FullParty.Member self = party.getFromUuid(player.getUuid());
        boolean isLeader = self.getRole() == FullParty.Role.LEADER;
        boolean isModerator = self.getRole() == FullParty.Role.MODERATOR;
        boolean hasModPermissions = isLeader || isModerator;

        set(new GUIClickableItem(18) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WRITABLE_BOOK, """
                        <a>Invite Player
                        <7>Invites a player to your party.""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();
                new HypixelSignGUI(player).open(new String[]{"Enter player", "name:"}).thenAccept(name -> {
                    if (name != null && !name.isEmpty()) {
                        PartyManager.invitePlayer(player, name);
                    }
                });
            }
        });

        set(GUIClickableItem.getCloseItem(19));

        if (isLeader) {
            set(new GUIClickableItem(20) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.NETHER_BRICK, """
                            <a>Warp Party
                            <7>Teleports all party members to your
                            <7>lobby.""");
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    PartyManager.warpParty(player);
                }
            });

            set(new GUIClickableItem(21) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.TNT, """
                            <a>Disband Party
                            <7>Breaks up the current party.""");
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    PartyManager.disbandParty(player);
                    player.closeInventory();
                }
            });
        } else {
            set(new GUIClickableItem(20) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.BARRIER, """
                            <c>Leave Party
                            <7>Leave the current party.""");
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    PartyManager.leaveParty(player);
                    player.closeInventory();
                }
            });
        }

        set(new GUIClickableItem(25) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String sortingOrder = reverseSorting ? "Reversed" : "Normal";
                return ItemStacks.item(Material.HOPPER, """
                        <a>Change sort
                        <7>Current sort: <b>{}
                        <7>Sorting order: <b>{}

                        <b>Default<7>: Alphabetical order, but
                        <7>show online players first
                        <b>Alphabetical<7>: Show everyone
                        <7>listed from A-Z
                        <b>Last Online<7>: Sorts by who was
                        <7>most recently online

                        <e>LEFT CLICK<7> to change between
                        <7>all the available sorting options.

                        <e>RIGHT CLICK<7> to reverse the
                        <7>current order!""", sortMode.getDisplayName(), sortingOrder);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Right) {
                    reverseSorting = !reverseSorting;
                } else {
                    sortMode = sortMode.next();
                }
                refreshItems(player);
                updateItemStacks(getInventory(), player);
            }
        });

        set(new GUIItem(26) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, "<a>Search Players");
            }
        });

        List<FullParty.Member> members = new ArrayList<>(party.getMembers());
        sortMembers(members);

        int slot = 27;
        for (FullParty.Member member : members) {
            if (slot > 44) break;

            final int currentSlot = slot;
            set(new GUIItem(currentSlot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return createMemberHead(member);
                }
            });
            slot++;
        }
    }

    private ItemStack.Builder createMemberHead(FullParty.Member member) {
        UUID memberUuid = member.getUuid();
        Text displayName = HypixelPlayer.getDisplayName(memberUuid);

        HypixelDataHandler account = HypixelDataHandler.getOfOfflinePlayer(memberUuid);
        long xp = account.get(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE, DatapointHypixelExperience.class).getValue();
        int level = HypixelExperience.xpToLevel(xp);
        int achievementPoints = account.get(HypixelDataHandler.Data.ACHIEVEMENT_DATA, DatapointAchievementData.class)
                .getValue()
                .getTotalPoints();

        String skinTexture = account.get(HypixelDataHandler.Data.SKIN_TEXTURE, DatapointString.class).getValue();
        String skinSignature = account.get(HypixelDataHandler.Data.SKIN_SIGNATURE, DatapointString.class).getValue();

        Text role = switch (member.getRole()) {
            case LEADER -> Text.of("<6>Party Leader");
            case MODERATOR -> Text.of("<9>Party Moderator");
            default -> Text.of("<7>Party Member");
        };

        List<Text> lore = Text.of("""
                <7>Hypixel Level: <6>{}
                <7>Achievement Points: <e>{:,}
                <7>Guild: <b>None

                {}""", level, achievementPoints, role).lines();

        if (skinTexture != null && !skinTexture.equals("null") && skinSignature != null && !skinSignature.equals("null")) {
            PlayerSkin skin = new PlayerSkin(skinTexture, skinSignature);
            return ItemStacks.head(skin, displayName, lore);
        } else {
            return ItemStacks.head("18614241b980319c02f5ee3ae1a7fc7ebf8b3fdd5301ed3d4e2159a80dae1d2c",
                    displayName, lore);
        }
    }

    private void sortMembers(List<FullParty.Member> members) {
        Comparator<FullParty.Member> comparator = switch (sortMode) {
            case ALPHABETICAL -> Comparator.comparing(m -> HypixelPlayer.getRawName(m.getUuid()).toLowerCase());
            case LAST_ONLINE -> Comparator.comparing(m -> m.isJoined() ? 0 : 1);
            default -> {
                Comparator<FullParty.Member> onlineFirst = Comparator.comparing(m -> m.isJoined() ? 0 : 1);
                Comparator<FullParty.Member> alphabetical = Comparator.comparing(m -> HypixelPlayer.getRawName(m.getUuid()).toLowerCase());
                yield onlineFirst.thenComparing(alphabetical);
            }
        };

        if (reverseSorting) {
            comparator = comparator.reversed();
        }

        members.sort(comparator);
    }

    @Override
    public int refreshRate() {
        return 20;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    private enum SortMode {
        DEFAULT("Default"),
        ALPHABETICAL("Alphabetical"),
        LAST_ONLINE("Last Online");

        private final String displayName;

        SortMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SortMode next() {
            SortMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}
