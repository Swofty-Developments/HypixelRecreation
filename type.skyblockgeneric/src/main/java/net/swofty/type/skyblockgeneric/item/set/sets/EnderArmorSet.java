package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.MuseumableSet;

import java.util.ArrayList;
import java.util.List;

public class EnderArmorSet implements ArmorSet, MuseumableSet {
    @Override
    public String getName() {
        return "Ender Armor";
    }

    @Override
    public ArrayList<String> getDescription() {
        return new ArrayList<>(List.of(
                "§7All stats of this armor are doubled",
                "§7while on §dThe End§7."
        ));
    }
}
