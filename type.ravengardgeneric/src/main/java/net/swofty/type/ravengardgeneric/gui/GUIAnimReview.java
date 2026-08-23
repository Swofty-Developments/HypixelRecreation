package net.swofty.type.ravengardgeneric.gui;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.entity.animation.AnimReviewService;
import net.swofty.type.ravengardgeneric.entity.animation.RavengardReviewClip;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GUIAnimReview extends RavengardView {
    private final String mob;

    public GUIAnimReview(String mob) {
        this.mob = mob;
    }

    @Override
    protected String title() {
        return mob == null ? "Animation Review" : "Captures: " + mob;
    }

    @Override
    protected boolean usesChrome() {
        return false;
    }

    private static Map<String, List<String>> collated() {
        Map<String, List<String>> byMob = new LinkedHashMap<>();
        for (String name : RavengardReviewClip.available()) {
            String mobName = name.replaceAll("_\\d+$", "");
            byMob.computeIfAbsent(mobName, key -> new ArrayList<>()).add(name);
        }
        return byMob;
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        if (mob == null) {
            int slot = 0;
            for (var entry : collated().entrySet()) {
                if (slot >= 54) break;
                String mobName = entry.getKey();
                int captures = entry.getValue().size();
                layout.slot(slot++, item(Material.SKELETON_SKULL, Text.of("<e>{}", mobName),
                                Text.of("<7>{}{}", captures, captures == 1 ? " capture" : " captures"),
                                Text.of("<e>Click to browse!")),
                        (click, viewContext) -> {
                            if (viewContext.player() instanceof RavengardPlayer player) {
                                ViewNavigator.get(player).push(new GUIAnimReview(mobName));
                            }
                        });
            }
            return;
        }

        layout.slot(53, item(Material.BARRIER, Text.of("<c>All mobs"), Text.of("<e>Click to go back!")),
                (click, viewContext) -> {
                    if (viewContext.player() instanceof RavengardPlayer player) {
                        ViewNavigator.get(player).push(new GUIAnimReview(null));
                    }
                });
        List<String> clips = collated().getOrDefault(mob, List.of());
        int slot = 0;
        for (String clipName : clips) {
            if (slot >= 45) break;
            layout.slot(slot++, item(Material.PAPER, Text.of("<f>{}", clipName),
                            Text.of("<7>Raw capture replay."),
                            Text.of("<e>Click to review on the stage!")),
                    (click, viewContext) -> {
                        if (viewContext.player() instanceof RavengardPlayer player) {
                            player.closeInventory();
                            AnimReviewService.start(player, clipName);
                        }
                    });
        }
    }

    private static ItemStack.Builder item(Material material, Text name, Text... lore) {
        return ItemStacks.raw(material, name, List.of(lore));
    }
}
