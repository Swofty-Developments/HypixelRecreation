package net.swofty.pvp.feature.projectile;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.property.ServerProperties;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import net.swofty.pvp.entity.projectile.FishingBobber;
import net.swofty.pvp.events.FishingBobberRetrieveEvent;
import net.swofty.pvp.feature.FeatureType;
import net.swofty.pvp.feature.RegistrableFeature;
import net.swofty.pvp.feature.config.DefinedFeature;
import net.swofty.pvp.feature.config.FeatureConfiguration;
import net.swofty.pvp.feature.item.ItemDamageFeature;
import net.swofty.pvp.utils.CombatVersion;
import net.swofty.pvp.utils.ViewUtil;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla implementation of {@link FishingRodFeature}
 */
public class VanillaFishingRodFeature implements FishingRodFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaFishingRodFeature> DEFINED = new DefinedFeature<>(
			FeatureType.FISHING_ROD, VanillaFishingRodFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);

	public static final Tag<FishingBobber> FISHING_BOBBER = Tag.Transient("fishingBobber");

	private final FeatureConfiguration configuration;

	private ItemDamageFeature itemDamageFeature;
	private CombatVersion version;

	public VanillaFishingRodFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (event.getItemStack().material() != Material.FISHING_ROD) return;

			ThreadLocalRandom random = ThreadLocalRandom.current();
			Player player = event.getPlayer();

			if (player.hasTag(FISHING_BOBBER)) {
				FishingBobber bobber = player.getTag(FISHING_BOBBER);

				FishingBobberRetrieveEvent retrieveEvent = new FishingBobberRetrieveEvent(player, bobber);
				EventDispatcher.callCancellable(retrieveEvent, () -> {
					int durability = bobber.retrieve();
					if (player.getGameMode() != GameMode.CREATIVE)
						itemDamageFeature.damageEquipment(player, event.getHand() == PlayerHand.MAIN ?
								EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, durability);

					ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
							SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE, Sound.Source.NEUTRAL,
							1.0f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
					), player);
				});
			} else {
				ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
						SoundEvent.ENTITY_FISHING_BOBBER_THROW, Sound.Source.NEUTRAL,
						0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
				), player);

				FishingBobber bobber = new FishingBobber(player, version.legacy());
				player.setTag(FISHING_BOBBER, bobber);

				EntityShootEvent shootEvent = new EntityShootEvent(player, bobber,
						player.getPosition(), 0, 1.0);
				EventDispatcher.call(shootEvent);
				if (shootEvent.isCancelled()) {
					bobber.remove();
					return;
				}
				double spread = shootEvent.getSpread() * (version.legacy() ? 0.0075 : 0.0045);

				Pos playerPos = player.getPosition();
				float playerPitch = playerPos.pitch();
				float playerYaw = playerPos.yaw();

				float zDir = (float) Math.cos(Math.toRadians(-playerYaw) - Math.PI);
				float xDir = (float) Math.sin(Math.toRadians(-playerYaw) - Math.PI);
				double x = playerPos.x() - (double) xDir * 0.3D;
				double y = playerPos.y() + player.getEyeHeight();
				double z = playerPos.z() - (double) zDir * 0.3D;
				bobber.setInstance(Objects.requireNonNull(player.getInstance()), new Pos(x, y, z));

				Vec velocity;

				if (version.modern()) {
					velocity = new Vec(
							-xDir,
							MathUtils.clamp(-(
									(float) Math.sin(Math.toRadians(-playerPitch)) /
											(float) -Math.cos(Math.toRadians(-playerPitch))
							), -5.0F, 5.0F),
							-zDir
					);
					double length = velocity.length();
					velocity = velocity.mul(
							0.6D / length + 0.5D + random.nextGaussian() * spread,
							0.6D / length + 0.5D + random.nextGaussian() * spread,
							0.6D / length + 0.5D + random.nextGaussian() * spread
					);
				} else {
					double maxVelocity = 0.4F;
					velocity = new Vec(
							-Math.sin(playerYaw / 180.0F * (float) Math.PI)
									* Math.cos(playerPitch / 180.0F * (float) Math.PI) * maxVelocity,
							-Math.sin(playerPitch / 180.0F * (float) Math.PI) * maxVelocity,
							Math.cos(playerYaw / 180.0F * (float) Math.PI)
									* Math.cos(playerPitch / 180.0F * (float) Math.PI) * maxVelocity
					);
					double length = velocity.length();
					velocity = velocity
							.div(length)
							.add(
									random.nextGaussian() * spread,
									random.nextGaussian() * spread,
									random.nextGaussian() * spread
							)
							.mul(1.5);
				}

				//TODO fix velocity code
				bobber.setVelocity(velocity.mul(ServerProperties.SERVER_TICKS_PER_SECOND.get() * 0.75));
			}
		});
	}
}
