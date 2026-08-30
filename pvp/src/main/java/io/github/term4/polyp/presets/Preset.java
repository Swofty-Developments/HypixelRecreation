package io.github.term4.polyp.presets;

import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.presets.hypixel.Hypixel;
import io.github.term4.polyp.presets.mmc18.Mmc18;
import io.github.term4.polyp.presets.scrims18.Scrims18;
import io.github.term4.polyp.presets.vanilla.Vanilla;
import io.github.term4.polyp.presets.vanilla18.Vanilla18;

import java.util.function.Supplier;

/** The shipped server presets; the primed-TNT config rides each profile ({@code MechanicsKeys.TNT}). */
public enum Preset {
    VANILLA18(Vanilla18::profile),
    /** Modern (26.1) mechanics. */
    VANILLA(Vanilla::profile),
    HYPIXEL(Hypixel::profile),
    /** {@link #HYPIXEL} with the BedWars-only quirks (the game-wide pearl landing). */
    HYPIXEL_BEDWARS(Hypixel::bedwars),
    MMC18(Mmc18::profile),
    SCRIMS18(Scrims18::profile);

    private final Supplier<MechanicsProfile> profile;

    Preset(Supplier<MechanicsProfile> profile) {
        this.profile = profile;
    }

    /** The mechanics profile (fresh build); the server layers compat + fixes on top. */
    public MechanicsProfile profile() { return profile.get(); }
}
