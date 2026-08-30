package io.github.term4.polyp.mechanics.consumable;

import io.github.term4.polyp.api.event.consume.ConsumeAppliedEvent;
import io.github.term4.polyp.api.event.consume.ConsumeEvent;
import io.github.term4.polyp.api.event.consume.PreConsumeEvent;
import io.github.term4.polyp.presets.vanilla18.Consumables;
import io.github.term4.polyp.mechanics.hunger.HungerSystem;
import io.github.term4.polyp.testsupport.FakePlayer;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionEffect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** The ComponentFood floor (plain foods eat off the item registry), the Pre/Consume/Applied trio, version splits. */
class ConsumableApiTest extends HeadlessServerTest {

    private static Player p;
    private static EventNode<Event> hooks;

    @BeforeAll
    static void setup() {
        HungerSystem.install(polyp);
        ConsumableSystem.install(polyp, Consumables.config());
        p = FakePlayer.connect(instance, new Pos(52.5, 64, 50.5), "ApiEater").player;
    }

    @AfterEach
    void cleanup() {
        if (hooks != null) MinecraftServer.getGlobalEventHandler().removeChild(hooks);
        hooks = null;
        p.clearEffects();
        p.setFood(20);
        p.setFoodSaturation(5f);
    }

    private static EventNode<Event> hooks() {
        hooks = EventNode.all("test:consume-hooks");
        MinecraftServer.getGlobalEventHandler().addChild(hooks);
        return hooks;
    }

    private static void swapConsumables(ConsumableConfig cfg) {
        polyp.unregister(ConsumableSystem.class);
        ConsumableSystem.install(polyp, cfg);
    }

    private static void finish(Material material) {
        EventDispatcher.call(new PlayerFinishItemUseEvent(p, PlayerHand.MAIN, ItemStack.of(material), 32L));
    }

    @Test
    void componentFloorEatsBreadOffTheRegistry() {
        p.setFood(10);
        p.setFoodSaturation(0f);
        finish(Material.BREAD); // unregistered: the food component supplies 5 + 5x0.6x2
        assertEquals(15, p.getFood());
        assertEquals(6f, p.getFoodSaturation(), 1e-6);
    }

    @Test
    void componentFloorCanBeDisabled() {
        var cfg = ConsumableConfig.builder(Consumables.config())
                .componentFoods(false).build();
        swapConsumables(cfg);
        try {
            p.setFood(10);
            finish(Material.BREAD);
            assertEquals(10, p.getFood(), "floor off: bread is not a consumable");
        } finally {
            swapConsumables(Consumables.config());
        }
    }

    @Test
    void preConsumeCancelBlocksTheStart() {
        hooks().addListener(PreConsumeEvent.class, e -> e.cancel());
        var use = new PlayerUseItemEvent(p, PlayerHand.MAIN, ItemStack.of(Material.BREAD), 32);
        EventDispatcher.call(use);
        assertEquals(0, use.getItemUseTime(), "cancelled pre-consume never starts the use");
    }

    @Test
    void consumeCancelSkipsEverything() {
        hooks().addListener(ConsumeEvent.class, e -> e.cancel());
        p.setFood(10);
        finish(Material.BREAD);
        assertEquals(10, p.getFood(), "cancelled consume restores nothing");
    }

    @Test
    void consumeBehaviorOverrideReplacesTheResolvedOne() {
        hooks().addListener(ConsumeEvent.class, e -> e.behavior(new ConsumableBehavior() {
            @Override public void onFinish(ConsumableConfigResolver.ConsumableContext ctx) { ctx.user().setFood(3); }
        }));
        p.setFood(10);
        finish(Material.BREAD);
        assertEquals(3, p.getFood(), "the override ran instead of the component restore");
    }

    @Test
    void appliedFiresAfterTheConsume() {
        AtomicInteger applied = new AtomicInteger();
        hooks().addListener(ConsumeAppliedEvent.class, e -> {
            if (e.item().material() == Material.COOKED_BEEF) applied.incrementAndGet();
        });
        finish(Material.COOKED_BEEF);
        assertEquals(1, applied.get());
    }

    @Test
    void pufferfishPoisonIsVersionSplit() {
        finish(Material.PUFFERFISH); // install config = vanilla18: Poison IV
        assertNotNull(p.getEffect(PotionEffect.POISON));
        assertEquals(3, p.getEffect(PotionEffect.POISON).potion().amplifier(), "1.8: Poison IV");
        p.clearEffects();

        swapConsumables(io.github.term4.polyp.presets.vanilla.Consumables.config());
        try {
            finish(Material.PUFFERFISH);
            assertEquals(1, p.getEffect(PotionEffect.POISON).potion().amplifier(), "modern: Poison II");
        } finally {
            swapConsumables(Consumables.config());
        }
    }

    @Test
    void spiderEyeAlwaysPoisons() {
        assertNull(p.getEffect(PotionEffect.POISON));
        finish(Material.SPIDER_EYE);
        assertNotNull(p.getEffect(PotionEffect.POISON), "chance 1 effect always applies");
        assertEquals(0, p.getEffect(PotionEffect.POISON).potion().amplifier());
    }
}
