package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.text.Text;
import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NewYearCakeComponent extends SkyBlockItemComponent {

    public NewYearCakeComponent() {
        addInheritedComponent(new CustomDisplayNameComponent((item) -> {
            int year = item.getAttributeHandler().getNewYearCakeYear();
            return Text.of("<c>New Year Cake (Year {})", year);
        }));
        addInheritedComponent(new LoreUpdateComponent(
                new LoreConfig(this::lore, null),
                false
        ));
        addInheritedComponent(new TrackedUniqueComponent());
    }

    private List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        int year = item.getAttributeHandler().getNewYearCakeYear();
        return List.of(
                "<7>Given to every player as a",
                "<7>celebration for the " + StringUtility.ntify(year) + " Skyblock",
                "<7>year!"
        );
    }

}