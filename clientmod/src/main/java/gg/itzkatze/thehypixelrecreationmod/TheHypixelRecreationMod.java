package gg.itzkatze.thehypixelrecreationmod;

import gg.itzkatze.thehypixelrecreationmod.commands.*;
import gg.itzkatze.thehypixelrecreationmod.features.KeybindRegistry;
import gg.itzkatze.thehypixelrecreationmod.features.SpraySchemaRecorder;
import gg.itzkatze.thehypixelrecreationmod.features.fullcapture.FullCapture;
import gg.itzkatze.thehypixelrecreationmod.features.guicapture.GuiCaptureRecorder;
import gg.itzkatze.thehypixelrecreationmod.features.packetlog.EntityPacketLogger;
import gg.itzkatze.thehypixelrecreationmod.features.worldexport.ChunkExportRecorder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.fabric.event.HypixelModAPICallback;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TheHypixelRecreationMod implements ClientModInitializer {
	public static final String MOD_ID = "thehypixelrecreationmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initialized");

		registerCommands();
		KeybindRegistry.register();
		registerTickHandlers();
		registerHypixelApiHandlers();
	}

	private static void registerCommands() {
		PlayerSkinCommand.register();
		RavengardNPCCommand.register();
		GetArmorStandArmorColorsCommand.register();
		GetArmorStandInfos.register();
		GetScoreboardInfo.register();
		ChunkExporterCommand.register();
		CopyMapTextureCommand.register();
		CopyBiomeData.register();
		FetchBlockDisplaysCommand.register();
		SpraySchemaRecorderCommand.register();
		SoundNbsRecorderCommand.register();
		HeldItemDataCommand.register();
		NearbyEntitiesCommand.register();
        CopyTabBossbarCommand.register();
        CopyAdvancementsCommand.register();
		LogPacketsCommand.register();
		RavengardSessionCommand.register();
		HudCaptureCommand.register();
		FullCaptureCommand.register();
	}

	private static void registerTickHandlers() {
		ClientTickEvents.END_CLIENT_TICK.register(_ -> {
			ChunkExportRecorder.tick();
			SpraySchemaRecorder.tick();
			EntityPacketLogger.tick();
			gg.itzkatze.thehypixelrecreationmod.features.packetlog.RavengardSessionLogger.tick();
			GuiCaptureRecorder.tick(net.minecraft.client.Minecraft.getInstance());
			FullCapture.tick();
		});
	}

	private static void registerHypixelApiHandlers() {
		HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);

		HypixelModAPICallback.EVENT.register(clientboundHypixelPacket ->
				LOGGER.info("ClientboundHypixelPacket: {}", clientboundHypixelPacket));

		HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, packet ->
				LOGGER.info("ClientboundLocationPacket: {}", packet));
	}
}
