package net.swofty.type.thepark;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.CustomWorlds;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.entity.BlockDisplayEntity;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.tab.TablistManager;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.TextDisplayEntity;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.missions.thepark.jungle.*;
import net.swofty.type.skyblockgeneric.race.RaceInstance;
import net.swofty.type.skyblockgeneric.race.RaceManager;
import net.swofty.type.skyblockgeneric.tabmodules.AccountInformationModule;
import net.swofty.type.skyblockgeneric.tabmodules.SkyBlockPlayersOnlineModule;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.tab.AreaServerModule;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeTheParkLoader implements SkyBlockTypeLoader, RaceInstance {

	public static List<LivingEntity> entities = new ArrayList<>();
	private static final RaceManager raceManager = new RaceManager(new WoodsRacing());

	@Override
	public ServerType getType() {
		return ServerType.SKYBLOCK_THE_PARK;
	}

	@Override
	public void onInitialize(MinecraftServer server) {
		Logger.info("TypeTheParkLoader initialized!");
	}

	@Override
	public void afterInitialize(MinecraftServer server) {
		List<Point> trapBlocks = List.of(
				new BlockVec(-466, 119, -54),
				new BlockVec(-449, 120, -65),
				new BlockVec(-440, 122, -92)
		);
		int index = 0;
		for (Point trap : trapBlocks) {
			int finalIndex = index;
			InteractionEntity hitbox = new InteractionEntity(1f, 1f, (p, event) -> {
				SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
				if (!player.getMissionData().isCurrentlyActive(MissionPlaceTraps.class)) {
					return;
				}

				MissionData.ActiveMission activeMission = player.getMissionData().getMission(MissionPlaceTraps.class).getKey();
				List<Integer> placedTraps = (List<Integer>) activeMission.getCustomData().getOrDefault("placedTraps", new ArrayList<Integer>());
				if (placedTraps.contains(finalIndex)) {
					player.sendMessage("<c>This trap has already been placed.");
					return;
				}
				placedTraps.add(finalIndex);
				activeMission.setCustomData(Map.of("placedTraps", placedTraps));
				player.sendMessage("<a>Placed trap <7>(<e>{}<7>/<a>3<7>)", placedTraps.size());
				TypeTheParkLoader.entities.forEach(Entity::updateViewableRule);

				// check if all placed:
				if (placedTraps.size() >= 3) {
					player.getMissionData().endMission(MissionPlaceTraps.class);
				}
			});

			TextDisplayEntity text = new TextDisplayEntity("<a>Place Trap Here", meta -> {});
			hitbox.setInstance(HypixelConst.getInstanceContainer(), trap);
			text.setInstance(HypixelConst.getInstanceContainer(), trap.add(0, 1, 0));

			text.updateViewableRule(player -> {
				if (!(player instanceof SkyBlockPlayer p)) return false;
				if (shouldRenderTraps(p)) {
					return true;
				}
				if (p.getMissionData().isCurrentlyActive(MissionTalkToMolbertAgainAgainAgain.class)) {
					return finalIndex == 1;
				}
				if (p.getMissionData().isCurrentlyActive(MissionHelpMolbert.class)) {
					return finalIndex == 1;
				}
				if(!p.getMissionData().isCurrentlyActive(MissionPlaceTraps.class)) {
					return false;
				}

				MissionData.ActiveMission activeMission = p.getMissionData().getMission(MissionPlaceTraps.class).getKey();
				List<Integer> placedTraps = (List<Integer>) activeMission.getCustomData().getOrDefault("placedTraps", new ArrayList<Integer>());
				return !placedTraps.contains(finalIndex);
			});
			entities.add(text);

			float size = 0.6f;
			TrapdoorSide[] sides = {
					new TrapdoorSide(-size / 2, 0f, -size / 2 + 0.1f, 0f, -90f, 0f), // NORTH
					new TrapdoorSide(-size / 2 + 0.1f, 0f, size / 2, 0f, -90f, 90f), // WEST
					new TrapdoorSide(size / 2, 0f, size / 2, 0f, -90f, 90f), // EAST
					new TrapdoorSide(-size / 2, 0.3f, size / 2, 0f, -90f, 0f), // SOUTH
					new TrapdoorSide(-size / 2, 0.5f, -size / 2, 0f, 0f, 0f), // TOP
			};

			for (TrapdoorSide side : sides) {
				BlockDisplayEntity trapdoor = new BlockDisplayEntity(Block.OAK_TRAPDOOR, meta -> {
					meta.setScale(new Vec(size, size, size));
					Vec hingeOffset = new Vec(side.x(), side.y(), side.z());
					meta.setLeftRotation(rotation(side.tiltX(), side.rotY(), side.tiltZ()));
					meta.setTranslation(hingeOffset);
				});

				trapdoor.setInstance(HypixelConst.getInstanceContainer(), trap);
				trapdoor.updateViewableRule((p) -> {
					if (!(p instanceof SkyBlockPlayer player)) return false;
					if (shouldRenderTraps(player)) {
						return true;
					}
					if (player.getMissionData().isCurrentlyActive(MissionTalkToMolbertAgainAgainAgain.class)) {
						return finalIndex == 1;
					}
					if (player.getMissionData().isCurrentlyActive(MissionHelpMolbert.class)) {
						return finalIndex == 1;
					}
					if(!player.getMissionData().isCurrentlyActive(MissionPlaceTraps.class)) {
						return false;
					}

					MissionData.ActiveMission activeMission = player.getMissionData().getMission(MissionPlaceTraps.class).getKey();
					List<Integer> placedTraps = (List<Integer>) activeMission.getCustomData().getOrDefault("placedTraps", new ArrayList<Integer>());
					return placedTraps.contains(finalIndex);
				});
				entities.add(trapdoor);
			}


			index++;
		}

		MinecraftServer.getSchedulerManager().submitTask(() -> {
			for (SkyBlockPlayer player : SkyBlockGenericLoader.getLoadedPlayers()) {
				if (!player.getMissionData().isCurrentlyActive(MissionPlaceTraps.class)) continue;
				int i = 0;
				for (Point position : trapBlocks) {
					if (!(player.getPosition().distance(position) <= 25)) continue;

					MissionData.ActiveMission activeMission = player.getMissionData().getMission(MissionPlaceTraps.class).getKey();
					List<Integer> placedTraps = (List<Integer>) activeMission.getCustomData().getOrDefault("placedTraps", new ArrayList<Integer>());
					if (placedTraps.contains(i)) {
						i++;
						continue;
					}

					List<SendablePacket> packets = TrapParticles.getParticlePackets(position);
					player.sendPackets(packets);
					i++;
				}
			}
			return TaskSchedule.millis(800);
		}, ExecutionType.TICK_START);

		TrialOfFire.init();
		RyanScene.init();
	}

	private boolean shouldRenderTraps(SkyBlockPlayer player) {
		MissionData data = player.getMissionData();
		List<Class<? extends SkyBlockMission>> relevantMissions = List.of(
				MissionTalkToMolbertAgainAgain.class,
				MissionLeaveTheAreaAgain.class,
				MissionTalkToMolbertAgainAgainAgain.class
		);
		for (Class<? extends SkyBlockMission> missionClass : relevantMissions) {
			if (data.isCurrentlyActive(missionClass)) {
				return true;
			}
		}
		return false;
	}

	private static float[] rotation(float xDeg, float yDeg, float zDeg) {
		Quaternionf q = new Quaternionf()
				.rotationXYZ(
						(float) Math.toRadians(xDeg),
						(float) Math.toRadians(yDeg),
						(float) Math.toRadians(zDeg)
				);

		return new float[]{
				q.x, q.y, q.z, q.w
		};
	}

	@Override
	public RaceManager getRace() {
		return raceManager;
	}

	record TrapdoorSide(float x, float y, float z, float rotY, float tiltX, float tiltZ) {
	}


	@Override
	public LoaderValues getLoaderValues() {
		return new LoaderValues(
				(type) -> switch (type) {
					case SKYBLOCK_GALATEA -> new Pos(-483.5, 117, -41.5, -120, 0);
					default -> new Pos(-265.5, 79, -17.5, 90, 0);
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
						new AreaServerModule("tablist.server_info.area.the_park"),
						new AccountInformationModule()
				));
			}
		};
	}

	@Override
	public List<HypixelEventClass> getTraditionalEvents() {
		return SkyBlockGenericLoader.loopThroughPackage(
				"net.swofty.type.thepark.events",
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
				"net.swofty.type.thepark.npcs",
				HypixelNPC.class
		).toList());
	}


	@Override
	public List<RedisMessageHandler<?, ?>> getProxyHandlers() {
		return List.of();
	}

	@Override
	public List<ServiceType> getRequiredServices() {
		return new ArrayList<>();
	}

	@Override
	public @Nullable CustomWorlds getMainInstance() {
		return CustomWorlds.SKYBLOCK_THE_PARK;
	}
}
