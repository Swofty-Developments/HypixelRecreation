package net.swofty.type.ravengardgeneric.profile;

import net.kyori.adventure.nbt.TagStringIO;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.ItemStack;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.ravengardgeneric.RavengardGenericLoader;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.classes.RavengardSelection;
import net.swofty.type.ravengardgeneric.data.RavengardDataHandler;
import net.swofty.type.ravengardgeneric.data.RavengardProfileIndex;
import net.swofty.type.ravengardgeneric.data.RavengardProfileMigration;
import net.swofty.type.ravengardgeneric.data.RavengardProfileStorage;
import net.swofty.type.ravengardgeneric.item.RavengardMenuItem;
import net.swofty.type.ravengardgeneric.region.RavengardRegionType;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RavengardProfiles {
    public static final int MAX_PROFILES = 5;
    public static final Pos TUTORIAL_SPAWN = new Pos(25.5, 64, 508.5, -90f, 0f);

    private static final Map<UUID, Long> SESSION_STARTED = new ConcurrentHashMap<>();

    private RavengardProfiles() {
    }

    public static List<RavengardProfile> list(RavengardPlayer player) {
        UUID selected = player.getSelectedProfile();
        List<RavengardProfile> profiles = new ArrayList<>();

        for (UUID profileId : RavengardProfileIndex.read(player.getUuid()).profiles()) {
            RavengardProfile profile = RavengardProfileStorage.byId(profileId);
            if (profile == null || !profile.getOwner().equals(player.getUuid())) continue;
            if (profileId.equals(selected)) overlayLiveValues(player, profile);
            profiles.add(profile);
        }
        return profiles;
    }

    public static @Nullable UUID ensure(RavengardPlayer player) {
        RavengardDataHandler handler = player.getRavengardDataHandler();
        if (handler == null) return null;

        UUID selected = handler.getCurrentProfileId();
        if (selected != null && player.getUuid().equals(RavengardProfileStorage.owner(selected))) {
            beginSession(player);
            return selected;
        }

        RavengardProfileIndex index = RavengardProfileIndex.read(player.getUuid());
        UUID target = firstOwned(player, index);
        if (target == null) {
            target = createProfile(player, index);
            Logger.info("Created first Ravengard profile {} for {}", target, player.getUsername());
        } else {
            RavengardProfileIndex.write(player.getUuid(), index.withSelected(target));
        }

        handler.attachProfile(target);
        beginSession(player);
        return target;
    }

    public static void announce(RavengardPlayer player) {
        UUID profileId = ensure(player);
        if (profileId == null) return;

        player.sendMessage("<8>Profile ID: {}", profileId);
        player.sendMessage("       ");
        restoreInventory(player, profileId);
    }

    public static void create(RavengardPlayer player) {
        RavengardProfileIndex index = RavengardProfileIndex.read(player.getUuid());
        if (index.profiles().size() >= MAX_PROFILES) {
            player.sendMessage("<c>You already have the maximum number of profiles!");
            return;
        }

        saveActive(player);
        player.sendMessage("<7>Creating profile...");

        UUID profileId = createProfile(player, index);

        player.sendMessage("<7>Successfully created profile!");
        player.sendMessage("<8>Profile ID: {}", profileId);
        player.sendMessage("       ");

        activate(player, profileId);
    }

    public static void select(RavengardPlayer player, UUID profileId) {
        RavengardProfileIndex index = RavengardProfileIndex.read(player.getUuid());
        if (!index.contains(profileId) || !player.getUuid().equals(RavengardProfileStorage.owner(profileId))) {
            player.sendMessage("<c>That profile no longer exists.");
            return;
        }
        if (profileId.equals(player.getSelectedProfile())) {
            return;
        }

        saveActive(player);
        player.sendMessage("<8>Profile ID: {}", profileId);
        player.sendMessage("       ");
        activate(player, profileId);
    }

    public static void delete(RavengardPlayer player, UUID profileId) {
        RavengardProfileIndex index = RavengardProfileIndex.read(player.getUuid());
        if (!index.contains(profileId) || !player.getUuid().equals(RavengardProfileStorage.owner(profileId))) {
            return;
        }

        RavengardDataHandler handler = player.getRavengardDataHandler();
        boolean wasSelected = profileId.equals(player.getSelectedProfile());
        if (wasSelected && handler != null) handler.attachProfile(null);

        RavengardProfileIndex remaining = index.without(profileId);
        RavengardProfileIndex.write(player.getUuid(), remaining);
        RavengardProfileStorage.delete(profileId);
        player.sendMessage("<7>Successfully deleted profile!");

        if (!wasSelected) {
            return;
        }

        if (remaining.profiles().isEmpty()) {
            create(player);
            return;
        }

        UUID next = remaining.profiles().getFirst();
        player.sendMessage("<8>Profile ID: {}", next);
        player.sendMessage("       ");
        activate(player, next);
    }

    public static void saveActive(RavengardPlayer player) {
        RavengardDataHandler handler = player.getRavengardDataHandler();
        UUID profileId = handler == null ? null : handler.getCurrentProfileId();
        if (profileId == null || !player.getUuid().equals(RavengardProfileStorage.owner(profileId))) {
            SESSION_STARTED.remove(player.getUuid());
            return;
        }

        handler.saveBackedData();
        RavengardProfileStorage.addPlaytime(profileId, endSession(player));
        RavengardProfileStorage.writeInventory(profileId, snapshotInventory(player));
        beginSession(player);
    }

    public static void endSessionAndSave(RavengardPlayer player) {
        saveActive(player);
        SESSION_STARTED.remove(player.getUuid());
    }

    public static boolean hasIntro(RavengardPlayer player, String npc) {
        return RavengardProfileStorage.hasIntro(player.getSelectedProfile(), npc);
    }

    public static void markIntro(RavengardPlayer player, String npc) {
        RavengardProfileStorage.addIntro(player.getSelectedProfile(), npc);
    }

    public static boolean hasDiscovered(RavengardPlayer player, RavengardRegionType region) {
        return RavengardProfileStorage.hasDiscoveredRegion(player.getSelectedProfile(), region.name());
    }

    public static void markDiscovered(RavengardPlayer player, RavengardRegionType region) {
        RavengardProfileStorage.addDiscoveredRegion(player.getSelectedProfile(), region.name());
    }

    public static int getCrowns(RavengardPlayer player) {
        return player.getCrowns();
    }

    public static void setCrowns(RavengardPlayer player, int amount) {
        player.setCrowns(amount);
        saveActive(player);
    }

    public static void addCrowns(RavengardPlayer player, int amount) {
        player.setCrowns(player.getCrowns() + amount);
        saveActive(player);
    }

    public static boolean tryPurchase(RavengardPlayer player, int price) {
        if (player.getCrowns() < price) {
            return false;
        }
        player.setCrowns(player.getCrowns() - price);
        saveActive(player);
        return true;
    }

    public static Map<Integer, String> snapshotInventory(RavengardPlayer player) {
        Map<Integer, String> inventory = new HashMap<>();
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir()) {
                continue;
            }
            try {
                inventory.put(slot, TagStringIO.tagStringIO().asString(stack.toItemNBT()));
            } catch (Exception exception) {
                Logger.warn(exception, "Could not serialize inventory slot {} for {}", slot, player.getUsername());
            }
        }
        return inventory;
    }

    public static void restoreInventory(RavengardPlayer player, UUID profileId) {
        Map<Integer, String> inventory = RavengardProfileStorage.readInventory(profileId);
        player.getInventory().clear();

        if (!inventory.isEmpty()) {
            inventory.forEach((slot, snbt) -> {
                try {
                    player.getInventory().setItemStack(slot,
                            ItemStack.fromItemNBT(TagStringIO.tagStringIO().asCompound(snbt)));
                } catch (Exception exception) {
                    Logger.warn(exception, "Could not restore inventory slot {} for {}", slot, player.getUsername());
                }
            });
            RavengardMenuItem.give(player);
            return;
        }

        player.sendMessage("<7>Restoring default equipment...");
        RavengardClass profileClass = player.getRavengardClass();
        if (profileClass != null) {
            RavengardSelection.giveKit(player, profileClass);
        } else {
            RavengardSelection.giveAccessorySlots(player);
        }
        RavengardMenuItem.give(player);
    }

    private static void activate(RavengardPlayer player, UUID profileId) {
        RavengardDataHandler handler = player.getRavengardDataHandler();
        if (handler == null) return;

        handler.attachProfile(profileId);
        RavengardProfileIndex.write(player.getUuid(),
                RavengardProfileIndex.read(player.getUuid()).withSelected(profileId));
        beginSession(player);

        restoreInventory(player, profileId);

        RavengardClass profileClass = player.getRavengardClass();
        boolean tutorial = player.isTutorial();
        player.closeInventory();

        Instance target = tutorial || profileClass == null
                ? RavengardGenericLoader.tutorialInstance
                : HypixelConst.getInstanceContainer();
        Pos spawn = tutorial || profileClass == null
                ? TUTORIAL_SPAWN
                : RavengardSelection.MAIN_WORLD_SPAWN;
        if (target != null && player.getInstance() != null) {
            if (player.getInstance() == target) {
                player.teleport(spawn);
            } else {
                player.setInstance(target, spawn);
            }
        }
    }

    private static UUID createProfile(RavengardPlayer player, RavengardProfileIndex index) {
        boolean firstEver = index.profiles().isEmpty() && !RavengardProfileIndex.isStored(player.getUuid());

        UUID profileId = UUID.randomUUID();
        RavengardProfileStorage.create(profileId, player.getUuid());
        if (firstEver) RavengardProfileMigration.seedFromLegacyAccount(player.getUuid(), profileId);
        RavengardProfileIndex.write(player.getUuid(), index.withSelected(profileId));
        return profileId;
    }

    private static @Nullable UUID firstOwned(RavengardPlayer player, RavengardProfileIndex index) {
        UUID selected = index.selected();
        if (selected != null && player.getUuid().equals(RavengardProfileStorage.owner(selected))) {
            return selected;
        }
        for (UUID profileId : index.profiles()) {
            if (player.getUuid().equals(RavengardProfileStorage.owner(profileId))) return profileId;
        }
        return null;
    }

    private static void overlayLiveValues(RavengardPlayer player, RavengardProfile profile) {
        profile.setProfileClass(player.getRavengardClass());
        profile.setLevel(player.getRavengardLevel());
        profile.setCrowns(player.getCrowns());
        profile.setTutorial(player.isTutorial());
    }

    private static void beginSession(RavengardPlayer player) {
        SESSION_STARTED.put(player.getUuid(), System.currentTimeMillis());
    }

    private static long endSession(RavengardPlayer player) {
        Long started = SESSION_STARTED.remove(player.getUuid());
        return started == null ? 0 : Math.max(0, (System.currentTimeMillis() - started) / 1000);
    }
}
