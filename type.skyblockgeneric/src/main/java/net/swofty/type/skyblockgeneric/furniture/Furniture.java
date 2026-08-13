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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Furniture {

	private static final File FURNITURE_DIR = new File("./configuration/skyblock/furniture");
	private static final Pattern TEXTURE_PATTERN = Pattern.compile("name:\\\"textures\\\",value:\\\"([^\\\"]+)\\\"");
	private static volatile Map<String, List<Map<String, Object>>> templates = Map.of();

	public static synchronized void preload() {
		if (!templates.isEmpty()) return;
		if (!FURNITURE_DIR.isDirectory()) {
			throw new IllegalStateException("Missing furniture template directory: " + FURNITURE_DIR.getPath());
		}

		File[] files = FURNITURE_DIR.listFiles((directory, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
		if (files == null) {
			throw new IllegalStateException("Unable to read furniture template directory: " + FURNITURE_DIR.getPath());
		}

		Map<String, List<Map<String, Object>>> loaded = new HashMap<>();
		for (File file : files) {
			try {
				JSONArray entries = new JSONArray(Files.readString(file.toPath(), StandardCharsets.UTF_8));
				List<Map<String, Object>> parsedEntries = new ArrayList<>(entries.length());
				for (int i = 0; i < entries.length(); i++) {
					Object value = entries.get(i);
					if (!(value instanceof JSONObject object)) {
						throw new IllegalArgumentException("Entry " + i + " is not an object");
					}
					Map<String, Object> entry = immutableObject(object);
					validateEntry(entry, i);
					parsedEntries.add(entry);
				}
				String name = file.getName().substring(0, file.getName().length() - ".json".length())
						.toLowerCase(Locale.ROOT);
				if (loaded.put(name, List.copyOf(parsedEntries)) != null) {
					throw new IllegalArgumentException("Duplicate furniture template name: " + name);
				}
			} catch (Exception exception) {
				throw new IllegalStateException("Failed to preload furniture template '" + file.getName() + "'", exception);
			}
		}

		if (loaded.isEmpty()) {
			throw new IllegalStateException("No furniture templates found in " + FURNITURE_DIR.getPath());
		}
		templates = Map.copyOf(loaded);
	}

	public static List<LivingEntity> load(String furnitureName) {
		return load(furnitureName, new Pos(0, 0, 0));
	}

	public static List<LivingEntity> load(String furnitureName, Pos offset) {
        return load(furnitureName, HypixelConst.getInstanceContainer(), offset);
    }

    public static List<LivingEntity> load(String furnitureName, Instance instance, Pos offset) {
        return load(furnitureName, instance, offset, 0f);
    }

    public static List<LivingEntity> load(String furnitureName, Instance instance, Pos offset, float rotationYaw) {
        final List<LivingEntity> spawned = new ArrayList<>();
		try {
			if (instance == null) {
				throw new IllegalStateException("SkyBlock instance is not initialized");
			}

			final String templateName = furnitureName.toLowerCase(Locale.ROOT);
			final List<Map<String, Object>> entries = templates.get(templateName);
			if (entries == null) {
				if (templates.isEmpty()) {
					throw new IllegalStateException("Furniture templates have not been preloaded");
				}
				throw new IllegalArgumentException("Furniture template not found: " + furnitureName);
			}

			for (Map<String, Object> entry : entries) {
				String type = requiredString(entry, "type");

				if ("minecraft:item_display".equals(type)) {
					LivingEntity entity = createItemDisplay(entry);
                    spawned.add(entity);
                    spawnEntity(entity, entry, offset, instance, rotationYaw);
					continue;
				}

				if ("minecraft:block_display".equals(type)) {
					LivingEntity entity = createBlockDisplay(entry);
                    spawned.add(entity);
                    spawnEntity(entity, entry, offset, instance, rotationYaw);
                    continue;
                }

                if ("minecraft:armor_stand".equals(type)) {
                    LivingEntity entity = createArmorStand(entry);
					spawned.add(entity);
                    spawnEntity(entity, entry, offset, instance, rotationYaw);
				}
			}

			return spawned;
		} catch (Exception exception) {
            remove(spawned);
			throw new IllegalStateException("Failed to load furniture '" + furnitureName + "'", exception);
		}
	}

    public static void remove(List<? extends LivingEntity> entities) {
        entities.forEach(LivingEntity::remove);
    }

    public static float facingPlayerYaw(float playerYaw) {
        return playerYaw + 180f;
    }

	private static void spawnEntity(LivingEntity entity, Map<String, Object> entry, Pos offset, Instance instance, float rotationYaw) {
		final Map<String, Object> position = requiredObject(entry, "position");
		final Map<String, Object> rotation = optionalObject(entry, "rotation");
        final double radians = Math.toRadians(rotationYaw);
		final double localX = requiredNumber(position, "x");
		final double localZ = requiredNumber(position, "z");

        final double x = localX * Math.cos(radians) - localZ * Math.sin(radians) + offset.x();
		final double y = requiredNumber(position, "y") + offset.y();
        final double z = localX * Math.sin(radians) + localZ * Math.cos(radians) + offset.z();

		final float yaw = (rotation == null ? 0f : (float) optionalNumber(rotation, "yaw", 0d)) + rotationYaw;
		final float pitch = rotation == null ? 0f : (float) optionalNumber(rotation, "pitch", 0d);

		entity.setInstance(instance, new Pos(x, y, z, yaw, pitch));
	}

	private static LivingEntity createItemDisplay(final Map<String, Object> entry) {
		final LivingEntity entity = new LivingEntity(EntityType.ITEM_DISPLAY);
		entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
			meta.setHasNoGravity(true);

			Map<String, Object> translation = requiredObject(entry, "translation");
			Map<String, Object> scale = requiredObject(entry, "scale");
			Map<String, Object> leftRotation = requiredObject(entry, "leftRotation");
			Map<String, Object> rightRotation = requiredObject(entry, "rightRotation");
			Map<String, Object> item = requiredObject(entry, "item");

			meta.setTranslation(new Vec(
					requiredNumber(translation, "x"),
					requiredNumber(translation, "y"),
					requiredNumber(translation, "z")
			));

			meta.setScale(new Vec(
					requiredNumber(scale, "x"),
					requiredNumber(scale, "y"),
					requiredNumber(scale, "z")
			));

			meta.setLeftRotation(new float[] {
					(float) requiredNumber(leftRotation, "x"),
					(float) requiredNumber(leftRotation, "y"),
					(float) requiredNumber(leftRotation, "z"),
					(float) requiredNumber(leftRotation, "w")
			});

			meta.setRightRotation(new float[] {
					(float) requiredNumber(rightRotation, "x"),
					(float) requiredNumber(rightRotation, "y"),
					(float) requiredNumber(rightRotation, "z"),
					(float) requiredNumber(rightRotation, "w")
			});

			meta.setItemStack(buildItemStack(item));
		});
		return entity;
	}

	private static LivingEntity createBlockDisplay(final Map<String, Object> entry) {
		final LivingEntity entity = new LivingEntity(EntityType.BLOCK_DISPLAY);
		entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
			meta.setHasNoGravity(true);

			String id = optionalString(entry, "id");
			Map<String, Object> translation = requiredObject(entry, "translation");
			Map<String, Object> scale = requiredObject(entry, "scale");
			Map<String, Object> leftRotation = requiredObject(entry, "leftRotation");
			Map<String, Object> rightRotation = requiredObject(entry, "rightRotation");
			Map<String, Object> blockState = requiredObject(entry, "blockState");

			meta.setTranslation(new Vec(
					requiredNumber(translation, "x"),
					requiredNumber(translation, "y"),
					requiredNumber(translation, "z")
			));

			meta.setScale(new Vec(
					requiredNumber(scale, "x"),
					requiredNumber(scale, "y"),
					requiredNumber(scale, "z")
			));

			meta.setLeftRotation(new float[] {
					(float) requiredNumber(leftRotation, "x"),
					(float) requiredNumber(leftRotation, "y"),
					(float) requiredNumber(leftRotation, "z"),
					(float) requiredNumber(leftRotation, "w")
			});

			meta.setRightRotation(new float[] {
					(float) requiredNumber(rightRotation, "x"),
					(float) requiredNumber(rightRotation, "y"),
					(float) requiredNumber(rightRotation, "z"),
					(float) requiredNumber(rightRotation, "w")
			});

			meta.setBlockState(buildBlockState(id, blockState));
		});
		return entity;
	}

	private static LivingEntity createArmorStand(final Map<String, Object> entry) {
        final LivingEntity entity = new LivingEntity(EntityType.ARMOR_STAND);
        entity.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(optionalBoolean(entry, "invisible", false));
            meta.setSmall(optionalBoolean(entry, "small", false));
            meta.setMarker(optionalBoolean(entry, "marker", false));
            meta.setHasArms(optionalBoolean(entry, "showArms", false));
            meta.setHasNoBasePlate(!optionalBoolean(entry, "showBasePlate", true));
            meta.setHasNoGravity(true);

            Map<String, Object> pose = optionalObject(entry, "pose");
            if (pose != null) {
                setPose(pose, "head", meta::setHeadRotation);
                setPose(pose, "body", meta::setBodyRotation);
                setPose(pose, "leftArm", meta::setLeftArmRotation);
                setPose(pose, "rightArm", meta::setRightArmRotation);
                setPose(pose, "leftLeg", meta::setLeftLegRotation);
                setPose(pose, "rightLeg", meta::setRightLegRotation);
            }
        });

        Map<String, Object> equipment = optionalObject(entry, "equipment");
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

    private static void setPose(Map<String, Object> pose, String key, java.util.function.Consumer<Vec> setter) {
        Map<String, Object> rotation = optionalObject(pose, key);
        if (rotation == null) return;
        setter.accept(new Vec(
                optionalNumber(rotation, "x", 0d),
                optionalNumber(rotation, "y", 0d),
                optionalNumber(rotation, "z", 0d)
        ));
    }

    private static void setEquipment(LivingEntity entity, Map<String, Object> equipment, String key, EquipmentSlot slot) {
        Map<String, Object> item = optionalObject(equipment, key);
        if (item != null) {
            entity.setEquipment(slot, buildItemStack(item));
        }
    }

	private static ItemStack buildItemStack(final Map<String, Object> item) {
		final String itemId = requiredString(item, "id");
		Material material = Material.fromKey(itemId);
		if (material == null) {
			material = Material.AIR;
		}

		final int count = Math.max(1, optionalInteger(item, "count", 1));

		ItemStack.Builder builder = ItemStack.builder(material).amount(count);

		final String snbt = optionalString(item, "snbt", "");
		final String texture = extractTextureFromSnbt(snbt);
		if (texture != null && material == Material.PLAYER_HEAD) {
			builder.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texture, null)));
		}
        if (snbt.contains("minecraft:enchantments")) {
            builder.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }

		return builder.build();
	}

	private static Block buildBlockState(@Nullable final String id, final Map<String, Object> blockState) {
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

	private static Map<String, Object> immutableObject(JSONObject object) {
		Map<String, Object> copy = new HashMap<>();
		for (String key : object.keySet()) {
			copy.put(key, immutableValue(object.get(key)));
		}
		return Collections.unmodifiableMap(copy);
	}

	private static Object immutableValue(Object value) {
		if (value == JSONObject.NULL) return null;
		if (value instanceof JSONObject object) return immutableObject(object);
		if (value instanceof JSONArray array) {
			List<Object> values = new ArrayList<>(array.length());
			for (int i = 0; i < array.length(); i++) {
				values.add(immutableValue(array.get(i)));
			}
			return Collections.unmodifiableList(values);
		}
		return value;
	}

	private static void validateEntry(Map<String, Object> entry, int index) {
		String type = requiredString(entry, "type");
		requireVector(entry, "position", "x", "y", "z");
		switch (type) {
            case "minecraft:item_display" -> {
                requireVector(entry, "translation", "x", "y", "z");
                requireVector(entry, "scale", "x", "y", "z");
                requireVector(entry, "leftRotation", "x", "y", "z", "w");
                requireVector(entry, "rightRotation", "x", "y", "z", "w");
                validateItem(requiredObject(entry, "item"));
            }
            case "minecraft:block_display" -> {
                requireVector(entry, "translation", "x", "y", "z");
                requireVector(entry, "scale", "x", "y", "z");
                requireVector(entry, "leftRotation", "x", "y", "z", "w");
                requireVector(entry, "rightRotation", "x", "y", "z", "w");
                Map<String, Object> blockState = requiredObject(entry, "blockState");
                String id = optionalString(entry, "id");
                if (id != null && Block.fromKey(id) == null) {
                    throw new IllegalArgumentException("Unknown block state id: " + id);
                }
                try {
                    buildBlockState(id, blockState);
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException("Invalid block state", exception);
                }
            }
            case "minecraft:armor_stand" -> {
                Map<String, Object> equipment = optionalObject(entry, "equipment");
                if (equipment != null) {
                    for (Object value : equipment.values()) {
                        if (value != null) validateItem(asObject(value, "equipment item"));
                    }
                }
            }
			default -> throw new IllegalArgumentException("Unknown furniture entity type at entry " + index + ": " + type);
        }
    }

    private static void validateItem(Map<String, Object> item) {
        String id = requiredString(item, "id");
        if (Material.fromKey(id) == null) {
            throw new IllegalArgumentException("Unknown item material: " + id);
        }
        Object count = item.get("count");
        if (count != null && (!(count instanceof Number number) || number.intValue() < 1)) {
            throw new IllegalArgumentException("Item count must be a positive number");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value, String name) {
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        throw new IllegalArgumentException("Invalid " + name);
    }

	private static void requireVector(Map<String, Object> parent, String key, String... components) {
		Map<String, Object> vector = requiredObject(parent, key);
		for (String component : components) {
			requiredNumber(vector, component);
		}
	}

	private static String requiredString(Map<String, Object> object, String key) {
		Object value = object.get(key);
		if (value instanceof String string && !string.isBlank()) return string;
		throw new IllegalArgumentException("Missing or invalid string field '" + key + "'");
	}

	private static String optionalString(Map<String, Object> object, String key) {
		return optionalString(object, key, null);
	}

	private static String optionalString(Map<String, Object> object, String key, String defaultValue) {
		Object value = object.get(key);
		return value instanceof String string ? string : defaultValue;
	}

	private static Map<String, Object> requiredObject(Map<String, Object> object, String key) {
		Map<String, Object> value = optionalObject(object, key);
		if (value != null) return value;
		throw new IllegalArgumentException("Missing or invalid object field '" + key + "'");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> optionalObject(Map<String, Object> object, String key) {
		Object value = object.get(key);
		return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
	}

	private static double requiredNumber(Map<String, Object> object, String key) {
		Object value = object.get(key);
		if (value instanceof Number number) return number.doubleValue();
		throw new IllegalArgumentException("Missing or invalid number field '" + key + "'");
	}

	private static double optionalNumber(Map<String, Object> object, String key, double defaultValue) {
		Object value = object.get(key);
		return value instanceof Number number ? number.doubleValue() : defaultValue;
	}

	private static int optionalInteger(Map<String, Object> object, String key, int defaultValue) {
		Object value = object.get(key);
		return value instanceof Number number ? number.intValue() : defaultValue;
	}

	private static boolean optionalBoolean(Map<String, Object> object, String key, boolean defaultValue) {
		Object value = object.get(key);
		return value instanceof Boolean booleanValue ? booleanValue : defaultValue;
	}

}
