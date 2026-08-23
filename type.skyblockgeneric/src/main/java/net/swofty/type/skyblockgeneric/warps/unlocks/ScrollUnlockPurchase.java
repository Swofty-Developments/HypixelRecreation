package net.swofty.type.skyblockgeneric.warps.unlocks;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockPurchase extends ScrollUnlockReason {
    private final String npc;

    public ScrollUnlockPurchase(String npc) {
        this.npc = npc;
    }

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll bought from NPC!");
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>Purchase the scroll from the {}.", StringUtility.toNormalCase(npc));
    }
}
