package net.swofty.type.skyblockgeneric.levels;

import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public abstract class SkyBlockLevelUnlock {
    public abstract UnlockType type();
    public abstract ItemStack.Builder getItemDisplay(SkyBlockPlayer player, int level);
    public abstract List<Text> getDisplay(SkyBlockPlayer player, int level);

    public enum UnlockType {
        STATISTIC,
        CUSTOM
    }
}
