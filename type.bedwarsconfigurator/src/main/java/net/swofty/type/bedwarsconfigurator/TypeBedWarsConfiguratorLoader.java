package net.swofty.type.bedwarsconfigurator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.HypixelTypeLoader;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.EmptyTabModule;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TypeBedWarsConfiguratorLoader implements HypixelTypeLoader {
	private static Gson gson;

	@Getter
	private static BedWarsMapsConfig mapsConfig;

	@Override
	public ServerType getType() {
		return ServerType.BEDWARS_CONFIGURATOR;
	}

	@Override
	public java.util.List<Class<? extends net.swofty.type.generic.data.GameDataHandler>> getAdditionalDataHandlers() {
		return java.util.List.of(net.swofty.type.generic.data.handlers.BedWarsDataHandler.class);
	}

	@Override
	public void onInitialize(MinecraftServer server) {
		gson = new GsonBuilder().create();
		loadMapsConfig();
	}

	private static void loadMapsConfig() {
		Path mapsPath = Path.of("./configuration/bedwars/maps.json");
		if (!Files.exists(mapsPath)) {
			Logger.error("maps.json not found at {}", mapsPath.toAbsolutePath());
			return;
		}
		try (InputStream in = Files.newInputStream(mapsPath)) {
			byte[] bytes = in.readAllBytes();
			String json = new String(bytes, StandardCharsets.UTF_8);
			mapsConfig = gson.fromJson(json, BedWarsMapsConfig.class);
		} catch (Exception e) {
			Logger.error("Failed to load maps.json", e);
		}
	}

	/**
	 * Reloads the map configuration from the file
	 */
	public static void reloadMapsConfig() {
		if (gson == null) {
			gson = new GsonBuilder().create();
		}
		loadMapsConfig();
		Logger.info("Reloaded maps.json configuration");
	}

	@Override
	public void afterInitialize(MinecraftServer server) {
		HypixelGenericLoader.loopThroughPackage("net.swofty.type.bedwarsconfigurator.commands", HypixelCommand.class).forEach(command -> {
			try {
				MinecraftServer.getCommandManager().register(command.getCommand());
			} catch (Exception e) {
				Logger.error(e, "Failed to register command " + command.getCommand().getName() + " in class " + command.getClass().getSimpleName());
			}
		});
	}

	@Override
	public List<ServiceType> getRequiredServices() {
		return List.of();
	}

	@Override
	public TablistManager getTablistManager() {
		return new TablistManager() {
			@Override
			public List<TablistModule> getModules() {
				return List.of(
						new EmptyTabModule(),
						new EmptyTabModule(),
						new EmptyTabModule(),
						new EmptyTabModule()
				);
			}
		};
	}

	@Override
	public LoaderValues getLoaderValues() {
		return new LoaderValues(
				(type) -> new Pos(0, 100, 0, -90, 0), // Spawn position
				false // Announce death messages
		);
	}

	@Override
	public List<HypixelEventClass> getTraditionalEvents() {
		return HypixelGenericLoader.loopThroughPackage(
				"net.swofty.type.bedwarsconfigurator.events",
				HypixelEventClass.class
		).toList();
	}

	@Override
	public List<HypixelEventClass> getCustomEvents() {
		return HypixelGenericLoader.loopThroughPackage(
				"net.swofty.type.bedwarsconfigurator.events.custom",
				HypixelEventClass.class
		).toList();
	}

	@Override
	public List<HypixelNPC> getNPCs() {
		return HypixelGenericLoader.loopThroughPackage(
				"net.swofty.type.bedwarsconfigurator.npcs",
				HypixelNPC.class
		).toList();
	}


	@Override
	public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
		return List.of();
	}

	@Override
	public @Nullable CustomWorlds getMainInstance() {
		return CustomWorlds.BEDWARS_LOBBY;
	}

}
