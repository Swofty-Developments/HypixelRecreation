package net.swofty.type.skyblockgeneric.item.crafting.grid;

import net.swofty.type.generic.gui.v2.click.SlotStack;

public interface CraftingResolver {

    SlotStack result(SlotStack[] grid);

    SlotStack[] consume(SlotStack[] grid);
}
