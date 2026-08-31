package net.swofty.type.replayviewer.view;

import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.TimedPotion;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.TypeReplayViewerLoader;
import net.swofty.type.replayviewer.entity.ReplayPlayerEntity;
import net.swofty.type.replayviewer.playback.ReplaySession;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUIViewPlayer implements StatefulView<GUIViewPlayer.State> {

    public record State(ReplayPlayerEntity entity) {
    }

    @Override
    public State initialState() {
        return new State(null);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> {
            var sessionOpt = TypeReplayViewerLoader.getSession(ctx.player());
            if (sessionOpt.isEmpty()) {
                return Text.key("replays.player_view");
            }

            return state.entity != null
                    ? Text.key("replays.player_view_name", getDisplayName(state.entity))
                    : Text.key("replays.player_view");
        }, InventoryType.CHEST_2_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        var sessionOpt = TypeReplayViewerLoader.getSession(ctx.player());
        if (sessionOpt.isEmpty()) {
            layout.slot(4, ItemStacks.item(
                Material.BARRIER,
                1,
                    Text.key("replays.no_replay_session_title"),
                    List.of(
                            Text.key("replays.no_replay_session_description"),
                            Text.key("replays.no_replay_session_description_line")
                    )
            ));
            Components.back(layout, 13, ctx);
            return;
        }

        ReplaySession replaySession = sessionOpt.get();
        ReplayPlayerEntity replayPlayer = state.entity;

        if (replayPlayer == null) {
            layout.slot(4, ItemStacks.item(
                Material.BARRIER,
                1,
                    Text.key("replays.player_not_found_title"),
                    List.of(Text.key("replays.player_not_found_description"))
            ));
            Components.back(layout, 49, ctx);
            return;
        }

        Text playerName = Text.key("replays.player_view_name",
                getDisplayName(replayPlayer));
        int health = Math.max(0, Math.round(replayPlayer.getHealth()));
        ItemStack.Builder head = replayPlayer.getSkin() != null
            ? ItemStacks.head(
            replayPlayer.getSkin(),
                playerName,
                List.of(
                        Text.key("replays.health", health),
                        Text.empty(),
                        Text.key("replays.right_click_first_person")
                )
        )
            : ItemStacks.item(
            Material.PLAYER_HEAD,
            1,
                playerName,
                List.of(
                        Text.key("replays.health", health),
                        Text.empty(),
                        Text.key("replays.right_click_first_person")
                )
        );

        layout.slot(0, head, (click, c) -> {
            if (click.click() instanceof Click.Right) {
                replaySession.followEntity(c.player(), state.entity.getInternalId());
                c.player().closeInventory();
                return;
            }

            c.player().teleport(replayPlayer.getPosition());
        });

        layout.slot(1, createEffectsItem(replayPlayer));
        layout.autoUpdating(3, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.MAIN_HAND), Text.key("replays.empty_main_hand")), Duration.ofSeconds(1));
        layout.autoUpdating(5, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.HELMET), Text.key("replays.empty_helmet")), Duration.ofSeconds(1));
        layout.autoUpdating(6, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.CHESTPLATE), Text.key("replays.empty_chestplate")), Duration.ofSeconds(1));
        layout.autoUpdating(7, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.LEGGINGS), Text.key("replays.empty_leggings")), Duration.ofSeconds(1));
        layout.autoUpdating(8, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.BOOTS), Text.key("replays.empty_boots")), Duration.ofSeconds(1));

        layout.slot(9, ItemStacks.item(
            Material.ANVIL,
            1,
                Text.key("replays.report_player"),
                List.of(
                        Text.key("replays.report_player_description"),
                        Text.empty(),
                        Text.key("replays.click_to_report")
                )
        ), (_, c) -> c.player().notImplemented());
    }

    private static Text getDisplayName(ReplayPlayerEntity replayPlayer) {
        try {
            return HypixelPlayer.getDisplayName(replayPlayer.getActualUuid());
        } catch (Exception ignored) {
            return Text.literal(replayPlayer.getPlayerName());
        }
    }

    private static ItemStack.Builder createEffectsItem(ReplayPlayerEntity replayPlayer) {
        List<TimedPotion> effects = new ArrayList<>(replayPlayer.getActiveEffects());
        if (effects.isEmpty()) {
            return ItemStacks.item(
                Material.POTION,
                1,
                    Text.key("replays.active_status_effects"),
                    List.of(Text.key("replays.no_status_effects"))
            );
        }

        List<Text> lore = new ArrayList<>();
        for (TimedPotion timedPotion : effects) {
            String effectName = formatEffectName(timedPotion.potion().effect().toString());
            int amplifier = timedPotion.potion().amplifier() + 1;
            lore.add(Text.key("replays.status_effect",
                    effectName,
                    StringUtility.getAsRomanNumeral(amplifier)));
        }

        return ItemStacks.item(
            Material.POTION,
            1,
            Text.key("replays.active_status_effects"),
            lore
        );
    }

    private static ItemStack.Builder createEquipmentItem(ItemStack itemStack, Text emptyText) {
        if (itemStack == null || itemStack.isAir()) {
            return ItemStacks.named(Material.RED_STAINED_GLASS_PANE, emptyText);
        }
        return itemStack.builder();
    }

    private static String formatEffectName(String raw) {
        String cleaned = raw.toLowerCase(Locale.ROOT)
            .replace("minecraft:", "")
            .replace('_', ' ');

        String[] words = cleaned.split(" ");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                out.append(word.substring(1));
            }
        }
        return out.toString();
    }
}
