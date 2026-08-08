package io.github.term4.polyp.platform.compatibility;

import io.github.term4.polyp.mechanics.blocking.catalog.VanillaBlocking;
import io.github.term4.polyp.testsupport.HeadlessServerTest;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttackRange;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.network.packet.server.play.EntityEquipmentPacket;
import net.minestom.server.network.packet.server.play.SetSlotPacket;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** View rewrites are client-only; {@link CompatState#sanitizeInboundItem} keeps a creative echo from becoming server state. */
class CompatStateTest extends HeadlessServerTest {

    private static ItemStack stampedSword() {
        return ItemStack.of(Material.DIAMOND_SWORD).with(DataComponents.ATTACK_RANGE, new AttackRange(0f, 3f, 0f, 5f, 0.1f, 1f));
    }

    @Test
    void sanitizeStripsEchoedStampForStampedClient() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        assertTrue(s.stampsAttackRange());
        assertNull(s.sanitizeInboundItem(stampedSword()).get(DataComponents.ATTACK_RANGE),
                "a stamped client's echoed attack_range is stripped");
    }

    @Test
    void sanitizeLeavesItemsAloneWhenNotStamping() {
        CompatState s = new CompatState();
        assertFalse(s.stampsAttackRange());
        assertNotNull(s.sanitizeInboundItem(stampedSword()).get(DataComponents.ATTACK_RANGE),
                "a non-stamped client keeps a legit attack_range");
    }

    private static ItemStack slotItem(CompatState s, ItemStack item) {
        return ((SetSlotPacket) s.rewriteItems(new SetSlotPacket(0, 0, (short) 36, item))).itemStack();
    }

    private static ItemStack fancySnowball() {
        return enchant(ItemStack.of(Material.SNOWBALL, 16)
                .withCustomName(Component.text("Feather"))
                .withLore(Component.text("Kit projectile")), Key.key("minecraft:power"), 2);
    }

    @Test
    void reskinsThrowableToNonUsableBaseInClientView() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        assertTrue(s.suppressesThrowSwing());
        ItemStack shown = slotItem(s, ItemStack.of(Material.SNOWBALL, 16));
        assertEquals(Material.PAPER, shown.material(), "the client sees a non-usable base (no throw swing)");
        assertEquals("minecraft:snowball", shown.get(DataComponents.ITEM_MODEL), "but it still renders as a snowball");
        assertEquals(16, shown.amount(), "count preserved");
        assertEquals(ItemStack.of(Material.SNOWBALL).get(DataComponents.ITEM_NAME), shown.get(DataComponents.ITEM_NAME),
                "and reads as a snowball, not \"Paper\"");
        assertEquals(16, shown.get(DataComponents.MAX_STACK_SIZE), "and stacks like a snowball, not paper's 64");
    }

    @Test
    void reskinPreservesRealNameLoreEnchants() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack shown = slotItem(s, fancySnowball());
        assertEquals(Material.PAPER, shown.material());
        assertEquals(Component.text("Feather"), shown.get(DataComponents.CUSTOM_NAME), "the item's real name is kept (not overwritten)");
        assertEquals(1, shown.get(DataComponents.LORE).size(), "lore is kept");
        assertNotNull(shown.get(DataComponents.ENCHANTMENTS), "enchantments are kept");
    }

    @Test
    void restoresEchoedReskinToTrueItem() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack restored = s.sanitizeInboundItem(slotItem(s, ItemStack.of(Material.SNOWBALL, 16)));
        assertEquals(Material.SNOWBALL, restored.material(), "a creative-echoed reskin becomes the true snowball again");
        assertNull(restored.get(DataComponents.ITEM_MODEL), "the reskin marker is cleared (renders as a plain snowball)");
        assertEquals(ItemStack.of(Material.SNOWBALL).get(DataComponents.ITEM_NAME), restored.get(DataComponents.ITEM_NAME), "and reads as a snowball");
        assertEquals(16, restored.amount());
    }

    @Test
    void restoreKeepsRealComponents() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack restored = s.sanitizeInboundItem(slotItem(s, fancySnowball()));
        assertEquals(Material.SNOWBALL, restored.material());
        assertEquals(Component.text("Feather"), restored.get(DataComponents.CUSTOM_NAME), "a real name survives the creative round-trip");
        assertNotNull(restored.get(DataComponents.ENCHANTMENTS), "so do enchantments");
        assertNull(restored.get(DataComponents.ITEM_MODEL));
    }

    @Test
    void leavesThrowablesUnchangedWhenNotSuppressing() {
        CompatState s = new CompatState();
        assertFalse(s.suppressesThrowSwing());
        assertEquals(Material.SNOWBALL, slotItem(s, ItemStack.of(Material.SNOWBALL)).material(),
                "a modern client without the fix keeps the real snowball (and its vanilla throw swing)");
    }

    /** An Animatium client takes the 1.8 set natively: compensations that would double or conflict are excluded;
     *  harmless strips stay on (belt against a spoofed handshake). */
    @Test
    void animatiumClientExclusionsFollowTheHarmLine() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        s.setAnimatiumClient(true);
        assertFalse(s.stampsAttackRange());
        assertFalse(s.suppressesThrowSwing());
        assertFalse(s.fistRayHits());
        assertEquals(Material.SNOWBALL, slotItem(s, ItemStack.of(Material.SNOWBALL)).material());
        // NOT excluded: Animatium only RESTYLES a block pose, so without the stamp there is nothing to restyle
        assertTrue(s.swordBlockingPose());
        assertNotNull(slotItem(s, ItemStack.of(Material.DIAMOND_SWORD)).get(DataComponents.BLOCKS_ATTACKS));
        // NOT excluded: use_cooldown isn't an Animatium feature, and the glider strip matches its native disable
        assertTrue(s.stripsUseCooldowns());
        assertNull(slotItem(s, ItemStack.of(Material.ENDER_PEARL)).get(DataComponents.USE_COOLDOWN));
        assertTrue(s.stripsGlider());
        assertNull(slotItem(s, ItemStack.of(Material.ELYTRA)).get(DataComponents.GLIDER));
    }

    @Test
    void gliderStrippedFromViewAndRestoredOnEcho() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack shown = slotItem(s, ItemStack.of(Material.ELYTRA));
        assertNull(shown.get(DataComponents.GLIDER), "the view carries no glider");
        assertEquals(Material.ELYTRA, shown.material(), "still an elytra (worn/rendered normally)");
        ItemStack restored = s.sanitizeInboundItem(shown);
        assertNotNull(restored.get(DataComponents.GLIDER), "an echoed strip never becomes a truly glide-less server item");
    }

    @Test
    void useCooldownStrippedFromViewAndRestoredOnEcho() {
        assertNotNull(ItemStack.of(Material.ENDER_PEARL).get(DataComponents.USE_COOLDOWN),
                "precondition: the pinned Minestom pearl prototype carries use_cooldown");
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack shown = slotItem(s, ItemStack.of(Material.ENDER_PEARL));
        assertNull(shown.get(DataComponents.USE_COOLDOWN), "no client-self-applied cooldown (1.8 pearls spam-throw)");
        assertNotNull(s.sanitizeInboundItem(shown).get(DataComponents.USE_COOLDOWN), "the echo restores the prototype cooldown");
    }

    /** Modern-only item, but the swing suppression is universal - so it is in the reskin set too. */
    @Test
    void windChargeIsReskinned() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        assertEquals(Material.PAPER, slotItem(s, ItemStack.of(Material.WIND_CHARGE)).material());
    }

    /** The applier re-sends the inventory whenever this key changes, so every view-rewrite knob must move it. */
    @Test
    void itemViewKeyTracksEveryViewRewrite() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        var full = s.itemViewKey();
        s.apply(Compat18.config().toBuilder().swordBlockingPose(false).build());
        assertNotEquals(full, s.itemViewKey(), "same margin, different sword pose -> re-send");
        s.apply(Compat18.config().toBuilder().removeUseCooldowns(false).build());
        assertNotEquals(full, s.itemViewKey(), "same margin, different cooldown strip -> re-send");
        s.apply(Compat18.config());
        assertEquals(full, s.itemViewKey(), "identical policy -> no re-send");
        s.apply(null);
        assertNotEquals(full, s.itemViewKey(), "compat dropped -> re-send (views revert)");
    }

    @Test
    void swordBlockPoseStampedAndStrippedOnEcho() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        ItemStack shown = slotItem(s, ItemStack.of(Material.DIAMOND_SWORD));
        assertNotNull(shown.get(DataComponents.BLOCKS_ATTACKS), "the client sees a blockable sword");
        assertNull(slotItem(s, ItemStack.of(Material.STONE)).get(DataComponents.BLOCKS_ATTACKS), "only swords");
        assertNull(s.sanitizeInboundItem(shown).get(DataComponents.BLOCKS_ATTACKS), "the echo never becomes server state");
    }

    /**
     * A modern client renders the third-person block pose off the held item's {@code blocks_attacks}
     * ({@code Item.getUseAnimation} -> BLOCK), so an unstamped sword means you never see anyone else blocking.
     */
    @Test
    void anotherPlayersSwordIsStampedSoTheBlockPoseRenders() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());

        var equipment = new EntityEquipmentPacket(7, Map.of(
                EquipmentSlot.MAIN_HAND, ItemStack.of(Material.DIAMOND_SWORD),
                EquipmentSlot.OFF_HAND, ItemStack.of(Material.STONE)));
        var shown = (EntityEquipmentPacket) s.rewriteItems(equipment);

        assertEquals(7, shown.entityId());
        assertNotNull(shown.equipments().get(EquipmentSlot.MAIN_HAND).get(DataComponents.BLOCKS_ATTACKS),
                "a viewer must see the component or the block pose never renders");
        assertNull(shown.equipments().get(EquipmentSlot.OFF_HAND).get(DataComponents.BLOCKS_ATTACKS), "only swords");
    }

    /** An opted-out sword stays unstamped for viewers too - it would pose a block the server refuses. */
    @Test
    void anOptedOutSwordIsNotStampedForViewers() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());

        ItemStack optedOut = VanillaBlocking.nonBlocking(ItemStack.of(Material.DIAMOND_SWORD));
        var shown = (EntityEquipmentPacket) s.rewriteItems(new EntityEquipmentPacket(7, Map.of(EquipmentSlot.MAIN_HAND, optedOut)));
        assertNull(shown.equipments().get(EquipmentSlot.MAIN_HAND).get(DataComponents.BLOCKS_ATTACKS));
    }

    /** A legacy client blocks natively and the component is junk through Via - leave its equipment alone. */
    @Test
    void aLegacyViewersEquipmentIsUntouched() {
        CompatState s = new CompatState();
        s.apply(Compat18.config());
        s.setLegacyClient(true);

        var equipment = new EntityEquipmentPacket(7, Map.of(EquipmentSlot.MAIN_HAND, ItemStack.of(Material.DIAMOND_SWORD)));
        assertSame(equipment, s.rewriteItems(equipment), "no stamp for a 1.8 viewer");
    }
}
