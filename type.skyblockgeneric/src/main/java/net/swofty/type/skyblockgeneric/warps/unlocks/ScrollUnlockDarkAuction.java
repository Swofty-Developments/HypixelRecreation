package net.swofty.type.skyblockgeneric.warps.unlocks;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.warps.ScrollUnlockReason;

public class ScrollUnlockDarkAuction extends ScrollUnlockReason {
    public ScrollUnlockDarkAuction() {}

    @Override
    public Text getTitleReason() {
        return Text.of("<c>Scroll bought in Dark Auction!");
    }

    @Override
    public Text getSubReason() {
        return Text.of("<7>Buy this scroll in a Dark Auction event.");
    }
}
