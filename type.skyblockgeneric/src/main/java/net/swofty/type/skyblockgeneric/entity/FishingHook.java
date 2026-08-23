package net.swofty.type.skyblockgeneric.entity;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FishingHookMeta;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.FishingService;
import net.swofty.type.skyblockgeneric.fishing.FishingSession;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.FishingBaitComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FishingHook {

	public static final List<FishingHook> activeHooks = new ArrayList<>();
	private static final double INITIAL_SPEED_MULTIPLIER = 10.0;
	private static final double BOB_SPEED = 0.1;
	private static final double BOB_AMOUNT = 0.09;
	private static final double SURFACE_OFFSET = -0.02;
	private static final double MAX_PULL_DOWN = 0.3;
	private static final double PULL_DOWN_RECOVERY_SPEED = 0.016666666666666666;
	private static final double CONTROLLER_DRAG = 0.2;
	private static final double WATER_CHECK_OFFSET = -0.2;
	private static final long BITE_WINDOW_TICKS = 20L;
	private final SkyBlockPlayer owner;
	private final SkyBlockItem rod;
	private final FishingMedium requiredMedium;
	private final Entity hook;
	private final Entity controller;
	@Getter
	private boolean isRemoved = false;
	private double bobTick = 0;
	private double pullDownOffset = 0;
	private Double stableWaterY = null;
	private Task nextBiteTask;
	private Task biteExpiryTask;
	private boolean sessionStarted = false;

	public FishingHook(SkyBlockPlayer owner, SkyBlockItem rod) {
		this.owner = owner;
		this.rod = rod;
		String itemId = rod.getAttributeHandler().getPotentialType() == null ? null : rod.getAttributeHandler().getPotentialType().name();
        var rodMetadata = FishingItemSupport.getRodMetadata(itemId);
        this.requiredMedium = rodMetadata == null ? FishingMedium.WATER : rodMetadata.getMedium();

		this.hook = new Entity(EntityType.FISHING_BOBBER);
		this.hook.editEntityMeta(FishingHookMeta.class, meta -> {
			meta.setOwnerEntity(owner);
		});

		this.controller = new Entity(EntityType.TEXT_DISPLAY);
		this.controller.setNoGravity(true);

		this.controller.eventNode().addListener(EntityTickEvent.class,
				event -> tick(event.getEntity()));
	}

	public static FishingHook getFishingHookForOwner(@NotNull SkyBlockPlayer owner) {
		for (FishingHook fishingHook : activeHooks) {
			if (fishingHook.owner.getUuid().equals(owner.getUuid())) {
				return fishingHook;
			}
		}
		return null;
	}

	private static boolean blockMatchesMedium(@NotNull Block block, @NotNull FishingMedium medium) {
		if (!block.liquid()) {
			return false;
		}
		String blockName = block.name();
		return switch (medium) {
			case WATER -> blockName.contains("water");
			case LAVA -> blockName.contains("lava");
		};
	}

	public void spawn(Instance instance) {
		Pos spawnPos = owner.getPosition().add(0, owner.getEyeHeight(), 0);
		spawn(instance, spawnPos);
	}

	public void spawn(Instance instance, Pos pos) {
		activeHooks.add(this);
		this.controller.setInstance(instance, pos);
		this.hook.setInstance(instance, pos);
		this.controller.addPassenger(hook);
		this.controller.setVelocity(owner.getPosition().direction().mul(INITIAL_SPEED_MULTIPLIER));
		float pitch = (float) 1/3 + (float) (Math.random() * (0.5 - 1.0 / 3.0));
		owner.playSound(
				Sound.sound()
						.type(Key.key("entity.fishing_bobber.throw"))
						.source(Sound.Source.NEUTRAL)
						.volume(0.5f)
						.pitch(pitch)
						.build()
		);
	}

	private boolean shouldKeepExisting() {
		// Checks if the user is still in the same instance as the hook
		Instance instance = controller.getInstance();
		if (instance == null) return false;
		if (owner.getInstance() != instance) return false;
		// Checks if the owner is still holding a fishing rod
		return owner.getItemInMainHand().material() == Material.FISHING_ROD;
	}

	private void tick(@NotNull Entity controller) {
		Instance instance = controller.getInstance();
		if (instance == null) return;
		if (!shouldKeepExisting()) {
			remove();
			return;
		}

		Pos pos = controller.getPosition();
		if (notInWater()) {
			stableWaterY = null;
			controller.setNoGravity(false); // Re-enable gravity to fall/stop
			return;
		}

		FishingMedium mediumAtHook = getCurrentMedium();
		if (mediumAtHook == null) {
			if (sessionStarted) {
				FishingService.clearSession(owner.getUuid());
				sessionStarted = false;
			}
			stableWaterY = null;
			cancelBiteTasks();
			controller.setNoGravity(false);
			return;
		}

		if (mediumAtHook != requiredMedium) {
			remove();
			return;
		}

		// Hook is in valid fishing liquid
		controller.setNoGravity(true);
		Block blockBelow = instance.getBlock(pos.add(0, WATER_CHECK_OFFSET, 0));

		if (stableWaterY == null) {
			// First time hitting water, establish stable surface Y
			stableWaterY = getWaterSurface(blockBelow, (int) Math.floor(pos.y() + WATER_CHECK_OFFSET)) + SURFACE_OFFSET;
			bobTick = 0;
			pullDownOffset = 0;
			// show particles
			instance.playSound(Sound.sound()
					.type(SoundEvent.ENTITY_FISHING_BOBBER_SPLASH)
					.source(Sound.Source.PLAYER)
					.volume(0.25f)
					.pitch(0.8f) // 0.6-1.4 in vanilla
					.build()
			);
			instance.sendGroupedPacket(new ParticlePacket(
					Particle.SPLASH,
					controller.getPosition(),
					new Pos(0.1, 0.001, 0.1),
					0, 5
			));
			startFishingSession();
		}

		bobTick += BOB_SPEED;
		double yBob = Math.sin(bobTick) * BOB_AMOUNT;

		pullDownOffset = Math.max(pullDownOffset - PULL_DOWN_RECOVERY_SPEED, 0);
		Pos targetPos = new Pos(pos.x(), stableWaterY + yBob - pullDownOffset, pos.z());

		controller.teleport(targetPos);
		controller.setVelocity(controller.getVelocity().mul(CONTROLLER_DRAG));
	}


	public boolean notInWater() {
		Instance instance = controller.getInstance();
		Pos pos = controller.getPosition();
		return getMedium(instance.getBlock(pos)) == null
			&& getMedium(instance.getBlock(pos.add(0, WATER_CHECK_OFFSET, 0))) == null;
	}

	public void showBiteAnimation() {
		Instance instance = controller.getInstance();
		if (instance == null) return;

		instance.playSound(Sound.sound()
				.type(SoundEvent.ENTITY_FISHING_BOBBER_SPLASH)
				.source(Sound.Source.PLAYER)
				.build()
		);
		instance.sendGroupedPacket(new ParticlePacket(
				Particle.SPLASH,
				controller.getPosition(),
				new Pos(0.1, 0.001, 0.1),
				0, 20
		));

		pullDownOffset = Math.min(pullDownOffset + 0.15, MAX_PULL_DOWN);
		bobTick += Math.PI / 6;
	}

	public void remove() {
		activeHooks.remove(this);
		if (this.isRemoved) return;
		this.isRemoved = true;
		cancelBiteTasks();
		FishingService.clearSession(owner.getUuid());
		hook.remove();
		controller.remove();
	}

	public void tryRetrieve(SkyBlockItem rodInHand) {
		FishingSession session = FishingService.getSession(owner.getUuid());
		if (session == null || !session.biteReady() || System.currentTimeMillis() > session.biteWindowEndsAt()) {
			return;
		}

		FishingService.resolveCatch(owner, rodInHand, this);
	}

	private double getWaterSurface(Block block, int blockY) {
		if (!block.liquid()) return blockY + 1.0;

		String levelStr = block.getProperty("level");
		int level = (levelStr == null) ? 0 : Integer.parseInt(levelStr);

		return blockY + ((level == 0) ? 1.0 : (8 - level) / 8.0);
	}

	public Scheduler getScheduler() {
		return hook.scheduler();
	}

	public Instance getInstance() {
		return controller.getInstance();
	}

	public Pos getSpawnPosition() {
		return controller.getPosition();
	}

	private void startFishingSession() {
		if (sessionStarted) {
			return;
		}

		FishingSession session = FishingService.beginCast(owner, rod, requiredMedium);
		sessionStarted = true;
        FishingBaitComponent bait = FishingItemSupport.getBait(session.baitItemId());
		long waitTicks = FishingService.computeWaitTicks(owner, rod, bait);
		scheduleNextBite(waitTicks);
	}

	private void scheduleNextBite(long delayTicks) {
		cancelBiteTasks();
		nextBiteTask = hook.scheduler().buildTask(() -> {
			if (isRemoved || notInWater()) {
				return;
			}

			FishingSession session = FishingService.getSession(owner.getUuid());
			if (session == null || session.resolved()) {
				return;
			}

			long now = System.currentTimeMillis();
			FishingService.updateSession(session.withBiteTiming(now, now + (BITE_WINDOW_TICKS * 50)).withBiteReady(true));
			showBiteAnimation();

			biteExpiryTask = hook.scheduler().buildTask(() -> {
				FishingSession activeSession = FishingService.getSession(owner.getUuid());
				if (activeSession == null || activeSession.resolved() || isRemoved) {
					return;
				}

				FishingService.updateSession(activeSession.withBiteReady(false));
                FishingBaitComponent bait = FishingItemSupport.getBait(activeSession.baitItemId());
				long nextDelay = FishingService.computeWaitTicks(owner, rod, bait);
				scheduleNextBite(nextDelay);
			}).delay(TaskSchedule.tick((int) BITE_WINDOW_TICKS)).schedule();
		}).delay(TaskSchedule.tick((int) delayTicks)).schedule();
	}

	private void cancelBiteTasks() {
		if (nextBiteTask != null) {
			nextBiteTask.cancel();
			nextBiteTask = null;
		}
		if (biteExpiryTask != null) {
			biteExpiryTask.cancel();
			biteExpiryTask = null;
		}
	}

	private FishingMedium getCurrentMedium() {
		Instance instance = controller.getInstance();
		if (instance == null) {
			return null;
		}

		Pos pos = controller.getPosition();
		FishingMedium current = getMedium(instance.getBlock(pos));
		if (current != null) {
			return current;
		}
		return getMedium(instance.getBlock(pos.add(0, WATER_CHECK_OFFSET, 0)));
	}

	private FishingMedium getMedium(Block block) {
		if (blockMatchesMedium(block, FishingMedium.WATER)) {
			return FishingMedium.WATER;
		}
		if (blockMatchesMedium(block, FishingMedium.LAVA)) {
			return FishingMedium.LAVA;
		}
		return null;
	}
}
