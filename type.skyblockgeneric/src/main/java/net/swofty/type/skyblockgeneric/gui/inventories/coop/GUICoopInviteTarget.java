package net.swofty.type.skyblockgeneric.gui.inventories.coop;

import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.ProfileSwitcher;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GUICoopInviteTarget extends HypixelInventoryGUI {
    private static final Map<Integer, List<Integer>> SLOTS_MAP = new HashMap<>(
            Map.of(
                    1, List.of(13),
                    2, List.of(12, 14),
                    3, List.of(11, 13, 15),
                    4, List.of(10, 12, 14, 16),
                    5, List.of(11, 12, 13, 14, 15)
            )
    );

    public GUICoopInviteTarget(CoopDatabase.Coop coop) {
        super(Text.key("gui_coop.target.title"), InventoryType.CHEST_5_ROW);

        UUID coopId = coop.coopUUID();

        fill(ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, ""));

        int amountInProfile = coop.memberInvites().size() + coop.members().size();
        int[] slots = SLOTS_MAP.get(Math.clamp(amountInProfile, 1, 5)).stream().mapToInt(Integer::intValue).toArray();

        // Put everyone who is a member as TRUE and ones only invited as FALSE
        Map<UUID, Boolean> invites = new HashMap<>();
        coop.members().forEach(uuid -> invites.put(uuid, true));
        coop.memberInvites().forEach(uuid -> invites.put(uuid, false));

        // Remove originator
        invites.remove(coop.originator());

        set(new GUIItem(slots[0]) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.head(PlayerSkin.fromUuid(String.valueOf(coop.originator())),
                        SkyBlockPlayer.getDisplayName(coop.originator()),
                        Text.keyLines("gui_coop.target.originator_head.lore"));
            }
        });

        for (int i = 0; i < invites.size() && i + 1 < slots.length; i++) {
            UUID target = (UUID) invites.keySet().toArray()[i];
            boolean accepted = invites.get(target);
            Text displayName = SkyBlockPlayer.getDisplayName(target);

            set(new GUIItem(slots[i + 1]) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    Text status = accepted ? Text.key("gui_coop.sender.accepted_yes") : Text.key("gui_coop.sender.accepted_no");
                    return ItemStacks.head(PlayerSkin.fromUuid(String.valueOf(target)), displayName,
                            List.of(Text.literal(" "), Text.key("gui_coop.sender.player_accepted", status)));
                }
            });
        }

        set(new GUIClickableItem(33) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                CoopDatabase.update(coopId, latest -> latest.removeInvite(player.getUuid()));
                player.sendMessage(Text.key("gui_coop.target.denied_message"));
                player.closeInventory();

                notifyOriginator(coop.originator(), Text.key("gui_coop.target.denied_notify", player.getUsername()));
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BARRIER, 1, "<key:gui_coop.target.deny_button>");
            }
        });

        set(new GUIClickableItem(29) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;

                CoopDatabase.Coop joined = CoopDatabase.update(coopId, latest -> {
                    latest.removeInvite(player.getUuid());
                    if (!latest.members().contains(player.getUuid())) latest.members().add(player.getUuid());
                });
                if (joined == null) {
                    player.sendMessage("<b>[Co-op] <c>That co-op no longer exists!");
                    player.closeInventory();
                    return;
                }

                UUID profileId = CoopProfileCreation.create(player, joined);
                CoopDatabase.update(coopId, latest -> latest.memberProfiles().add(profileId));

                SkyBlockPlayerProfiles profiles = player.getProfiles();
                profiles.addProfile(profileId);
                new UserDatabase(player.getUuid()).saveProfiles(profiles);

                notifyOriginator(coop.originator(), Text.key("gui_coop.target.accepted_notify", player.getUsername()));
                player.closeInventory();
                ProfileSwitcher.switchTo(player, profileId);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return ItemStacks.item(Material.GREEN_TERRACOTTA, 1, Text.key("gui_coop.target.accept_button"),
                        Text.keyLines("gui_coop.target.accept_button.lore", player.getProfiles().getProfiles().size()));
            }
        });
    }

    private static void notifyOriginator(UUID originator, Text message) {
        SkyBlockPlayer target = SkyBlockGenericLoader.getLoadedPlayers().stream()
                .filter(loaded -> loaded.getUuid().equals(originator))
                .findFirst().orElse(null);
        if (target != null) target.sendMessage(message);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {

    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {

    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
