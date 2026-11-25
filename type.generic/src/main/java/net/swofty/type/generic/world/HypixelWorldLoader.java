package net.swofty.type.generic.world;

import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarReader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWorldAccess;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.display.AbstractDisplayMeta;
import net.minestom.server.entity.metadata.display.BlockDisplayMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.biome.Biome;
import net.swofty.commons.CustomWorlds;
import net.swofty.type.generic.HypixelTypeLoader;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HypixelWorldLoader {
    public static final java.util.function.Predicate<net.minestom.server.entity.Player> LOADED_ONLY =
            player -> player.getInstance() != null
                    && net.swofty.type.generic.data.DataHandler.findUser(player.getUuid()).isPresent();

    private HypixelWorldLoader() {
    }

    public static void refreshViewers(net.minestom.server.instance.Instance instance) {
        if (instance == null) {
            return;
        }
        for (Entity entity : instance.getEntities()) {
            if (!(entity instanceof net.minestom.server.entity.Player)) {
                entity.updateViewableRule();
            }
        }
    }

    public static SharedInstance load(HypixelTypeLoader loader, InstanceManager instanceManager) throws IOException {
        CustomWorlds world = loader.getMainInstance();
        InstanceContainer source = loader.getDimensionType() == null
                ? instanceManager.createInstanceContainer()
                : instanceManager.createInstanceContainer(loader.getDimensionType());
        return loadFrom(world.getPath(), source, instanceManager);
    }

    public static SharedInstance load(final @NotNull CustomWorlds world, final @NotNull InstanceManager manager) throws IOException {
        return loadFrom(world.getPath(), manager.createInstanceContainer(), manager);
    }

    /**
     * Loads any polar file into the given container, applying its custom biomes and display
     * entities the same way the main world does.
     */
    public static SharedInstance loadFrom(Path path, InstanceContainer source,
                                          InstanceManager instanceManager) throws IOException {
        PolarWorld polarWorld = PolarReader.read(Files.readAllBytes(path));

        CompoundBinaryTag userData = readUserData(polarWorld.userData());
        registerCustomBiomes(userData);
        source.setChunkLoader(new PolarLoader(path, polarWorld).setWorldAccess(new PolarWorldAccess() {
            @Override
            public int getBiomeId(@NotNull String name) {
                int id = MinecraftServer.getBiomeRegistry().getId(RegistryKey.of(name));
                if (id != -1) {
                    return id;
                }
                Logger.info("Missing biome " + name + ", falling back to plains");
                return MinecraftServer.getBiomeRegistry().getId(Biome.PLAINS);
            }
        }));

        SharedInstance instance = instanceManager.createSharedInstance(source);
        loadDisplays(userData, instance);
        return instance;
    }

    private static CompoundBinaryTag readUserData(byte[] userData) throws IOException {
        if (userData == null || userData.length == 0) {
            return null;
        }
        CompoundBinaryTag root = BinaryTagIO.unlimitedReader()
                .read(new ByteArrayInputStream(userData), BinaryTagIO.Compression.GZIP);
        if (!"hypixel:custom_biomes".equals(root.getString("format", ""))) {
            return null;
        }
        if (root.getInt("version", 0) != 1) {
            Logger.warn("Ignoring unsupported polar user data version {}", root.getInt("version", 0));
            return null;
        }
        return root;
    }

    private static void registerCustomBiomes(CompoundBinaryTag root) {
        if (root == null) {
            return;
        }
        int registered = 0;
        for (BinaryTag tag : root.getList("custom_biomes", BinaryTagTypes.COMPOUND)) {
            CompoundBinaryTag data = (CompoundBinaryTag) tag;
            String id = data.getString("id", "");
            if (id.isBlank()) {
                Logger.warn("Skipping custom biome entry without id");
                continue;
            }
            try {
                Key key = Key.key(id);
                if (MinecraftServer.getBiomeRegistry().getId(RegistryKey.of(id)) != -1) {
                    continue;
                }
                if (!(data.get("definition") instanceof CompoundBinaryTag definition)) {
                    Logger.warn("Skipping custom biome {} without compound definition", id);
                    continue;
                }
                Biome biome = Biome.REGISTRY_CODEC.decode(Transcoder.NBT, definition)
                        .orElseThrow("Failed to decode custom biome " + id);
                MinecraftServer.getBiomeRegistry().register(key, biome);
                registered++;
            } catch (RuntimeException exception) {
                Logger.warn(exception, "Skipping custom biome {}", id);
            }
        }
        if (registered > 0) {
            Logger.info("Registered {} custom biomes from polar user data", registered);
        }
    }

    private static void loadDisplays(CompoundBinaryTag root, SharedInstance instance) {
        if (root == null) {
            return;
        }
        int loaded = 0;
        for (BinaryTag tag : root.getList("block_displays", BinaryTagTypes.COMPOUND)) {
            try {
                if (loadDisplay((CompoundBinaryTag) tag, instance)) {
                    loaded++;
                }
            } catch (RuntimeException exception) {
                Logger.warn(exception, "Skipping invalid display entity in polar user data");
            }
        }
        if (loaded > 0) {
            Logger.info("Loaded {} display entities from polar user data", loaded);
        }
    }

    private static boolean loadDisplay(CompoundBinaryTag data, SharedInstance instance) {
        String id = data.getString("id", "");
        Entity entity;
        if ("minecraft:block_display".equals(id)) {
            entity = new Entity(EntityType.BLOCK_DISPLAY);
            entity.editEntityMeta(BlockDisplayMeta.class, meta -> {
                applyDisplayMeta(data, meta);
                if (data.get("block_state") instanceof CompoundBinaryTag blockState) {
                    Block block = Block.STATE_STRUCT_CODEC.decode(Transcoder.NBT, blockState)
                            .orElseThrow("Invalid block display state");
                    meta.setBlockState(block);
                }
            });
        } else if ("minecraft:item_display".equals(id)) {
            entity = new Entity(EntityType.ITEM_DISPLAY);
            entity.editEntityMeta(ItemDisplayMeta.class, meta -> {
                applyDisplayMeta(data, meta);
                if (data.get("item") instanceof CompoundBinaryTag item) {
                    meta.setItemStack(ItemStack.fromItemNBT(item, MinecraftServer.getRegistries()));
                }
                String context = data.getString("item_display", "none");
                meta.setDisplayContext(switch (context) {
                    case "thirdperson_lefthand" -> ItemDisplayMeta.DisplayContext.THIRDPERSON_LEFT_HAND;
                    case "thirdperson_righthand" -> ItemDisplayMeta.DisplayContext.THIRDPERSON_RIGHT_HAND;
                    case "firstperson_lefthand" -> ItemDisplayMeta.DisplayContext.FIRSTPERSON_LEFT_HAND;
                    case "firstperson_righthand" -> ItemDisplayMeta.DisplayContext.FIRSTPERSON_RIGHT_HAND;
                    case "head" -> ItemDisplayMeta.DisplayContext.HEAD;
                    case "gui" -> ItemDisplayMeta.DisplayContext.GUI;
                    case "ground" -> ItemDisplayMeta.DisplayContext.GROUND;
                    case "fixed" -> ItemDisplayMeta.DisplayContext.FIXED;
                    case "on_shelf" -> ItemDisplayMeta.DisplayContext.ON_SHELF;
                    default -> ItemDisplayMeta.DisplayContext.NONE;
                });
            });
        } else {
            return false;
        }

        ListBinaryTag position = data.getList("Pos");
        ListBinaryTag rotation = data.getList("Rotation");
        entity.setNoGravity(true);
        entity.setHasPhysics(false);
        entity.updateViewableRule(LOADED_ONLY);
        entity.setInstance(instance, new Pos(
                number(position, 0, 0), number(position, 1, 0), number(position, 2, 0),
                (float) number(rotation, 0, 0), (float) number(rotation, 1, 0)));
        return true;
    }

    private static void applyDisplayMeta(CompoundBinaryTag data, AbstractDisplayMeta meta) {
        //meta.setHasNoGravity(data.getByte("NoGravity", (byte) 0) != 0);
        meta.setHasNoGravity(true);
        meta.setTransformationInterpolationStartDelta(data.getInt("start_interpolation", 0));
        meta.setTransformationInterpolationDuration(data.getInt("interpolation_duration", 0));
        meta.setPosRotInterpolationDuration(data.getInt("teleport_duration", 0));
        meta.setViewRange(data.getFloat("view_range", 1f));
        meta.setShadowRadius(data.getFloat("shadow_radius", 0f));
        meta.setShadowStrength(data.getFloat("shadow_strength", 1f));
        meta.setWidth(data.getFloat("width", 0f));
        meta.setHeight(data.getFloat("height", 0f));
        meta.setGlowColorOverride(data.getInt("glow_color_override", -1));
        meta.setBillboardRenderConstraints(switch (data.getString("billboard", "fixed")) {
            case "vertical" -> AbstractDisplayMeta.BillboardConstraints.VERTICAL;
            case "horizontal" -> AbstractDisplayMeta.BillboardConstraints.HORIZONTAL;
            case "center" -> AbstractDisplayMeta.BillboardConstraints.CENTER;
            default -> AbstractDisplayMeta.BillboardConstraints.FIXED;
        });
        if (data.get("brightness") instanceof CompoundBinaryTag brightness) {
            meta.setBrightness(brightness.getInt("block", 0), brightness.getInt("sky", 0));
        }
        if (data.get("transformation") instanceof CompoundBinaryTag transformation) {
            meta.setTranslation(vector(transformation.getList("translation"), 0));
            meta.setScale(vector(transformation.getList("scale"), 1));
            meta.setLeftRotation(quaternion(transformation.getList("left_rotation")));
            meta.setRightRotation(quaternion(transformation.getList("right_rotation")));
        }
    }

    private static Vec vector(ListBinaryTag values, double fallback) {
        return new Vec(number(values, 0, fallback), number(values, 1, fallback), number(values, 2, fallback));
    }

    private static float[] quaternion(ListBinaryTag values) {
        return new float[]{(float) number(values, 0, 0), (float) number(values, 1, 0),
                (float) number(values, 2, 0), (float) number(values, 3, 1)};
    }

    private static double number(ListBinaryTag values, int index, double fallback) {
        if (index >= values.size() || !(values.get(index) instanceof NumberBinaryTag number)) {
            return fallback;
        }
        return number.doubleValue();
    }
}
