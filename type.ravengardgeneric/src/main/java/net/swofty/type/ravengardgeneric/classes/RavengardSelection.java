package net.swofty.type.ravengardgeneric.classes;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.ravengardgeneric.item.RavengardItem;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;

public final class RavengardSelection {
    public static final Pos MAIN_WORLD_SPAWN = new Pos(47.5, 61, 121.5, 180f, 0f);

    private RavengardSelection() {
    }

    public static void choose(RavengardPlayer player, RavengardClass value) {
        player.setRavengardClass(value);
        player.setTutorial(false);

        player.closeInventory();
        player.sendMessage("<a>You have selected the {} class!", value.getDisplayName());

        giveKit(player, value);
        net.swofty.type.ravengardgeneric.profile.RavengardProfiles.saveActive(player);

        player.setInstance(HypixelConst.getInstanceContainer(), MAIN_WORLD_SPAWN);
    }

    /**
     * Every profile has the four accessory slot panes, class or not -- a fresh tutorial inventory
     * on the live server carries them already.
     */
    public static void giveAccessorySlots(RavengardPlayer player) {
        player.getInventory().setItemStack(RavengardKit.SLOT_NECK, RavengardItem.of("NECK_SLOT"));
        player.getInventory().setItemStack(RavengardKit.SLOT_EARRING, RavengardItem.of("EARRING_SLOT"));
        player.getInventory().setItemStack(RavengardKit.SLOT_BELT, RavengardItem.of("BELT_SLOT"));
        player.getInventory().setItemStack(RavengardKit.SLOT_RING, RavengardItem.of("RING_SLOT"));
    }

    public static void giveKit(RavengardPlayer player, RavengardClass value) {
        RavengardKit kit = RavengardKit.forClass(value);
        if (kit == null) {
            return;
        }

        // armour comes out of the item registry so the config owns models, assets and stats
        player.getInventory().setItemStack(RavengardKit.SLOT_CHEST, RavengardItem.of(kit.chestId(), player));
        player.getInventory().setItemStack(RavengardKit.SLOT_LEGS, RavengardItem.of(kit.legsId(), player));
        player.getInventory().setItemStack(RavengardKit.SLOT_BOOTS, RavengardItem.of(kit.bootsId(), player));
        int hotbarSlot = 0;
        for (String itemId : kit.hotbarIds()) {
            player.getInventory().setItemStack(hotbarSlot++, RavengardItem.of(itemId, player));
        }
        giveAccessorySlots(player);

        List<RavengardAbility> abilities = value.defaultAbilities();
        int[] slots = {RavengardKit.SLOT_ABILITY_ONE, RavengardKit.SLOT_ABILITY_TWO};
        for (int index = 0; index < slots.length && index < abilities.size(); index++) {
            player.getInventory().setItemStack(slots[index], RavengardKit.ability(abilities.get(index), slots[index]));
        }
    }
}
