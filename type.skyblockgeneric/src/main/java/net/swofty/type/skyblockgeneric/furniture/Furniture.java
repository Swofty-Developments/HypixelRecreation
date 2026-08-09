package net.swofty.type.skyblockgeneric.furniture;

import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.ResolvableProfile;
import net.swofty.type.generic.HypixelConst;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Furniture {

	private static final File FURNITURE_DIR = new File("./configuration/skyblock/furniture");
	private static final Pattern TEXTURE_PATTERN = Pattern.compile("name:\\\"textures\\\",value:\\\"([^\\\"]+)\\\"");

	public static List<LivingEntity> load(String furnitureName) {
		return load(furnitureName, new Pos(0, 0, 0));
	}

	public static List<LivingEntity> load(String furnitureName, Pos offset) {
		return load(HypixelConst.getInstanceContainer(), furnitureName, offset);
	}

	public static List<LivingEntity> load(final Instance instance, final String furnitureName, final Pos offset) {
		try {
			if (instance == null) {
				throw new IllegalStateException("Instance is not initialized");
			}

			final File file = new File(FURNITURE_DIR, furnitureName.toLowerCase() + ".json");
			if (!file.exists()) {
				throw new FurnitureNotPresent("Furniture file not found: " + file.getPath());
			}

			final String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			final JSONArray entries = new JSONArray(content);

			final List<LivingEntity> spawned = new ArrayList<>();

			for (int i = 0; i < entries.length(); i++) {
				JSONObject entry = entries.getJSONObject(i);
				String type = entry.getString("type");

				if ("minecraft:item_display".equals(type)) {
					LivingEntity entity = createItemDisplay(entry);
					spawnEntity(entity, entry, offset, instance);
					spawned.add(entity);
					continue;
				}

				if ("minecraft:block_display".equals(type)) {
					LivingEntity entity = createBlockDisplay(entry);
					spawnEntity(entity, entry, offset, instance);
					spawned.add(entity);
					continue;
				}

				if ("minecraft:armor_stand".equals(type)) {
					LivingEntity entity = createArmorStand(entry);
					spawnEntity(entity, entry, offset, instance);
					spawned.add(entity);
				}
			}

			return spawned;
		} catch (Exception exception) {
			throw new FurnitureLoadException("Failed to load furniture '" + furnitureName + "'", exception);
		}
	}

	private static void spawnEntity(LivingEntity entity, JSONObject entry, Pos offset, Instance instance) {
		final JSONObject position = entry.getJSONObject("position");
		final JSONObject rotation = entry.optJSONObject("rotation");

		final double x = position.getDouble("x") + offset.x();
		final double y = position.getDouble("y") + offset.y();
		final double z = position.getDouble("z") + offset.z();

		final float yaw = rotation == null ? 0f : (float) rotation.optDouble("yaw", 0d);
		final float pitch = rotation == null ? 0f : (float) rotation.optDouble("pitch", 0d);

		entity.setInstance(instance, new Pos(x, y, z, yaw, pitch));
	}

	private static LivingEntity createItemDisplay(final JSONObject entry) {
		final LivingEntity entity = new LivingEntity(EntityType.ITEM_DISPLAY);
		entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
			meta.setHasNoGravity(true);

			JSONObject translation = entry.getJSONObject("translation");
			JSONObject scale = entry.getJSONObject("scale");
			JSONObject leftRotation = entry.getJSONObject("leftRotation");
			JSONObject rightRotation = entry.getJSONObject("rightRotation");
			JSONObject item = entry.getJSONObject("item");

			meta.setTranslation(new Vec(
					translation.getDouble("x"),
					translation.getDouble("y"),
					translation.getDouble("z")
			));

			meta.setScale(new Vec(
					scale.getDouble("x"),
					scale.getDouble("y"),
					scale.getDouble("z")
			));

			meta.setLeftRotation(new float[] {
					(float) leftRotation.getDouble("x"),
					(float) leftRotation.getDouble("y"),
					(float) leftRotation.getDouble("z"),
					(float) leftRotation.getDouble("w")
			});

			meta.setRightRotation(new float[] {
					(float) rightRotation.getDouble("x"),
					(float) rightRotation.getDouble("y"),
					(float) rightRotation.getDouble("z"),
					(float) rightRotation.getDouble("w")
			});

			meta.setItemStack(buildItemStack(item));
		});
		return entity;
	}

	private static LivingEntity createBlockDisplay(final JSONObject entry) {
		final LivingEntity entity = new LivingEntity(EntityType.BLOCK_DISPLAY);
		entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
			meta.setHasNoGravity(true);

			String id = entry.optString("id", null);
			JSONObject translation = entry.getJSONObject("translation");
			JSONObject scale = entry.getJSONObject("scale");
			JSONObject leftRotation = entry.getJSONObject("leftRotation");
			JSONObject rightRotation = entry.getJSONObject("rightRotation");
			JSONObject blockState = entry.getJSONObject("blockState");

			meta.setTranslation(new Vec(
					translation.getDouble("x"),
					translation.getDouble("y"),
					translation.getDouble("z")
			));

			meta.setScale(new Vec(
					scale.getDouble("x"),
					scale.getDouble("y"),
					scale.getDouble("z")
			));

			meta.setLeftRotation(new float[] {
					(float) leftRotation.getDouble("x"),
					(float) leftRotation.getDouble("y"),
					(float) leftRotation.getDouble("z"),
					(float) leftRotation.getDouble("w")
			});

			meta.setRightRotation(new float[] {
					(float) rightRotation.getDouble("x"),
					(float) rightRotation.getDouble("y"),
					(float) rightRotation.getDouble("z"),
					(float) rightRotation.getDouble("w")
			});

			meta.setBlockState(buildBlockState(id, blockState));
		});
		return entity;
	}

	private static LivingEntity createArmorStand(final JSONObject entry) {
		final LivingEntity entity = new LivingEntity(EntityType.ARMOR_STAND);
		entity.editEntityMeta(ArmorStandMeta.class, meta -> {
			meta.setHasNoGravity(true);
			meta.setInvisible(entry.optBoolean("invisible", false));
			meta.setSmall(entry.optBoolean("small", false));
			meta.setMarker(entry.optBoolean("marker", false));
			meta.setHasArms(entry.optBoolean("showArms", false));
			meta.setHasNoBasePlate(!entry.optBoolean("showBasePlate", true));

			JSONObject pose = entry.optJSONObject("pose");
			if (pose != null) {
				meta.setHeadRotation(readRotationVector(pose, "head"));
				meta.setBodyRotation(readRotationVector(pose, "body"));
				meta.setLeftArmRotation(readRotationVector(pose, "leftArm"));
				meta.setRightArmRotation(readRotationVector(pose, "rightArm"));
				meta.setLeftLegRotation(readRotationVector(pose, "leftLeg"));
				meta.setRightLegRotation(readRotationVector(pose, "rightLeg"));
			}
		});

		JSONObject equipment = entry.optJSONObject("equipment");
		if (equipment != null) {
			setEquipment(entity, equipment, "head", EquipmentSlot.HELMET);
			setEquipment(entity, equipment, "chest", EquipmentSlot.CHESTPLATE);
			setEquipment(entity, equipment, "legs", EquipmentSlot.LEGGINGS);
			setEquipment(entity, equipment, "feet", EquipmentSlot.BOOTS);
			setEquipment(entity, equipment, "mainhand", EquipmentSlot.MAIN_HAND);
			setEquipment(entity, equipment, "offhand", EquipmentSlot.OFF_HAND);
		}

		return entity;
	}

	private static ItemStack buildItemStack(final JSONObject item) {
		final String itemId = item.getString("id");
		Material material = Material.fromKey(itemId);
		if (material == null) {
			material = Material.AIR;
		}

		final int count = Math.max(1, item.optInt("count", 1));

		ItemStack.Builder builder = ItemStack.builder(material).amount(count);

		// snbt is only used for getting player heads currently
		final String snbt = item.optString("snbt", "");
		final String texture = extractTextureFromSnbt(snbt);
		if (texture != null && material == Material.PLAYER_HEAD) {
			builder.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texture, null)));
		}

		return builder.build();
	}

	private static Block buildBlockState(@Nullable final String id, final JSONObject blockState) {
		Block base = id == null ? Block.STONE_SLAB : Block.fromKey(id);
		if (base == null) {
			base = Block.STONE_SLAB;
		}

		for (String key : blockState.keySet()) {
			if ("id".equals(key)) {
				continue;
			}
			base = base.withProperty(key, String.valueOf(blockState.get(key)));
		}

		return base;
	}

	private static String extractTextureFromSnbt(final String snbt) {
		final Matcher matcher = TEXTURE_PATTERN.matcher(snbt);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private static Vec readRotationVector(JSONObject pose, String key) {
		JSONObject rotation = pose.optJSONObject(key);
		if (rotation == null) {
			return Vec.ZERO;
		}
		return new Vec(
			rotation.optDouble("x", 0D),
			rotation.optDouble("y", 0D),
			rotation.optDouble("z", 0D)
		);
	}

	private static void setEquipment(LivingEntity entity, JSONObject equipment, String key, EquipmentSlot slot) {
		JSONObject item = equipment.optJSONObject(key);
		if (item == null) {
			return;
		}
		entity.setEquipment(slot, buildItemStack(item));
	}

}
