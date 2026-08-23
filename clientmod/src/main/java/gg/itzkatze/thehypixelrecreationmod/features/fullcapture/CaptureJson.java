package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import gg.itzkatze.thehypixelrecreationmod.utils.ItemStackUtils;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CaptureJson {
    private static final int MAX_DEPTH = 6;
    private static final int MAX_ELEMENTS = 512;
    private static final int MAX_BYTES = 4096;
    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private CaptureJson() {
    }

    public static String legacy(Component component) {
        return component == null ? "" : StringUtility.toLegacyString(component);
    }

    public static JsonObject component(Component component) {
        JsonObject json = new JsonObject();
        if (component == null) {
            return json;
        }
        json.addProperty("legacy", StringUtility.toLegacyString(component));
        json.addProperty("plain", component.getString());
        json.add("json", componentTree(component));
        return json;
    }

    public static JsonElement componentTree(Component component) {
        RegistryAccess registries = registries();
        if (registries == null) {
            return new JsonPrimitive(StringUtility.toLegacyString(component));
        }
        try {
            return ComponentSerialization.CODEC
                    .encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), component)
                    .result()
                    .orElseGet(() -> new JsonPrimitive(StringUtility.toLegacyString(component)));
        } catch (Exception exception) {
            return new JsonPrimitive(StringUtility.toLegacyString(component));
        }
    }

    public static JsonElement item(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return JsonNull.INSTANCE;
        }

        JsonObject json = new JsonObject();
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        json.addProperty("name", StringUtility.toLegacyString(stack.getHoverName()));
        json.add("nameJson", componentTree(stack.getHoverName()));

        List<String> lore = ItemStackUtils.getLoreAsStrings(stack);
        if (!lore.isEmpty()) {
            JsonArray loreArray = new JsonArray();
            lore.forEach(loreArray::add);
            json.add("lore", loreArray);
        }

        ItemStackUtils.HeadTextureProperty texture = ItemStackUtils.getPlayerHeadTextureProperty(stack);
        if (texture != null) {
            json.addProperty("headTexture", texture.valueBase64());
        }

        JsonObject components = new JsonObject();
        for (TypedDataComponent<?> component : stack.getComponents()) {
            Identifier key = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component.type());
            components.add(key == null ? component.type().toString() : key.toString(), componentValue(component));
        }
        json.add("components", components);
        return json;
    }

    private static JsonElement componentValue(TypedDataComponent<?> component) {
        try {
            return component.encodeValue(JsonOps.INSTANCE)
                    .result()
                    .orElseGet(() -> new JsonPrimitive(String.valueOf(component.value())));
        } catch (Exception exception) {
            return new JsonPrimitive(String.valueOf(component.value()));
        }
    }

    public static JsonElement encode(Object value) {
        return encode(value, 0, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    public static JsonObject fields(Object value) {
        JsonElement encoded = encode(value);
        if (encoded instanceof JsonObject object) {
            return object;
        }
        JsonObject wrapper = new JsonObject();
        wrapper.add("value", encoded);
        return wrapper;
    }

    private static JsonElement encode(Object value, int depth, Set<Object> visited) {
        switch (value) {
            case null -> {
                return JsonNull.INSTANCE;
            }
            case String string -> {
                return new JsonPrimitive(string);
            }
            case Number number -> {
                return new JsonPrimitive(number);
            }
            case Boolean bool -> {
                return new JsonPrimitive(bool);
            }
            case Character character -> {
                return new JsonPrimitive(character);
            }
            case Enum<?> constant -> {
                return new JsonPrimitive(constant.name());
            }
            case UUID uuid -> {
                return new JsonPrimitive(uuid.toString());
            }
            case Identifier identifier -> {
                return new JsonPrimitive(identifier.toString());
            }
            case Component component -> {
                return component(component);
            }
            case ItemStack stack -> {
                return item(stack);
            }
            case Optional<?> optional -> {
                return optional.isEmpty() ? JsonNull.INSTANCE : encode(optional.get(), depth, visited);
            }
            case OptionalInt optional -> {
                return optional.isEmpty() ? JsonNull.INSTANCE : new JsonPrimitive(optional.getAsInt());
            }
            case ResourceKey<?> key -> {
                return new JsonPrimitive(key.identifier().toString());
            }
            case Holder<?> holder -> {
                return holder.unwrapKey()
                        .<JsonElement>map(key -> new JsonPrimitive(key.identifier().toString()))
                        .orElseGet(() -> encode(holder.value(), depth + 1, visited));
            }
            case Tag tag -> {
                return nbt(tag);
            }
            case Vec3 position -> {
                return vector(position.x(), position.y(), position.z());
            }
            case Vec3i position -> {
                return vector(position.getX(), position.getY(), position.getZ());
            }
            case ChunkPos position -> {
                JsonObject json = new JsonObject();
                json.addProperty("x", position.x());
                json.addProperty("z", position.z());
                return json;
            }
            case Vector3fc vector -> {
                return vector(vector.x(), vector.y(), vector.z());
            }
            case Quaternionfc quaternion -> {
                JsonArray array = new JsonArray();
                array.add(quaternion.x());
                array.add(quaternion.y());
                array.add(quaternion.z());
                array.add(quaternion.w());
                return array;
            }
            case GameProfile profile -> {
                JsonObject json = new JsonObject();
                json.addProperty("name", profile.name());
                json.addProperty("id", String.valueOf(profile.id()));
                return json;
            }
            case byte[] bytes -> {
                JsonObject json = new JsonObject();
                json.addProperty("bytes", bytes.length);
                json.addProperty("base64", Base64.getEncoder()
                        .encodeToString(bytes.length > MAX_BYTES ? java.util.Arrays.copyOf(bytes, MAX_BYTES) : bytes));
                return json;
            }
            default -> {
            }
        }

        if (isOpaque(value)) {
            return new JsonPrimitive(value.getClass().getSimpleName());
        }
        if (depth >= MAX_DEPTH || !visited.add(value)) {
            return new JsonPrimitive(String.valueOf(value));
        }

        if (value.getClass().isArray()) {
            JsonArray array = new JsonArray();
            int length = Math.min(Array.getLength(value), MAX_ELEMENTS);
            for (int index = 0; index < length; index++) {
                array.add(encode(Array.get(value, index), depth + 1, visited));
            }
            return array;
        }
        if (value instanceof Iterable<?> iterable) {
            JsonArray array = new JsonArray();
            for (Object element : iterable) {
                if (array.size() >= MAX_ELEMENTS) {
                    break;
                }
                array.add(encode(element, depth + 1, visited));
            }
            return array;
        }
        if (value instanceof Map<?, ?> map) {
            JsonObject json = new JsonObject();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (json.size() >= MAX_ELEMENTS) {
                    break;
                }
                json.add(String.valueOf(entry.getKey()), encode(entry.getValue(), depth + 1, visited));
            }
            return json;
        }

        JsonObject json = new JsonObject();
        for (Field field : declaredFields(value.getClass())) {
            Object read = read(field, value);
            if (read == null) {
                continue;
            }
            json.add(field.getName(), encode(read, depth + 1, visited));
        }
        return json.isEmpty() ? new JsonPrimitive(String.valueOf(value)) : json;
    }

    private static JsonElement nbt(Tag tag) {
        try {
            return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        } catch (Exception exception) {
            return new JsonPrimitive(tag.toString());
        }
    }

    private static JsonObject vector(double x, double y, double z) {
        JsonObject json = new JsonObject();
        json.addProperty("x", round(x));
        json.addProperty("y", round(y));
        json.addProperty("z", round(z));
        return json;
    }

    public static double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private static boolean isOpaque(Object value) {
        return value instanceof Minecraft
                || value instanceof RegistryAccess
                || value instanceof net.minecraft.world.level.Level
                || value instanceof net.minecraft.world.entity.Entity
                || value instanceof net.minecraft.core.Registry<?>
                || value instanceof io.netty.buffer.ByteBuf
                || value instanceof java.util.concurrent.Executor
                || value instanceof java.lang.invoke.MethodHandle
                || value.getClass().getName().startsWith("io.netty.");
    }

    private static List<Field> declaredFields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, key -> {
            List<Field> fields = new ArrayList<>();
            for (Class<?> current = key; current != null && current != Object.class; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    try {
                        field.setAccessible(true);
                    } catch (RuntimeException exception) {
                        continue;
                    }
                    fields.add(field);
                }
            }
            return List.copyOf(fields);
        });
    }

    private static Object read(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static RegistryAccess registries() {
        Minecraft client = Minecraft.getInstance();
        return client.level == null ? null : client.level.registryAccess();
    }
}
