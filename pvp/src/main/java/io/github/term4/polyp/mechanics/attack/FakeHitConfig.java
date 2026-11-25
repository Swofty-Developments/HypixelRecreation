package io.github.term4.polyp.mechanics.attack;

/**
 * Tuning for the {@link FakeHits} swing fill. Presets set one on {@code AttackConfig.fakeHits}; the compat layer
 * derives its own for gated bare-fist clients ({@code CompatConfig.fistRayHits}). Both modes target only the LAST
 * player the attacker hit.
 *
 * @param emptyHandOnly fill only bare-fist swings (the compat gap - a held item covers itself via {@code attack_range})
 * @param windowed      arm only inside the swing window around the last-hit victim's i-frame expiry (the MineMen combo
 *                      fill); off, EVERY eligible swing at the last victim fills (the compat bare-fist pick emulation)
 * @param reach         ray reach in blocks (1.8 melee = 3); the target box is already grown by the attacker's hit margin
 * @param windowBefore  swing arms within the last {@code windowBefore} i-frame ticks of the last-hit victim's window ...
 * @param windowAfter   ... plus the first {@code windowAfter} hittable ticks past it (bounded, so a swing at a long-idle victim never arms)
 * @param lookWindow    ticks of move/look packets the ray is checked on after an armed swing
 * @param fullBoxWhileInvul the whole padded box fills only while the victim sits inside the i-frame window its last hit
 *                      opened; past it, only rays entering ABOVE the real box top (the head band of the expansion).
 *                      {@code false} = anywhere, always.
 */
public record FakeHitConfig(boolean emptyHandOnly, boolean windowed, double reach, int windowBefore, int windowAfter, int lookWindow, boolean fullBoxWhileInvul) {

    /** Any-item, windowed fill at {@code reach} with the 1-tick windows (the MineMen-style preset setup). */
    public static FakeHitConfig ofReach(double reach) {
        return new FakeHitConfig(false, true, reach, 1, 1, 1, false);
    }
}
