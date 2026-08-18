package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import gg.itzkatze.thehypixelrecreationmod.features.fullcapture.FullCapture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public final class FullCaptureCommand {
    private FullCaptureCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> {
            dispatcher.register(tree());
            RecreationCommand.register(dispatcher, tree());
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> tree() {
        return ClientCommands.literal("fullcapture")
                .executes(FullCaptureCommand::status)
                .then(ClientCommands.literal("start")
                        .executes(context -> start(context, "capture", false))
                        .then(ClientCommands.argument("name", StringArgumentType.word())
                                .executes(context -> start(context, StringArgumentType.getString(context, "name"), false))
                                .then(ClientCommands.literal("raw")
                                        .executes(context -> start(context,
                                                StringArgumentType.getString(context, "name"), true)))))
                .then(ClientCommands.literal("stop")
                        .executes(context -> stop(context, true))
                        .then(ClientCommands.literal("nopolar").executes(context -> stop(context, false))))
                .then(ClientCommands.literal("status").executes(FullCaptureCommand::status))
                .then(ClientCommands.literal("split")
                        .executes(context -> split(context, null))
                        .then(ClientCommands.argument("label", StringArgumentType.word())
                                .executes(context -> split(context, StringArgumentType.getString(context, "label")))))
                .then(ClientCommands.literal("note")
                        .then(ClientCommands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> note(context, StringArgumentType.getString(context, "text")))));
    }

    private static int start(CommandContext<FabricClientCommandSource> context, String name, boolean raw) {
        try {
            Path directory = FullCapture.start(name, raw);
            feedback(context, "§aFull capture started" + (raw ? " §7(raw packets)" : "") + " §f" + directory);
            feedback(context, "§7Play normally, switch worlds freely, then run §f/fullcapture stop§7.");

            int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();
            if (renderDistance < 16) {
                feedback(context, "§eRender distance is " + renderDistance
                        + " chunks; raise it to capture more of each world per pass.");
            }
            return 1;
        } catch (Exception exception) {
            feedback(context, "§cFailed to start full capture: " + exception.getMessage());
            return 0;
        }
    }

    private static int stop(CommandContext<FabricClientCommandSource> context, boolean convertPolar) {
        try {
            if (convertPolar) {
                feedback(context, "§7Stopping and converting worlds, the client will hang for a moment...");
            }
            FullCapture.StopResult result = FullCapture.stop(convertPolar);
            feedback(context, "§aFull capture stopped: "
                    + result.worlds() + " worlds, "
                    + result.chunks() + " chunks, "
                    + result.events() + " events from "
                    + result.packets() + " packets"
                    + (convertPolar ? ", " + result.polarWorlds() + " polar worlds" : "")
                    + " → §f" + result.directory());
            return 1;
        } catch (Exception exception) {
            feedback(context, "§cFailed to stop full capture: " + exception.getMessage());
            return 0;
        }
    }

    private static int status(CommandContext<FabricClientCommandSource> context) {
        if (!FullCapture.isActive()) {
            feedback(context, "§cNo full capture is running. Start one with §f/fullcapture start <name>§c.");
            return 0;
        }

        FullCapture.Status status = FullCapture.status();
        feedback(context, "§aFull capture §f" + status.name() + "§a running for §f"
                + FullCapture.describeDuration(status.elapsedMs()));
        feedback(context, "§7worlds §f" + status.worlds() + "§7 (current " + status.currentWorld()
                + "), chunks §f" + status.chunks());
        feedback(context, "§7events §f" + status.events() + "§7 from §f" + status.packets()
                + "§7 packets, menus §f" + status.screens()
                + "§7, scoreboards §f" + status.scoreboardUpdates()
                + "§7, entity spawns §f" + status.entitySpawns());
        if (status.failures() > 0) {
            feedback(context, "§e" + status.failures() + " capture errors were swallowed");
        }
        feedback(context, "§7→ §f" + status.directory());
        return 1;
    }

    private static int split(CommandContext<FabricClientCommandSource> context, String label) {
        if (!FullCapture.isActive()) {
            feedback(context, "§cNo full capture is running.");
            return 0;
        }
        FullCapture.split(label);
        feedback(context, "§aStarted a new world segment.");
        return 1;
    }

    private static int note(CommandContext<FabricClientCommandSource> context, String text) {
        if (!FullCapture.isActive()) {
            feedback(context, "§cNo full capture is running.");
            return 0;
        }
        FullCapture.note(text);
        feedback(context, "§aNoted: §f" + text);
        return 1;
    }

    private static void feedback(CommandContext<FabricClientCommandSource> context, String message) {
        context.getSource().sendFeedback(Component.literal(message));
    }
}
