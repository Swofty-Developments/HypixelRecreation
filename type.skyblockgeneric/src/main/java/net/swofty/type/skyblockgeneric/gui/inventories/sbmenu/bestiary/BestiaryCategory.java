package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobBarbarian;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobMushroomBull;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobWitherSkeleton;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.deepcaverns.*;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.hub.*;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.island.*;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden.*;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.theend.*;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.thepark.MobHowlingSpirit;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.thepark.MobPackSpirit;

import java.util.List;

public enum BestiaryCategory {
    ;

    @Getter
    public enum PRIVATE_ISLAND implements BestiaryEntry {
        ZOMBIE("<a>Zombie", "Brains.", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(new MobZombie_01(), new MobZombie_02())),
        SKELETON("<a>Skeleton", "Just a normal skelly.", new GUIMaterial(Material.SKELETON_SKULL), List.of(new MobSkeleton())),
        SLIME("<a>Slime", "World's loudest creatures.", new GUIMaterial(Material.SLIME_BALL), List.of(new MobSlime())),
        SPIDER("<a>Spider", "Likes to climb.", new GUIMaterial(Material.SPIDER_EYE), List.of(new MobSpider())),
        CREEPER("<a>Creeper", "Look out for their hiss!", new GUIMaterial(Material.CREEPER_HEAD), List.of(new MobCreeper())),
        ENDERMAN("<a>Enderman", "Don't like making eye contact.", new GUIMaterial(Material.ENDER_PEARL), List.of(new MobEnderman())),
        WITCH("<a>Witch", "Masters of alchemy.", new GUIMaterial(Material.POTION), List.of(new MobWitch())),
        BAT("<a>Bat", "Can be found hanging around.", new GUIMaterial(Material.BAT_SPAWN_EGG), List.of(new MobBat())),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        PRIVATE_ISLAND(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum HUB implements BestiaryEntry {
        GRAVEYARD_ZOMBIE("<a>Graveyard Zombie", "Brains.", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(new MobGraveyardZombie())),
        CRYPT_GHOUL("<a>Crypt Ghoul", "Long buried, newly unburied.", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(new MobCryptGhoul())),
        GOLDEN_GHOUL("<a>Golden Ghoul", "A ghoul that fell into molten gold.", new GUIMaterial(Material.ZOMBIE_HEAD), List.of(new MobGoldenGhoul())),
        OLD_WOLF("<a>Old Wolf", "Wolves older than the island itself.", new GUIMaterial("d359537c15534f61c1cd886bc118774ed22280e7cdab6613870160aad4ca39"), List.of(new MobRuinsOldWolf())),
        WOLF("<a>Wolf", "Roaming the remains of a Castle far from its best days.", new GUIMaterial("f4cb7a6bf6c32c49f2589147e6f0f888e9e35875dd1ea2a8af379ca710589e6b"), List.of(new MobRuinsWolf())),
        ZOMBIE_VILLAGER("<a>Zombie Villager", "The real enemy isn't the dead - it's the living.", new GUIMaterial("69198f410a10f99314aa0fbe9a3db10697bbc1c011f019507d96673c64217f5a"), List.of(new MobGraveyardZombieVillager())),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        HUB(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum SPIDERS_DEN implements BestiaryEntry {
        SPLITTER_SPIDER("<a>Splitter Spider", "If you think you've killed this spider, think again.", new GUIMaterial(Material.SPIDER_EYE), List.of(new MobSplitterSpider())),
        DASHER_SPIDER("<a>Dasher Spider", "It'll dash your hopes of survival.", new GUIMaterial(Material.SPIDER_EYE), List.of(new MobDasherSpider())),
        WEAVER_SPIDER("<a>Weaver Spider", "Weaving webs and weaving woes.", new GUIMaterial(Material.SPIDER_EYE), List.of(new MobWeaverSpider())),
        VORACIOUS_SPIDER("<a>Voracious Spider", "Always hungry.", new GUIMaterial(Material.SPIDER_EYE), List.of(new MobVoraciousSpider())),
        GRAVEL_SKELETON("<a>Gravel Skeleton", "These Skeletons just never stay dead.", new GUIMaterial(Material.SKELETON_SKULL), List.of(new MobGravelSkeleton())),
        SILVERFISH("<a>Silverfish", "Small but vicious.", new GUIMaterial(Material.STRING), List.of(new MobSilverfish())),
        RAIN_SLIME("<a>Rain Slime", "Only comes out in the rain.", new GUIMaterial(Material.SLIME_BALL), List.of(new MobRainSlime()));

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        SPIDERS_DEN(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum THE_END implements BestiaryEntry {
        ENDERMAN("<a>Enderman", "Not your mother's endermen.", new GUIMaterial(Material.ENDER_PEARL), List.of(new MobEnderman_42(), new MobEnderman_45(), new MobEnderman_50())),
        ENDERMITE("<a>Endermite", "Hidden in the stone of The End.", new GUIMaterial(Material.ENDERMITE_SPAWN_EGG), List.of(new MobEndermite_37(), new MobEndermite_40(), new MobNestEndermite()));

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        THE_END(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum THE_PARK implements BestiaryEntry {
        PACK_SPIRIT("<a>Pack Spirit", "A spirit of the pack.", new GUIMaterial(Material.BONE), List.of(new MobPackSpirit())),
        HOWLING_SPIRIT("<a>Howling Spirit", "A howling spirit.", new GUIMaterial(Material.BONE), List.of(new MobHowlingSpirit()));

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        THE_PARK(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum CRIMSON_ISLE implements BestiaryEntry {
        WITHER_SKELETON("<a>Wither Skeleton", "A skeleton touched by the Wither.", new GUIMaterial(Material.WITHER_SKELETON_SKULL), List.of(new MobWitherSkeleton())),
        MUSHROOM_BULL("<a>Mushroom Bull", "A hostile mushroom cow.", new GUIMaterial(Material.RED_MUSHROOM), List.of(new MobMushroomBull())),
        BARBARIAN("<a>Barbarian", "An aggressive inhabitant of the Crimson Isle.", new GUIMaterial(Material.PIGLIN_SPAWN_EGG), List.of(new MobBarbarian()));

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        CRIMSON_ISLE(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }

    @Getter
    public enum DEEP_CAVERNS implements BestiaryEntry {
        EMERALD_SLIME("<a>Emerald Slime", "It is said that these slimes absorb emeralds to grow larger.",
                new GUIMaterial("895aeec6b842ada8669f846d65bc49762597824ab944f22f45bf3bbb941abe6c"), List.of(new MobEmeraldSlime_05(), new MobEmeraldSlime_10())),
        LAPIS_ZOMBIE("<a>Lapis Zombie", "These zombies adapted to their environment, using the lapis around them as a defense mechanism.",
                new GUIMaterial("e9f7979b25001087969d58c06e14d00b8dab57dab060b4c8b483c1b7f869940"), List.of(new MobLapisZombie())),
        MINER_SKELETON("<a>Miner Skeleton", "These skeletons have crafted gear from the diamonds around them - resulting in a look both fashionable and protective.",
                new GUIMaterial("8de8bbd7f6d77a1614865ef6a1d31f53f797550d14ee21d107a8415c14b48ca6"), List.of(new MobMinerSkeleton_15(), new MobMinerSkeleton_20())),
        MINER_ZOMBIE("<a>Miner Zombie", "Like their skeleton counterparts, these zombies have bedazzled themselves throughout the years.",
                new GUIMaterial("1b8a707e8a58d2ffe297474d18daee86951b21994566358dc0b5d7dcc9e2ed9b"), List.of(new MobMinerZombie_15(), new MobMinerZombie_20())),
        REDSTONE_PIGMAN("<a>Redstone Pigman", "These pigmen will defend their redstone to the death.",
                new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), List.of(new MobRedstonePigman())),
        SNEAKY_CREEPER("<a>Sneaky Creeper", "They be creepin'.",
                new GUIMaterial("74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"), List.of(new MobSneakyCreeper())),
        ;

        private final String name, description;
        private final GUIMaterial guiMaterial;
        private final List<BestiaryMob> mobs;

        DEEP_CAVERNS(String name, String description, GUIMaterial guiMaterial, List<BestiaryMob> mobs) {
            this.name = name;
            this.description = description;
            this.guiMaterial = guiMaterial;
            this.mobs = mobs;
        }
    }
}
