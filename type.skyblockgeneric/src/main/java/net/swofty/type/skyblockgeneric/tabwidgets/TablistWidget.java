package net.swofty.type.skyblockgeneric.tabwidgets;

import net.minestom.server.item.Material;

import java.util.EnumSet;
import java.util.Set;

public enum TablistWidget {
    GENERAL_INFO("General Info", Material.OAK_SIGN, "Shows general information for this area."),
    PROFILE("Profile", Material.NAME_TAG, "Shows your profile and account information."),
    PET("Pet", Material.BONE, "Shows information about your equipped pet."),
    FIRE_SALES("Fire Sales", Material.BLAZE_POWDER, "Shows if there are active Fire Sales."),
    ELECTION("Election", Material.JUKEBOX, "Shows information about Mayor Elections."),
    EVENTS("Events", Material.CAKE, "Shows upcoming and current events."),
    SKILLS("Skills", Material.DIAMOND_SWORD, "Shows your skill levels and progress."),
    STATS("Stats", Material.BOOK, "Shows selected player stats."),
    BESTIARY("Bestiary", Material.PLAYER_HEAD, "Shows this location's Bestiary progress."),
    COLLECTIONS("Collections", Material.PAINTING, "Shows collection progress."),
    DAILY_QUESTS("Daily Quests", Material.WRITABLE_BOOK, "Shows available daily quests."),
    EFFECTS("Effects", Material.POTION, "Shows your active effects."), ESSENCE("Essence", Material.NETHER_STAR, "Shows your Essences."),
    FORGE("Forge", Material.FURNACE, "Shows the status of your Forge slots."), JACOBS_CONTEST("Jacob's Contest", Material.WOODEN_HOE, "Shows farming contest information."),
    PITY("Pity", Material.EMERALD, "Shows progress toward pity rewards."), SLAYER("Slayer", Material.BAT_SPAWN_EGG, "Shows Slayer XP and quests."),
    TIMERS("Timers", Material.CLOCK, "Shows timers for upcoming events."), TRACKERS("Trackers", Material.COMPASS, "Shows progress in active events."),
    CATACOMBS("Catacombs", Material.WITHER_SKELETON_SKULL, "Shows dungeon-related progress."), PARTY("Party", Material.PLAYER_HEAD, "Shows your current party."),
    COMMISSIONS("Commissions", Material.WRITABLE_BOOK, "Shows mining commission progress."), CRYSTALS("Crystals", Material.AMETHYST_SHARD, "Shows obtained Gemstone Crystals."),
    POWDER("Powder", Material.LIME_DYE, "Shows your total powder."), PICKAXE_COOLDOWNS("Pickaxe Ability Cooldowns", Material.GOLDEN_PICKAXE, "Shows pickaxe ability cooldowns."),
    COMPOSTER("Composter", Material.COMPOSTER, "Shows composter information."), CROP_MILESTONE("Crop Milestone", Material.WHEAT, "Shows crop milestones."),
    PESTS("Pests", Material.HOPPER_MINECART, "Shows Garden pest information."), VISITORS("Visitors", Material.PLAYER_HEAD, "Shows Garden visitors."),
    DRAGON("Dragon", Material.DRAGON_EGG, "Shows the summoned dragon and damage leaders."), FACTION_QUESTS("Faction Quests", Material.PAPER, "Shows faction quests."),
    REPUTATION("Reputation", Material.GOLDEN_HELMET, "Shows faction reputation."), TROPHY_FISH("Trophy Fish", Material.TROPICAL_FISH, "Shows Trophy Fishing progress."),
    MINIONS("Minions", Material.COBBLESTONE, "Shows placed Minions."), TRAPPER("Trapper", Material.OAK_TRAPDOOR, "Shows pelts and the tracked animal."),
    FROZEN_CORPSES("Frozen Corpses", Material.SKELETON_SKULL, "Shows Frozen Corpses in the current mineshaft."), BARRY("Barry", Material.PLAYER_HEAD, "Barry is the best mayor SkyBlock has ever had."),
    RIFT("Rift", Material.ENDER_EYE, "Shows Rift information.");

    public final String display;
    public final Material material;
    public final String description;

    TablistWidget(String display, Material material, String description) {
        this.display = display;
        this.material = material;
        this.description = description;
    }

    public static Set<TablistWidget> available(TablistLocation l) {
        EnumSet<TablistWidget> out = EnumSet.of(GENERAL_INFO, PROFILE, PET, FIRE_SALES, ELECTION, EVENTS, SKILLS, STATS, BESTIARY, COLLECTIONS, DAILY_QUESTS, EFFECTS, FORGE, PITY, TIMERS, TRACKERS);
        if (EnumSet.of(TablistLocation.HUB, TablistLocation.DUNGEON_HUB, TablistLocation.DWARVEN_MINES,
                TablistLocation.CRYSTAL_HOLLOWS, TablistLocation.CRIMSON_ISLE).contains(l)) out.add(ESSENCE);
        if (l == TablistLocation.DUNGEON_HUB) out.addAll(EnumSet.of(CATACOMBS, PARTY));
        if (EnumSet.of(TablistLocation.GOLD_MINE, TablistLocation.DEEP_CAVERNS, TablistLocation.DWARVEN_MINES, TablistLocation.CRYSTAL_HOLLOWS, TablistLocation.MINESHAFT).contains(l))
            out.add(PICKAXE_COOLDOWNS);
        if (EnumSet.of(TablistLocation.DWARVEN_MINES, TablistLocation.CRYSTAL_HOLLOWS).contains(l))
            out.addAll(EnumSet.of(COMMISSIONS, POWDER));
        if (l == TablistLocation.CRYSTAL_HOLLOWS) out.add(CRYSTALS);
        if (l == TablistLocation.MINESHAFT) out.add(FROZEN_CORPSES);
        if (EnumSet.of(TablistLocation.PRIVATE_ISLAND, TablistLocation.GARDEN, TablistLocation.FARMING_ISLANDS).contains(l))
            out.add(JACOBS_CONTEST);
        if (l == TablistLocation.PRIVATE_ISLAND) out.add(MINIONS);
        if (l == TablistLocation.GARDEN) out.addAll(EnumSet.of(COMPOSTER, CROP_MILESTONE, PESTS, VISITORS));
        if (l == TablistLocation.FARMING_ISLANDS) out.add(TRAPPER);
        if (l == TablistLocation.THE_END) out.add(DRAGON);
        if (EnumSet.of(TablistLocation.HUB, TablistLocation.SPIDERS_DEN, TablistLocation.THE_END,
                TablistLocation.CRIMSON_ISLE, TablistLocation.THE_PARK).contains(l)) out.add(SLAYER);
        if (l == TablistLocation.CRIMSON_ISLE) out.addAll(EnumSet.of(FACTION_QUESTS, REPUTATION, TROPHY_FISH));
        if (l == TablistLocation.THE_RIFT) {
            out.clear();
            out.addAll(EnumSet.of(GENERAL_INFO, BARRY, RIFT));
        }
        return out;
    }
}
