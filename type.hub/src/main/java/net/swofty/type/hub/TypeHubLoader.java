package net.swofty.type.hub;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.registry.TagKey;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.hologram.ServerHolograms;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.AreaServerModule;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.hub.darkauction.DarkAuctionDisplay;
import net.swofty.type.hub.util.HubMap;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.entity.GlassDisplay;
import net.swofty.type.skyblockgeneric.furniture.Furniture;
import net.swofty.type.skyblockgeneric.gui.inventories.election.ElectionView;
import net.swofty.type.skyblockgeneric.gui.inventories.election.ElectionViewStatsView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.museum.MuseumDisplays;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.WarpPortal;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class TypeHubLoader implements SkyBlockTypeLoader {

	@Override
	public ServerType getType() {
		return ServerType.SKYBLOCK_HUB;
	}

	@Override
	public void onInitialize(MinecraftServer server) {
		Logger.info("TypeHubLoader initialized!");
	}

	@Override
	public void afterInitialize(MinecraftServer server) {
		Pos runePos = new Pos(23.5, 65.3, -135, 0, 0f);
		AtomicReference<Double> i = new AtomicReference<>(0D);
		MinecraftServer.getSchedulerManager().scheduleTask(() -> {
			List<Pos> locationsToDisplayParticle = new ArrayList<>();
			for (double x = 0; x < 1.25; x = x + 0.25) {
				locationsToDisplayParticle.add(new Pos(runePos.x() + Math.cos(i.get() - x) * 2.5, runePos.y() - x, runePos.z() + Math.sin(i.get() - x) * 2.5));
				locationsToDisplayParticle.add(new Pos(runePos.x() - Math.cos(i.get() - x) * 2.5, runePos.y() + x, runePos.z() - Math.sin(i.get() - x) * 2.5));
			}

			for (Pos pos : locationsToDisplayParticle) {
				SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
					player.sendPacket(new ParticlePacket(
							Particle.DUST.withColor(new Color(153, 0, 255)),
							false,
							true,
							(float) pos.x(),
							(float) pos.y(),
							(float) pos.z(),
							0,
							0,
							0,
							0,
							1
					));
				});
			}
			i.updateAndGet(v -> v + 1 / 10f);
		}, TaskSchedule.seconds(2), TaskSchedule.tick(1));

		// Register bazaar cache
		Logger.info("Registering bazaar cache");
		ProxyService bazaarService = new ProxyService(ServiceType.BAZAAR);

		// Register museum chunks
		Logger.info("Registering museum chunks");
		MuseumDisplays.getAllPositions().forEach(displayPosition -> {
			HypixelConst.getInstanceContainer().loadChunk(displayPosition).join();
		});

		GlassDisplay.create(new SkyBlockItem(ItemType.ABIPHONE_CONTACTS_TRIO), HypixelConst.getInstanceContainer(), new Pos(70, 81, -63), (player, _) -> {
			player.sendMessage("<e>Talk to <d>Elizabeth <e>in the <b>Community Center <e>to purchase!");
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.ABIPHONE_BASIC), HypixelConst.getInstanceContainer(), new Pos(70, 81, -56), (player, _) -> {
			player.sendMessage("<e>Talk to <6>Alda <e>to purchase!");
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.DIRT), HypixelConst.getInstanceContainer(), new Pos(25, 74, -44), (player, _) -> {
			player.notImplemented();
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.DIRT), HypixelConst.getInstanceContainer(), new Pos(28, 74, -47), (player, _) -> {
			player.notImplemented();
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.DIRT), HypixelConst.getInstanceContainer(), new Pos(32, 74, -48), (player, _) -> {
			player.notImplemented();
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.DIRT), HypixelConst.getInstanceContainer(), new Pos(35, 74, -47), (player, _) -> {
			player.notImplemented();
		});
		GlassDisplay.create(new SkyBlockItem(ItemType.DIRT), HypixelConst.getInstanceContainer(), new Pos(36, 74, -44), (player, _) -> {
			player.notImplemented();
		});

		// Create Dark Auction display
		DarkAuctionDisplay darkAuctionDisplay = new DarkAuctionDisplay(HypixelConst.getInstanceContainer());

		// Register callback to refresh Sirius NPC and Dark Auction display when state changes
		DarkAuctionHandler.setOnStateChangeCallback(darkAuctionDisplay::update);

        // Place forest trees
        //ForestTreePlacement.placeTrees(HypixelConst.getInstanceContainer()); TODO: fix this on new map

		final HubMap hubMap = new HubMap();
		hubMap.placeItemFrames(HypixelConst.getInstanceContainer());

		final UUID electionUUID = UUID.nameUUIDFromBytes("election".getBytes());

		ServerHolograms.addExternalHologram(
			ServerHolograms.ExternalHologram.builder()
				.uuid(electionUUID)
				.instance(HypixelConst.getInstanceContainer())
				.pos(new Pos(9.5, 81, 13.5))
				.text(electionLines())
				.build()
		);

		MinecraftServer.getSchedulerManager().buildTask(() ->
			ServerHolograms.updateExternalHologramText(electionUUID, electionLines())
		).delay(TaskSchedule.seconds(1)).repeat(TaskSchedule.seconds(1)).schedule();

		WarpPortal.create(HypixelConst.getInstanceContainer(), new BlockVec(13, 78, 13), Component.text("Election Room"), new Pos(0.5, 50, 45.5, -180, 0));
        WarpPortal.create(HypixelConst.getInstanceContainer(), new BlockVec(0, 49, 47), Component.text("Community Center"), new Pos(9.5, 79, 12.5, 90, 0));

		Furniture.load("hexatorum");
		Furniture.load("rune_table");
		Furniture.load("big_arrows"); // the things above the house in Combat Settlement
		Furniture.load("pufferfish", new Pos(115.5, 70, -30.5));
		Furniture.load("hub_swords", new Pos(-52.5, 69.25, -85.5));

		new InteractionEntity(1.1f, 1.1f, (player, _) -> {
			final SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
			String text = ElectionManager.getPlayerVote(skyBlockPlayer.getUuid());
			if (text == null) {
				player.openView(new ElectionView());
			} else {
				player.openView(new ElectionViewStatsView());
			}
		}).setInstance(HypixelConst.getInstanceContainer(), new Pos(0.5, 50, 34.5));

		MinecraftServer.getSchedulerManager().buildTask(ElectionDisplay::addAndUpdate).delay(TaskSchedule.seconds(1)).repeat(TaskSchedule.seconds(1)).schedule();
	}

	private static String[] electionLines() {
		return new String[]{
			"<e><l>MAYOR ELECTIONS",
			Text.of("<b>Year {}", SkyBlockCalendar.getYear()).serialize(),
			Text.of("<e>Time left: <a>{:time}",
				SkyBlockCalendar.ticksUntilEvent(CalendarEvent.ELECTION_CLOSE) * 50L).serialize()
		};
	}

    @Override
    public LoaderValues getLoaderValues() {
        return new LoaderValues(
                (type) -> switch (type) {
                    case SKYBLOCK_THE_FARMING_ISLANDS -> new Pos(84, 72, -150, 43, 0);
                    case SKYBLOCK_SPIDERS_DEN -> new Pos(-142.5, 75, -184.7, -30, 0);
                    case SKYBLOCK_GOLD_MINE -> new Pos(0, 64, -189, 0, 0);
                    case SKYBLOCK_DUNGEON_HUB -> new Pos(-33.5, 72, 25, 0, 0);
                    case SKYBLOCK_THE_PARK -> new Pos(-220, 73, -15, -90, 0);
                    default -> new Pos(0.5, 77, -0.5, -180, 0);
                }, // Spawn position
                true // Announce death messages
        );
    }

	public TablistManager getTablistManager() {
		return new TablistManager() {
			@Override
			public List<TablistModule> getModules() {
				return new ArrayList<>(List.of(
						new SkyBlockPlayersOnlineModule(1),
						new SkyBlockPlayersOnlineModule(2),
						new AreaServerModule("tablist.server_info.area.hub"),
						new AccountInformationModule()
				));
			}
		};
	}

	@Override
	public @Nullable RegistryKey<DimensionType> getDimensionType() {
		return MinecraftServer.getDimensionTypeRegistry().register(
			Key.key("skyblock:hub"),
			DimensionType.builder()
				.setAttribute(EnvironmentAttribute.CLOUD_HEIGHT, 192.33f)
				.ambientLight(1f)
				.skybox(DimensionType.Skybox.OVERWORLD)
				.setAttribute(EnvironmentAttribute.FOG_START_DISTANCE, 50f)
				.setAttribute(EnvironmentAttribute.FOG_END_DISTANCE, 1000f)
				.setAttribute(EnvironmentAttribute.FOG_COLOR, new Color(0xc0d8ff))
				.setAttribute(EnvironmentAttribute.SKY_COLOR, new Color(0x78a7ff))
				.setAttribute(EnvironmentAttribute.AMBIENT_LIGHT_COLOR, Color.WHITE)
				.timelines(MinecraftServer.getTimelineRegistry().getTag(TagKey.ofHash("#minecraft:in_overworld")))
				.skylight(true)
				.build());
	}

	@Override
	public List<HypixelEventClass> getTraditionalEvents() {
		return SkyBlockGenericLoader.loopThroughPackage(
				"net.swofty.type.hub.events",
				HypixelEventClass.class
		).collect(Collectors.toList());
	}

	@Override
	public List<HypixelEventClass> getCustomEvents() {
		return new ArrayList<>();
	}

	@Override
	public List<HypixelNPC> getNPCs() {
        return new ArrayList<>(SkyBlockGenericLoader.loopThroughPackage(
                "net.swofty.type.hub.npcs",
                HypixelNPC.class
        ).toList());
	}


	@Override
	public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
		return List.of();
	}

	@Override
	public List<ServiceType> getRequiredServices() {
		return List.of(ServiceType.ELECTION, ServiceType.AUCTION_HOUSE, ServiceType.BAZAAR, ServiceType.ITEM_TRACKER);
	}

	@Override
	public @Nullable CustomWorlds getMainInstance() {
		return CustomWorlds.SKYBLOCK_HUB;
	}
}
