package io.github.term4.polyp.mechanics.hunger;

import io.github.term4.polyp.MechanicsKeys;
import io.github.term4.polyp.MechanicsProfile;
import io.github.term4.polyp.presets.vanilla18.Hunger;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceTickEvent;
import net.minestom.server.instance.Instance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Natural regen, both vanilla shapes (1.8 {@code FoodMetaData.a}, 26.1 {@code FoodData.tick}) + the cost-rule config surface. */
class HungerRegenTest extends HeadlessServerTest {

    @BeforeAll
    static void installHunger() {
        HungerSystem.install(polyp);
    }

    private static Instance instance(HungerConfig hunger) {
        return flatInstance(MechanicsProfile.builder().set(MechanicsKeys.HUNGER, hunger).build());
    }

    private static void tick(Instance inst, int times) {
        for (int i = 0; i < times; i++) EventDispatcher.call(new InstanceTickEvent(inst, 0, 0));
    }

    private static Player player(Instance inst, String name) {
        return FakePlayer.connect(inst, new Pos(8.5, 64, 8.5), name).player;
    }

    @Test
    void legacyHealsOnePerEightyTicks() {
        Instance inst = instance(Hunger.config());
        Player p = player(inst, "LegacyRegen");
        p.setHealth(10f);

        tick(inst, 79);
        assertEquals(10f, p.getHealth(), 1e-6, "no heal before the 80th tick");
        tick(inst, 1);
        assertEquals(11f, p.getHealth(), 1e-6, "80th tick heals one half-heart");
        assertEquals(3.0f, HungerSystem.exhaustion(p), 1e-6, "1.8 regen costs 3.0 exhaustion");
    }

    @Test
    void modernSaturationFastRegenHealsEveryTenTicks() {
        Instance inst = instance(io.github.term4.polyp.presets.vanilla.Hunger.config());
        Player p = player(inst, "ModernRegen");
        p.setHealth(10f);
        // defaults: food 20, saturation 5 -> fast path heals min(5,6)/6 on the 10th tick at 5.0 exhaustion
        tick(inst, 9);
        assertEquals(10f, p.getHealth(), 1e-6, "no heal before the 10th tick");
        tick(inst, 1);
        assertEquals(10f + 5f / 6f, p.getHealth(), 1e-5, "10th tick heals saturation/6");
        assertEquals(5.0f, HungerSystem.exhaustion(p), 1e-6, "fast regen costs the spent saturation");
    }

    @Test
    void belowThresholdNeverRegens() {
        Instance inst = instance(Hunger.config());
        Player p = player(inst, "NoFood");
        p.setHealth(10f);
        p.setFood(17);
        tick(inst, 200);
        assertEquals(10f, p.getHealth(), 1e-6, "food 17 < threshold 18 - no regen");
    }

    @Test
    void naturalRegenToggleOff() {
        Instance inst = instance(HungerConfig.builder(
                Hunger.config()).naturalRegen(false).build());
        Player p = player(inst, "NoRegen");
        p.setHealth(10f);
        tick(inst, 200);
        assertEquals(10f, p.getHealth(), 1e-6, "toggle off - no regen at full food");
    }

    @Test
    void exhaustionDrainsSaturationThenFood() {
        Instance inst = instance(Hunger.config());
        Player p = player(inst, "Drain");
        HungerSystem hunger = polyp.module(HungerSystem.class);
        p.setFoodSaturation(1f);
        hunger.exhaust(p, Key.key("test:drain"), 4.5f); // a custom source at default scale
        tick(inst, 1);
        assertEquals(0f, p.getFoodSaturation(), 1e-6, "4 exhaustion takes a saturation point");
        assertEquals(20, p.getFood(), "food untouched while saturation lasts");
        hunger.exhaust(p, Key.key("test:drain"), 4.5f); // 0.5 residual + 4.5
        tick(inst, 1);
        assertEquals(19, p.getFood(), "empty saturation - food pays next");
    }

    @Test
    void globalScaleZeroRegensWithoutEverDepleting() { // the BedWars shape
        Instance inst = instance(HungerConfig.builder(
                Hunger.config()).exhaustionScale(0f).build());
        Player p = player(inst, "BedWars");
        p.setHealth(1f);
        tick(inst, 160);
        assertEquals(3f, p.getHealth(), 1e-6, "regen keeps healing");
        assertEquals(0f, HungerSystem.exhaustion(p), 1e-6, "no cost accrues");
        assertEquals(20, p.getFood(), "food never moves");
        assertEquals(5f, p.getFoodSaturation(), 1e-6, "saturation never moves");
    }

    @Test
    void perSourceCostRuleAffectsOnlyThatSource() {
        Instance inst = instance(HungerConfig.builder(Hunger.config())
                .exhaustionCost(HungerSystem.REGEN_COST, ExhaustionCost.free()).build());
        Player p = player(inst, "FreeRegen");
        p.setHealth(10f);
        tick(inst, 80);
        assertEquals(11f, p.getHealth(), 1e-6);
        assertEquals(0f, HungerSystem.exhaustion(p), 1e-6, "regen cost freed");
        polyp.module(HungerSystem.class).exhaust(p, Key.key("game:dash"), 4.5f);
        assertEquals(4.5f, HungerSystem.exhaustion(p), 1e-6, "a custom source still costs its own quantity");
    }

    @Test
    void scaledRuleKeepsTheFastPathDynamic() {
        Instance inst = instance(HungerConfig.builder(io.github.term4.polyp.presets.vanilla.Hunger.config())
                .exhaustionCost(HungerSystem.SATURATION_REGEN_COST, ExhaustionCost.scaled(0.5f)).build());
        Player p = player(inst, "HalfCost");
        p.setHealth(10f);
        tick(inst, 10); // spends min(5,6)=5 saturation -> half cost = 2.5
        assertEquals(10f + 5f / 6f, p.getHealth(), 1e-5, "heal amount unchanged");
        assertEquals(2.5f, HungerSystem.exhaustion(p), 1e-6, "cost scales with the dynamic quantity");
    }

    @Test
    void restoreCapsFoodAndSaturation() {
        Instance inst = instance(Hunger.config());
        Player p = player(inst, "Restore");
        p.setFood(14);
        p.setFoodSaturation(0f);
        polyp.module(HungerSystem.class).restore(p, 4, 9.6f); // golden apple
        assertEquals(18, p.getFood());
        assertEquals(9.6f, p.getFoodSaturation(), 1e-6);
        polyp.module(HungerSystem.class).restore(p, 4, 9.6f);
        assertEquals(20, p.getFood(), "food caps at 20");
        assertEquals(19.2f, p.getFoodSaturation(), 1e-6);
        polyp.module(HungerSystem.class).restore(p, 4, 9.6f);
        assertEquals(20f, p.getFoodSaturation(), 1e-6, "saturation caps at the food level");
    }
}
