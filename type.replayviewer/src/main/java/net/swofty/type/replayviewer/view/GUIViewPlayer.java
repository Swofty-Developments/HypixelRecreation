package net.swofty.type.replayviewer.view;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.TimedPotion;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
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
        return ViewConfiguration.withTitle((state, ctx) -> {
            var sessionOpt = TypeReplayViewerLoader.getSession(ctx.player());
            if (sessionOpt.isEmpty()) {
                return I18n.t("replays.player_view");
            }

            return state.entity != null
                    ? I18n.t("replays.player_view_name", Argument.component("player", getDisplayName(state.entity)))
                    : I18n.t("replays.player_view");
        }, InventoryType.CHEST_2_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        var sessionOpt = TypeReplayViewerLoader.getSession(ctx.player());
        if (sessionOpt.isEmpty()) {
            layout.slot(4, ItemStackCreator.getStack(
                    I18n.t("replays.no_replay_session_title"),
                Material.BARRIER,
                1,
                    List.of(
                            I18n.t("replays.no_replay_session_description"),
                            I18n.t("replays.no_replay_session_description_line")
                    )
            ));
            Components.back(layout, 13, ctx);
            return;
        }

        ReplaySession replaySession = sessionOpt.get();
        ReplayPlayerEntity replayPlayer = state.entity;

        if (replayPlayer == null) {
            layout.slot(4, ItemStackCreator.getStack(
                    I18n.t("replays.player_not_found_title"),
                Material.BARRIER,
                1,
                    I18n.t("replays.player_not_found_description")
            ));
            Components.back(layout, 49, ctx);
            return;
        }

        Component playerName = I18n.t("replays.player_view_name",
                Argument.component("player", getDisplayName(replayPlayer)));
        int health = Math.max(0, Math.round(replayPlayer.getHealth()));
        ItemStack.Builder head = replayPlayer.getSkin() != null
            ? ItemStackCreator.getStackHead(
                playerName,
            replayPlayer.getSkin(),
            1,
                List.of(
                        I18n.t("replays.health", Argument.numeric("health", health)),
                        Component.empty(),
                        I18n.t("replays.right_click_first_person")
                )
        )
            : ItemStackCreator.getStack(
                playerName,
            Material.PLAYER_HEAD,
            1,
                List.of(
                        I18n.t("replays.health", Argument.numeric("health", health)),
                        Component.empty(),
                        I18n.t("replays.right_click_first_person")
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
        layout.autoUpdating(3, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.MAIN_HAND), I18n.t("replays.empty_main_hand")), Duration.ofSeconds(1));
        layout.autoUpdating(5, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.HELMET), I18n.t("replays.empty_helmet")), Duration.ofSeconds(1));
        layout.autoUpdating(6, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.CHESTPLATE), I18n.t("replays.empty_chestplate")), Duration.ofSeconds(1));
        layout.autoUpdating(7, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.LEGGINGS), I18n.t("replays.empty_leggings")), Duration.ofSeconds(1));
        layout.autoUpdating(8, (_, _) -> createEquipmentItem(replayPlayer.getEquipment(EquipmentSlot.BOOTS), I18n.t("replays.empty_boots")), Duration.ofSeconds(1));

        layout.slot(9, ItemStackCreator.getStack(
                I18n.t("replays.report_player"),
            Material.ANVIL,
            1,
                List.of(
                        I18n.t("replays.report_player_description"),
                        Component.empty(),
                        I18n.t("replays.click_to_report")
                )
        ), (_, c) -> c.player().notImplemented());
    }

    private static Component getDisplayName(ReplayPlayerEntity replayPlayer) {
        try {
            return LegacyComponentSerializer.legacySection().deserialize(
                    HypixelPlayer.getDisplayName(replayPlayer.getActualUuid()));
        } catch (Exception ignored) {
            return Component.text(replayPlayer.getPlayerName());
        }
    }

    private static ItemStack.Builder createEffectsItem(ReplayPlayerEntity replayPlayer) {
        List<TimedPotion> effects = new ArrayList<>(replayPlayer.getActiveEffects());
        if (effects.isEmpty()) {
            return ItemStackCreator.getStack(
                    I18n.t("replays.active_status_effects"),
                Material.POTION,
                1,
                    I18n.t("replays.no_status_effects")
            );
        }

        List<Component> lore = new ArrayList<>();
        for (TimedPotion timedPotion : effects) {
            String effectName = formatEffectName(timedPotion.potion().effect().toString());
            int amplifier = timedPotion.potion().amplifier() + 1;
            lore.add(I18n.t("replays.status_effect",
                    Argument.string("effect", effectName),
                    Argument.string("amplifier", StringUtility.getAsRomanNumeral(amplifier))));
        }

        return ItemStackCreator.getStack(
                I18n.t("replays.active_status_effects"),
            Material.POTION,
            1,
            lore
        );
    }

    private static ItemStack.Builder createEquipmentItem(ItemStack itemStack, Component emptyText) {
        if (itemStack == null || itemStack.isAir()) {
            return ItemStackCreator.getStack(
                emptyText,
                Material.RED_STAINED_GLASS_PANE,
                1
            );
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
