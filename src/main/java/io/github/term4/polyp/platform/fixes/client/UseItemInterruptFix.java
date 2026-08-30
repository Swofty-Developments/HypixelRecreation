package io.github.term4.polyp.platform.fixes.client;

import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

/**
 * Vanilla (1.8 and 26 alike) stops an item use the moment the used stack changes under it - a swapped
 * hotbar slot, a resync rewriting the hand. Minestom keeps the use armed, and the client never sends the
 * release it no longer owes - so a bow draw survives invisibly and folds its ticks into the NEXT release
 * (the too-powerful arrow after a dud). Stop it silently, exactly like vanilla: no release, no shot.
 */
public final class UseItemInterruptFix {

    private record UseStart(byte heldSlot, Material material) {}

    private static final Tag<UseStart> USE_START = Tag.Transient("polyp:use-start");

    private UseItemInterruptFix() {}

    public static void install(EventNode<? super Event> node) {
        node.addListener(PlayerBeginItemUseEvent.class, e -> e.getPlayer().setTag(USE_START,
                new UseStart(e.getPlayer().getHeldSlot(), e.getItemStack().material())));
        node.addListener(PlayerTickEvent.class, e -> {
            Player p = e.getPlayer();
            PlayerHand hand = p.getItemUseHand();
            if (hand == null) return;
            UseStart start = p.getTag(USE_START);
            if (start == null) return;
            boolean slotMoved = hand == PlayerHand.MAIN && p.getHeldSlot() != start.heldSlot();
            if (!slotMoved && p.getItemInHand(hand).material() == start.material()) return;
            p.refreshActiveHand(false, hand == PlayerHand.OFF, false);
            p.clearItemUse();
            p.removeTag(USE_START);
        });
    }
}
