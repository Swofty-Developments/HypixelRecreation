package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.context.CommandContext;
import gg.itzkatze.thehypixelrecreationmod.features.packetlog.SkyBlockSessionLogger;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Path;

public final class SkyBlockCaptureCommand {
    private SkyBlockCaptureCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) ->
                dispatcher.register(ClientCommands.literal("skyblockcapture")
                        .then(ClientCommands.literal("start").executes(SkyBlockCaptureCommand::start))
                        .then(ClientCommands.literal("stop").executes(SkyBlockCaptureCommand::stop))));
    }

    private static int start(CommandContext<FabricClientCommandSource> context) {
        if (SkyBlockSessionLogger.isActive()) {
            context.getSource().sendFeedback(Component.literal("§cA SkyBlock capture is already running."));
            return 0;
        }
        try {
            Path path = SkyBlockSessionLogger.start();
            context.getSource().sendFeedback(Component.literal("§aSkyBlock capture started → §f" + path.getFileName()));
            return 1;
        } catch (IOException | RuntimeException exception) {
            context.getSource().sendFeedback(Component.literal("§cCould not start capture: " + exception.getMessage()));
            return 0;
        }
    }

    private static int stop(CommandContext<FabricClientCommandSource> context) {
        if (!SkyBlockSessionLogger.isActive()) {
            context.getSource().sendFeedback(Component.literal("§cNo SkyBlock capture is running."));
            return 0;
        }
        SkyBlockSessionLogger.StopResult result = SkyBlockSessionLogger.stop();
        context.getSource().sendFeedback(Component.literal("§aSaved " + result.totalPackets()
                + " useful packets → §f" + result.path()));
        return 1;
    }
}
