package net.swofty.type.skyblockgeneric.levels.causes;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.levels.abstr.SkyBlockLevelCauseAbstr;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

@Getter
public final class MetaphysicalSerumLevelCause extends SkyBlockLevelCauseAbstr {
    private final int dose;

    public MetaphysicalSerumLevelCause(int dose) {
        this.dose = dose;
    }

    @Override
    public double xpReward() {
        return 5;
    }

    @Override
    public boolean shouldDisplayMessage(SkyBlockPlayer player) {
        return false;
    }
}
