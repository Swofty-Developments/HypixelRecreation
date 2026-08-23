package net.swofty.type.skyblockgeneric.levels;

import lombok.Getter;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.levels.causes.LevelCause;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum SkyBlockEmblems {
    SKILLS(new GUIMaterial(Material.DIAMOND_SWORD), List.of("<7>These symbols are related to", "<7>levelling up your Skills!"), List.of(
            new SkyBlockEmblem("Mining Pickaxe", Material.STONE_PICKAXE, "<7>⸕",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.MINING, 50)),
            new SkyBlockEmblem("Mining Master", Material.GOLDEN_PICKAXE, "<6>⸕",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.MINING, 60)),
            new SkyBlockEmblem("Farming Flower", Material.STONE_HOE, "<7>✿",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FARMING, 50)),
            new SkyBlockEmblem("Farming Master", Material.GOLDEN_HOE, "<6>✿",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FARMING, 60)),
            new SkyBlockEmblem("Combat Explosion", Material.STONE_SWORD, "<7>❁",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.COMBAT, 50)),
            new SkyBlockEmblem("Combat Master", Material.GOLDEN_SWORD, "<6>❁",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.COMBAT, 50)),
            new SkyBlockEmblem("Foraging Leaf", Material.STONE_AXE, "<7>⸙",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FORAGING, 50)),
            new SkyBlockEmblem("Fishing Fish", Material.COD, "<7>α",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.FISHING, 50)),
            new SkyBlockEmblem("Enchanting Pencil", Material.ENCHANTING_TABLE, "<7>✎",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.ENCHANTING, 50)),
            new SkyBlockEmblem("Enchanting Master", Material.ENCHANTING_TABLE, "<6>✎",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.ENCHANTING, 60)),
            //new SkyBlockEmblem("Alchemy Brew", Material.BREWING_STAND, "<7>☕",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.ALCHEMY, 50)),
            new SkyBlockEmblem("Carpentry House", Material.CRAFTING_TABLE, "<7>☖",
                    SkyBlockLevelCause.getSkillCause(SkillCategories.CARPENTRY, 50))
            //new SkyBlockEmblem("Taming Clover", Material.WOLF_SPAWN_EGG, "<7>♣",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.TAMING, 50)),
            //new SkyBlockEmblem("Taming Master", Material.WOLF_SPAWN_EGG, "<6>♣",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.TAMING, 60)),
            //new SkyBlockEmblem("Social Statement", Material.EMERALD, "<7>℻",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.SOCIAL, 15)),
            //new SkyBlockEmblem("Social Master", Material.EMERALD, "<6>℻",
            //        SkyBlockLevelCause.getSkillCause(SkillCategories.SOCIAL, 25)) //Skills not there rn, would throw an error
    )),
    LEVELING(new GUIMaterial("3255327dd8e90afad681a19231665bea2bd06065a09d77ac1408837f9e0b242"), List.of("<7>These symbols are unlocked by", "<7>leveling up your SkyBlock Level."), List.of(
            new SkyBlockEmblem("Diamond", Material.NAME_TAG, "<7>◆",
                    SkyBlockLevelCause.getLevelCause(10)),
            new SkyBlockEmblem("Spade", Material.NAME_TAG, "<7>♠",
                    SkyBlockLevelCause.getLevelCause(20)),
            new SkyBlockEmblem("Heart", Material.NAME_TAG, "<7>❤",
                    SkyBlockLevelCause.getLevelCause(30)),
            new SkyBlockEmblem("Pristine", Material.NAME_TAG, "<7>✧",
                    SkyBlockLevelCause.getLevelCause(40)),
            new SkyBlockEmblem("Arc Reactor", Material.NAME_TAG, "<7>⎊",
                    SkyBlockLevelCause.getLevelCause(50)),
            new SkyBlockEmblem("Marker", Material.NAME_TAG, "<7>፠",
                    SkyBlockLevelCause.getLevelCause(60)),
            new SkyBlockEmblem("Badge", Material.NAME_TAG, "<7>☬",
                    SkyBlockLevelCause.getLevelCause(70)),
            new SkyBlockEmblem("Star", Material.NAME_TAG, "<7>⚝",
                    SkyBlockLevelCause.getLevelCause(80)),
            new SkyBlockEmblem("Boxes", Material.NAME_TAG, "<7>⧉",
                    SkyBlockLevelCause.getLevelCause(90)),
            new SkyBlockEmblem("Jerry", Material.NAME_TAG, "<7>ꈔ",
                    SkyBlockLevelCause.getLevelCause(100)),
            new SkyBlockEmblem("Globe", Material.NAME_TAG, "<7>㋖",
                    SkyBlockLevelCause.getLevelCause(120)),
            new SkyBlockEmblem("Soulflow", Material.NAME_TAG, "<7>⸎",
                    SkyBlockLevelCause.getLevelCause(140)),
            new SkyBlockEmblem("Warning", Material.NAME_TAG, "<7>⚠",
                    SkyBlockLevelCause.getLevelCause(160)),
            new SkyBlockEmblem("Mustache", Material.NAME_TAG, "<7>ꕁ",
                    SkyBlockLevelCause.getLevelCause(180)),
            new SkyBlockEmblem("Helmet", Material.NAME_TAG, "<7>〠",
                    SkyBlockLevelCause.getLevelCause(200)),
            new SkyBlockEmblem("Sideways Smiley", Material.NAME_TAG, "<7>ツ",
                    SkyBlockLevelCause.getLevelCause(250)),
            new SkyBlockEmblem("Spaceship", Material.NAME_TAG, "<7>⥈",
                    SkyBlockLevelCause.getLevelCause(300)),
            new SkyBlockEmblem("Toxic", Material.NAME_TAG, "<7>☢",
                    SkyBlockLevelCause.getLevelCause(350)),
            new SkyBlockEmblem("Biohazard", Material.NAME_TAG, "<7>☣",
                    SkyBlockLevelCause.getLevelCause(400)),
            new SkyBlockEmblem("Florette", Material.NAME_TAG, "<7>✾",
                    SkyBlockLevelCause.getLevelCause(450)),
            new SkyBlockEmblem("Golden Fleur De Lis", Material.NAME_TAG, "<6>⚜",
                    SkyBlockLevelCause.getLevelCause(500))
    )),
    ;

    private final GUIMaterial guiMaterial;
    @Getter(lombok.AccessLevel.NONE)
    private final List<String> description;
    private final List<SkyBlockEmblem> emblems;

    public List<Text> getDescription() {
        return description.stream().map(Text::of).toList();
    }

    SkyBlockEmblems(GUIMaterial guiMaterial, List<String> description, List<SkyBlockEmblem> emblems) {
        this.guiMaterial = guiMaterial;
        this.description = description;
        this.emblems = emblems;
    }

    public int amountUnlocked(SkyBlockPlayer player) {
        int amount = 0;
        for (SkyBlockEmblem emblem : emblems) {
            if (player.hasUnlockedXPCause(emblem.cause())) {
                amount++;
            }
        }
        return amount;
    }

    @Override
    public String toString() {
        return StringUtility.toNormalCase(name());
    }

    public static List<SkyBlockEmblem> getEmblemsWithLevelCause() {
        List<SkyBlockEmblem> emblems = new ArrayList<>();
        Arrays.stream(values()).forEach(emblem -> {
            emblem.getEmblems().stream().filter(emblem1 -> emblem1.cause() instanceof LevelCause).forEach(emblems::add);
        });

        return emblems;
    }

    public static SkyBlockEmblems getCategoryFromEmblem(SkyBlockEmblem emblem) {
        for (SkyBlockEmblems emblems : values()) {
            for (SkyBlockEmblem emblem1 : emblems.emblems) {
                if (emblem1.equals(emblem)) {
                    return emblems;
                }
            }
        }
        return null;
    }

    public record SkyBlockEmblem(String displayName, Material displayMaterial, String emblemMarkup, SkyBlockLevelCauseAbstr cause) {
        public Text emblem() {
            return Text.of(emblemMarkup);
        }
    }
}
