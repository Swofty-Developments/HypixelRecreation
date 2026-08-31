package gg.itzkatze.thehypixelrecreationmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import gg.itzkatze.thehypixelrecreationmod.features.worldexport.ChunkExportRecorder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

public final class ChunkExporterCommand {
    private ChunkExporterCommand() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                RecreationCommand.register(dispatcher, ClientCommands.literal("chunkexporter")
                        .then(ClientCommands.literal("start")
                                .executes(context -> start(context, ChunkExportRecorder.CaptureMode.CHUNKS))
                                .then(ClientCommands.literal("block_displays")
                                        .executes(context -> start(context, ChunkExportRecorder.CaptureMode.BLOCK_DISPLAYS)))
                                .then(ClientCommands.literal("bedwars")
                                        .executes(context -> start(context, ChunkExportRecorder.CaptureMode.BEDWARS)))
                                .then(ClientCommands.literal("ravengard")
                                        .then(ClientCommands.argument("name", StringArgumentType.string())
                                                .executes(context -> startStitchedRavengard(context,
                                                        StringArgumentType.getString(context, "name"))))))
                        .then(ClientCommands.literal("stop")
                                .then(ClientCommands.argument("name", StringArgumentType.string())
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            try {
                                                ChunkExportRecorder.StopResult result = ChunkExportRecorder.stop(name);
                                                context.getSource().sendFeedback(Component.literal(
                                                        "§aSaved chunk export session '"
                                                                + result.sessionName()
                                                                + "' with "
                                                                + result.chunkCount()
                                                                + " chunks, "
                                                                + result.sectionCount()
                                                                + " sections, "
                                                                + result.blockEntityCount()
                                                                + " block entities, and "
                                                                + result.blockDisplayCount()
                                                                + " stationary block displays, and "
                                                                + result.ravengardObjectCount()
                                                                + " Ravengard dungeon objects to "
                                                                + result.path().getFileName()
                                                                + " and "
                                                                + result.polarPath().getFileName()
                                                                + " with "
                                                                + result.customBiomeCount()
                                                                + " custom biome definitions"
                                                ));
                                                return 1;
                                            } catch (Exception exception) {
                                                context.getSource().sendFeedback(Component.literal("§cFailed to stop chunk export session: " + exception.getMessage()));
                                                return 0;
                                            }
                                        })))
                        .then(ClientCommands.literal("status")
                                .executes(context -> {
                                    if (!ChunkExportRecorder.isActive()) {
                                        context.getSource().sendFeedback(Component.literal("§eNo chunk export session is active."));
                                        return 1;
                                    }

                                    ChunkExportRecorder.Status status = ChunkExportRecorder.getStatus();
                                    context.getSource().sendFeedback(Component.literal(
                                            "§eChunk export session active in "
                                                    + status.dimension()
                                                    + ": "
                                                    + status.chunkCount()
                                                    + " captured chunks, "
                                                    + status.blockDisplayCount()
                                                    + " stationary block displays, "
                                                    + status.ravengardObjectCount()
                                                    + " Ravengard dungeon objects, and "
                                                    + status.movingEntityCount()
                                                    + " moving displays excluded (mode: "
                                                    + status.mode().name().toLowerCase()
                                                    + ")"
                                    ));
                                    return 1;
                                }))));
    }

    private static int start(
        CommandContext<FabricClientCommandSource> context,
        ChunkExportRecorder.CaptureMode mode
    ) {
        try {
            ChunkExportRecorder.StartResult result = ChunkExportRecorder.start(mode);
            context.getSource().sendFeedback(Component.literal(
                "§aChunk export session started. Captured "
                    + result.initialChunkCount()
                    + " loaded chunks and "
                    + result.initialBlockDisplayCount()
                    + " block displays in "
                    + result.dimension()
                    + " (mode: "
                    + result.mode().name().toLowerCase()
                    + ")"
            ));
            return 1;
        } catch (Exception exception) {
            context.getSource().sendFeedback(Component.literal("§cFailed to start chunk export session: " + exception.getMessage()));
            return 0;
        }
    }

    private static int startStitchedRavengard(CommandContext<FabricClientCommandSource> context, String name) {
        try {
            ChunkExportRecorder.StartResult result = ChunkExportRecorder.start(ChunkExportRecorder.CaptureMode.RAVENGARD, name);
            context.getSource().sendFeedback(Component.literal(
                    "§aRavengard stitched session '" + name + "' is active with "
                            + result.initialChunkCount() + " chunks and "
                            + result.initialBlockDisplayCount() + " static displays. Stop it with the same name."));
            return 1;
        } catch (Exception exception) {
            context.getSource().sendFeedback(Component.literal("§cFailed to start stitched Ravengard session: " + exception.getMessage()));
            return 0;
        }
    }
}
