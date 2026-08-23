package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.profiles;

import lombok.SneakyThrows;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.item.crafting.SkyBlockRecipe;
import net.swofty.type.skyblockgeneric.user.ProfileMode;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIProfileManagement extends StatelessView {
    private static final int[] PROFILE_SLOTS = {11, 12, 13, 14, 15};

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.profiles.management.title", InventoryType.CHEST_4_ROW);
    }

    @SneakyThrows
    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        SkyBlockPlayerProfiles profiles = player.getProfiles();
        List<UUID> profileIds = profiles.getProfiles();

        for (int profileCount = 0; profileCount <= 4; profileCount++) {
            int slot = PROFILE_SLOTS[profileCount];

            if (profileIds.size() <= profileCount) {
                // Empty profile slot
                layout.slot(slot, (s, c) -> ItemStacks.item(Material.OAK_BUTTON, 1, Text.key("gui_sbmenu.profiles.empty_slot"),
                                Text.keyLines("gui_sbmenu.profiles.empty_slot.lore")),
                        (click, c) -> c.player().openView(new GUIProfileSelectMode()));
                continue;
            }

            UUID profileId = profileIds.get(profileCount);
            boolean selected = profileId.equals(profiles.getCurrentlySelected());
            SkyBlockDataHandler dataHandler;

            if (selected) {
                dataHandler = SkyBlockDataHandler.getUser(player.getUuid());
            } else {
                try {
                    ProfilesDatabase profileDb = new ProfilesDatabase(profileId.toString());
                    dataHandler = SkyBlockDataHandler.createFromProfileOnly(profileDb.getDocument());
                } catch (NullPointerException profileNotYetSaved) {
                    dataHandler = SkyBlockDataHandler.initUserWithDefaultData(player.getUuid(), profileId);
                }
            }

            SkyBlockDataHandler finalDataHandler = dataHandler;

            if (selected) {
                layout.slot(slot, (s, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    List<Text> lore = new ArrayList<>(List.of(Text.key("gui_sbmenu.profiles.selected.subtitle"), Text.literal(" ")));
                    updateLore(profileId, finalDataHandler, lore);
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_sbmenu.profiles.selected.playing"));

                    String profileName = finalDataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();
                    return ItemStacks.item(Material.EMERALD_BLOCK, 1, Text.key("gui_sbmenu.profiles.selected", profileName), lore);
                }, (click, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    String profileName = finalDataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();
                    p.sendMessage(Text.key("gui_sbmenu.profiles.msg.playing_on", profileName));
                    p.sendMessage(Text.key("gui_sbmenu.profiles.msg.switch_first"));
                });
            } else {
                layout.slot(slot, (s, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    List<Text> lore = new ArrayList<>(List.of(Text.key("gui_sbmenu.profiles.unselected.subtitle"), Text.literal(" ")));
                    updateLore(profileId, finalDataHandler, lore);
                    lore.add(Text.literal(" "));
                    lore.add(Text.key("gui_sbmenu.profiles.unselected.click"));

                    String profileName = finalDataHandler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue();
                    return ItemStacks.item(Material.GRASS_BLOCK, 1, Text.key("gui_sbmenu.profiles.unselected", profileName), lore);
                }, (click, c) -> c.player().openView(new GUIProfileSelect(profileId)));
            }
        }
    }

    public static List<Text> updateLore(UUID profileUuid, SkyBlockDataHandler handler, List<Text> lore) {
        ProfileMode mode = ProfileMode.fromStored(
                handler.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class).getValue());
        if (mode != ProfileMode.CLASSIC) {
            lore.add(Text.of("<7>Mode: ").append(mode.getDisplayName()));
            lore.add(Text.literal(" "));
        }
        if (handler.get(SkyBlockDataHandler.Data.IS_COOP, DatapointBoolean.class).getValue()) {
            CoopDatabase.Coop coop = CoopDatabase.getFromMemberProfile(profileUuid);
            if (coop != null) {
                lore.add(Text.of("<b>Co-op with <e>{} <b>players:", coop.members().size()));
                coop.members().forEach(member -> lore.add(Text.of(" <7>- ").append(SkyBlockPlayer.getDisplayName(member))));
                lore.add(Text.literal(" "));
            }
        }

        List<String> missionLore = new ArrayList<>();
        SkyBlockRecipe.getMissionDisplay(missionLore, handler.getUuid());
        missionLore.forEach(line -> lore.add(Text.parse(line)));
        lore.add(Text.literal(" "));

        lore.add(Text.key("gui_sbmenu.profiles.no_skills"));
        lore.add(Text.literal(" "));

        Double coins = handler.get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class).getValue();
        if (coins > 0) {
            lore.add(Text.key("gui_sbmenu.profiles.purse_coins", coins));
        }

        Long createdTime = handler.get(SkyBlockDataHandler.Data.CREATED, DatapointLong.class).getValue();
        String age = StringUtility.profileAge(System.currentTimeMillis() - createdTime);
        lore.add(Text.key("gui_sbmenu.profiles.age", age));

        return lore;
    }
}
