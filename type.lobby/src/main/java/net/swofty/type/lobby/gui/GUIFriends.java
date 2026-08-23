package net.swofty.type.lobby.gui;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.friend.Friend;
import net.swofty.commons.friend.FriendData;
import net.swofty.commons.presence.PresenceInfo;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointFriendSort;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.friend.FriendManager;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GUIFriends extends HypixelInventoryGUI {
    private static int[] COLOURED_PANE_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17
    };
    private static final int FRIENDS_PER_PAGE = 18;
    private static final int[] FRIEND_SLOTS = {
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };

    private int currentPage = 1;
    private String searchFilter = null;

    public GUIFriends() {
        super("Friends", InventoryType.CHEST_6_ROW);
    }

    public GUIFriends(int page) {
        super("Friends", InventoryType.CHEST_6_ROW);
        this.currentPage = page;
    }

    public GUIFriends(int page, String searchFilter) {
        super("Friends", InventoryType.CHEST_6_ROW);
        this.currentPage = page;
        this.searchFilter = searchFilter;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();
        PlayerExperienceHandler xpHandler = player.getExperienceHandler();
        int level = xpHandler.getLevel();
        int achievementPoints = player.getAchievementHandler().getTotalPoints();

        for (int slot : COLOURED_PANE_SLOTS) {
            set(new GUIItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.named(Material.MAGENTA_STAINED_GLASS_PANE, "");
                }
            });
        }

        FriendData friendData = FriendManager.getFriendData(player);
        List<Friend> allFriendsUnfiltered = friendData != null ? friendData.getFriends() : new ArrayList<>();

        List<Friend> allFriends;
        if (searchFilter != null && !searchFilter.isEmpty()) {
            String lowerFilter = searchFilter.toLowerCase();
            allFriends = allFriendsUnfiltered.stream()
                    .filter(f -> {
                        try {
                            String rawName = HypixelPlayer.getRawName(f.getUuid());
                            return rawName != null && rawName.toLowerCase().contains(lowerFilter);
                        } catch (Exception ignored) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            allFriends = allFriendsUnfiltered;
        }

        List<UUID> friendUuids = allFriends.stream().map(Friend::getUuid).collect(Collectors.toList());
        List<PresenceInfo> presenceList = FriendManager.getPresenceBulk(friendUuids);
        Map<UUID, PresenceInfo> presenceMap = presenceList.stream()
                .collect(Collectors.toMap(PresenceInfo::getUuid, p -> p, (a, b) -> a));

        DatapointFriendSort.FriendSortData sortData = player.getDataHandler()
                .get(HypixelDataHandler.Data.FRIEND_SORT, DatapointFriendSort.class).getValue();

        List<FriendDisplayEntry> sortedFriends = sortFriends(allFriends, presenceMap, sortData);

        int totalPages = Math.max(1, (int) Math.ceil((double) sortedFriends.size() / FRIENDS_PER_PAGE));
        currentPage = Math.min(currentPage, totalPages);
        int startIndex = (currentPage - 1) * FRIENDS_PER_PAGE;
        int endIndex = Math.min(startIndex + FRIENDS_PER_PAGE, sortedFriends.size());
        List<FriendDisplayEntry> pageEntries = sortedFriends.subList(startIndex, endIndex);

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

        set(new GUIItem(3) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.enchanted(ItemStacks.head("e063eedb2184354bd43a19deffba51b53dd6b7222f8388caa239cabcdce84", """
                        <a>Friends
                        <7>View your Hypixel friends' profiles,
                        <7>and interact with your online friends!

                        <e>Currently viewing!"""));
            }
        });

        set(new GUIClickableItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.head("667963ca1ffdc24a10b397ff8161d0da82d6a3f4788d5f67f1a9f9bfbc1eb1", """
                        <a>Party
                        <7>Create a party and join up with
                        <7>other players to play games
                        <7>together!

                        <e>Click to view!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIParty().open(player);
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

        set(new GUIClickableItem(18) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.WRITABLE_BOOK, """
                        <a>Add Friend
                        <7>Click to add a friend to your friend
                        <7>list.

                        <7>Friends can see what each other
                        <7>are doing on the network, and can
                        <7>see when each other are online.

                        <e>Click to add a friend!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();
                new HypixelSignGUI(player).open(new String[]{"Enter friend", "name above"})
                        .thenAccept(name -> {
                            if (name != null && !name.trim().isEmpty()) {
                                FriendManager.addFriend(player, name.trim());
                            }
                        });
            }
        });

        set(GUIClickableItem.getCloseItem(19));

        set(new GUIClickableItem(25) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                String currentSort = switch (sortData.sortType) {
                    case DEFAULT -> "Default";
                    case ALPHABETICAL -> "Alphabetical";
                    case LAST_ONLINE -> "Last Online";
                };
                String orderText = sortData.reversed ? "Reversed" : "Normal";

                return ItemStacks.item(Material.HOPPER, """
                        <a>Change Sort
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
                        <7>current order!""", currentSort, orderText);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                DatapointFriendSort datapoint = player.getDataHandler()
                        .get(HypixelDataHandler.Data.FRIEND_SORT, DatapointFriendSort.class);
                DatapointFriendSort.FriendSortData data = datapoint.getValue();

                if (e.getClick() instanceof Click.Right) {
                    data.toggleReversed();
                } else {
                    data.cycleSortType();
                }

                datapoint.setValue(data);
                new GUIFriends(currentPage, searchFilter).open(player);
            }
        });

        int matchingCount = allFriends.size();
        set(new GUIClickableItem(26) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (searchFilter != null && !searchFilter.isEmpty()) {
                    return ItemStacks.enchanted(ItemStacks.item(Material.OAK_SIGN, """
                            <a>Search: <e>{0}
                            <7>Currently filtering by: <f>{0}
                            <7>Showing <e>{1}<7> matching friends

                            <e>LEFT CLICK<7> to search for a
                            <7>different player.

                            <e>RIGHT CLICK<7> to clear the
                            <7>search filter.""", searchFilter, matchingCount));
                } else {
                    return ItemStacks.item(Material.OAK_SIGN, """
                            <a>Search Players
                            <7>Search for a player by name
                            <7>in your friends list.

                            <e>Click to search!""");
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.Right && searchFilter != null && !searchFilter.isEmpty()) {
                    new GUIFriends(1).open(player);
                } else {
                    player.closeInventory();
                    new HypixelSignGUI(player).open(new String[]{"Enter player", "name to search"})
                            .thenAccept(name -> {
                                if (name != null && !name.trim().isEmpty()) {
                                    new GUIFriends(1, name.trim()).open(player);
                                } else {
                                    new GUIFriends(1).open(player);
                                }
                            });
                }
            }
        });

        for (int i = 0; i < FRIEND_SLOTS.length; i++) {
            int slot = FRIEND_SLOTS[i];
            if (i < pageEntries.size()) {
                FriendDisplayEntry entry = pageEntries.get(i);
                set(createFriendItem(slot, entry));
            }
        }

        if (currentPage > 1) {
            set(new GUIClickableItem(45) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, """
                            <a>Previous Page
                            <7>Page {}/{}""", currentPage - 1, totalPages);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIFriends(currentPage - 1, searchFilter).open(player);
                }
            });
        }

        if (currentPage < totalPages) {
            set(new GUIClickableItem(53) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.ARROW, """
                            <a>Next Page
                            <7>Page {}/{}""", currentPage + 1, totalPages);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIFriends(currentPage + 1, searchFilter).open(player);
                }
            });
        }

        int totalFriendCount = allFriendsUnfiltered.size();
        set(new GUIItem(49) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                if (searchFilter != null && !searchFilter.isEmpty()) {
                    return ItemStacks.item(Material.BOOK, """
                            <a>Page {}/{}
                            <7>Showing <e>{}<7> of <e>{}<7> friends
                            <7>Searching for: <f>{}""",
                            currentPage, totalPages, matchingCount, totalFriendCount, searchFilter);
                } else {
                    return ItemStacks.item(Material.BOOK, """
                            <a>Page {}/{}
                            <7>Total friends: <e>{}""",
                            currentPage, totalPages, totalFriendCount);
                }
            }
        });

        updateItemStacks(getInventory(), player);
    }

    private List<FriendDisplayEntry> sortFriends(List<Friend> friends, Map<UUID, PresenceInfo> presenceMap,
                                                  DatapointFriendSort.FriendSortData sortData) {
        List<FriendDisplayEntry> entries = friends.stream()
                .map(f -> new FriendDisplayEntry(f, presenceMap.get(f.getUuid())))
                .collect(Collectors.toList());

        Comparator<FriendDisplayEntry> comparator = switch (sortData.sortType) {
            case DEFAULT -> Comparator
                    .comparing((FriendDisplayEntry e) -> !e.isOnline())
                    .thenComparing(e -> e.getDisplayName().plain().toLowerCase());
            case ALPHABETICAL -> Comparator.comparing(e -> e.getDisplayName().plain().toLowerCase());
            case LAST_ONLINE -> Comparator.comparing(FriendDisplayEntry::getLastSeen).reversed();
        };

        if (sortData.reversed) {
            comparator = comparator.reversed();
        }

        entries.sort(comparator);
        return entries;
    }

    private GUIClickableItem createFriendItem(int slot, FriendDisplayEntry entry) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                List<Text> lore = new ArrayList<>();

                try {
                    HypixelDataHandler friendData = HypixelDataHandler.getOfOfflinePlayer(entry.getUuid());
                    long friendLevel = friendData.get(HypixelDataHandler.Data.HYPIXEL_EXPERIENCE,
                            net.swofty.type.generic.data.datapoints.DatapointHypixelExperience.class).getValue();
                    int friendLevelDisplay = (int) (friendLevel / 2500);
                    lore.add(Text.of("<7>Hypixel Level: <6>{}", friendLevelDisplay));
                } catch (Exception e) {
                    lore.add(Text.of("<7>Hypixel Level: <6>?"));
                }

                lore.add(Text.of("<7>Guild: <b>None"));
                lore.add(Text.empty());

                if (entry.isOnline()) {
                    String server = entry.getServerInfo();
                    if (server != null && !server.isEmpty()) {
                        lore.add(Text.of("<a>Online: <e>{}", server));
                    } else {
                        lore.add(Text.of("<a>Online"));
                    }
                } else {
                    lore.add(Text.of("<7>Last Online: <b>{}", formatLastSeen(entry.getLastSeen())));
                }

                if (entry.isBestFriend()) {
                    lore.add(Text.empty());
                    lore.add(Text.of("<6>Best Friend"));
                }

                lore.add(Text.empty());
                lore.add(Text.of("<e>Left-click to view profile"));
                lore.add(Text.of("<e>Shift-click to remove friend"));

                String namePrefix = entry.isOnline() ? "<a>" : "<7>";
                Text displayName = Text.of((entry.isBestFriend() ? "<6>" : "") + namePrefix + "{}",
                        entry.getDisplayName());

                PlayerSkin skin = getFriendSkin(entry.getUuid());
                if (skin != null) {
                    return ItemStacks.head(skin, displayName, lore);
                } else {
                    return ItemStacks.head("8667ba71b85a4004af54457a9734eed7e09dcc6abe4dd49f4c11d4c8e3c91cfe",
                            displayName, lore);
                }
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                if (e.getClick() instanceof Click.LeftShift || e.getClick() instanceof Click.RightShift) {
                    FriendManager.removeFriend(player, entry.getRawName());
                    player.sendMessage("<c>Removing friend...");
                    new GUIFriends(currentPage, searchFilter).open(player);
                } else {
                    player.sendMessage("<e>Viewing profile of <f>{}<e>...", entry.getRawName());
                }
            }
        };
    }

    private PlayerSkin getFriendSkin(UUID uuid) {
        try {
            HypixelDataHandler friendData = HypixelDataHandler.getOfOfflinePlayer(uuid);
            String texture = friendData.get(HypixelDataHandler.Data.SKIN_TEXTURE, DatapointString.class).getValue();
            String signature = friendData.get(HypixelDataHandler.Data.SKIN_SIGNATURE, DatapointString.class).getValue();

            if (texture != null && !texture.equals("null") && !texture.isEmpty()) {
                return new PlayerSkin(texture, signature != null && !signature.equals("null") ? signature : null);
            }
        } catch (Exception e) {
            // Ignore and return null
        }
        return null;
    }

    private String formatLastSeen(long lastSeenTimestamp) {
        if (lastSeenTimestamp <= 0) {
            return "Unknown";
        }

        long secondsAgo = (System.currentTimeMillis() - lastSeenTimestamp) / 1000;

        if (secondsAgo < 60) {
            return secondsAgo + " seconds ago";
        }

        long minutesAgo = secondsAgo / 60;
        if (minutesAgo < 60) {
            return minutesAgo + " minute" + (minutesAgo != 1 ? "s" : "") + " ago";
        }

        long hoursAgo = minutesAgo / 60;
        if (hoursAgo < 24) {
            long remainingMinutes = minutesAgo % 60;
            return hoursAgo + " hour" + (hoursAgo != 1 ? "s" : "") + ", " + remainingMinutes + " minute" + (remainingMinutes != 1 ? "s" : "") + " ago";
        }

        long daysAgo = hoursAgo / 24;
        if (daysAgo < 30) {
            return daysAgo + " day" + (daysAgo != 1 ? "s" : "") + " ago";
        }

        long monthsAgo = daysAgo / 30;
        return monthsAgo + " month" + (monthsAgo != 1 ? "s" : "") + " ago";
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    private static class FriendDisplayEntry {
        private final Friend friend;
        private final PresenceInfo presence;
        private Text cachedDisplayName;
        private String cachedRawName;

        public FriendDisplayEntry(Friend friend, PresenceInfo presence) {
            this.friend = friend;
            this.presence = presence;
        }

        public UUID getUuid() {
            return friend.getUuid();
        }

        public boolean isBestFriend() {
            return friend.isBestFriend();
        }

        public boolean isOnline() {
            return presence != null && presence.isOnline();
        }

        public long getLastSeen() {
            if (presence != null) {
                return presence.getLastSeen();
            }
            return friend.getAddedTimestamp();
        }

        public String getServerInfo() {
            if (presence != null && presence.getServerType() != null) {
                String serverType = presence.getServerType();
                String serverId = presence.getServerId();
                if (serverId != null && !serverId.isEmpty()) {
                    return serverType + " " + serverId;
                }
                return serverType;
            }
            return null;
        }

        public Text getDisplayName() {
            if (cachedDisplayName == null) {
                try {
                    cachedDisplayName = HypixelPlayer.getDisplayName(friend.getUuid());
                    if (cachedDisplayName == null || cachedDisplayName.isEmpty()) {
                        cachedDisplayName = Text.literal(getRawName());
                    }
                } catch (Exception e) {
                    cachedDisplayName = Text.literal(getRawName());
                }
            }
            return cachedDisplayName;
        }

        public String getRawName() {
            if (cachedRawName == null) {
                try {
                    cachedRawName = HypixelPlayer.getRawName(friend.getUuid());
                } catch (Exception e) {
                    cachedRawName = friend.getUuid().toString().substring(0, 8);
                }
            }
            return cachedRawName;
        }
    }
}
