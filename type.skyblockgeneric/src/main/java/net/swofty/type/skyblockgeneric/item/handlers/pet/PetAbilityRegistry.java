package net.swofty.type.skyblockgeneric.item.handlers.pet;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class PetAbilityRegistry {
    private static final Map<Class<? extends PetAbility>, AbilityDescriptor> BY_ABILITY = new HashMap<>();
    private static final Map<PetHandler, List<AbilityDescriptor>> ABILITIES = load();

    private PetAbilityRegistry() {
    }

    public static List<PetAbility> getAbilities(SkyBlockItem item) {
        PetComponent component = item.getComponent(PetComponent.class);
        PetHandler petHandler = PetHandler.valueOf(component.getHandlerId());
        Rarity rarity = item.getAttributeHandler().getRarity();

        return ABILITIES.getOrDefault(petHandler, List.of())
                .stream()
                .filter(descriptor -> rarity.isAtLeast(descriptor.minimumRarity))
                .filter(descriptor -> rarity.isAtMost(descriptor.maximumRarity))
                .map(descriptor -> instantiate(descriptor.type))
                .toList();
    }

    public static @Nullable String notImplementedLine(PetAbility ability) {
        AbilityDescriptor descriptor = BY_ABILITY.get(ability.getClass());
        if (descriptor == null || descriptor.implemented) return null;
        String reason = descriptor.notImplementedReason;
        return "<c>⚠ <l>NOT IMPLEMENTED<r><c>" + (reason.isEmpty() ? "" : " — " + reason);
    }

    public static void invoke(PetAbility ability, PetEvent event) {
        AbilityDescriptor descriptor = BY_ABILITY.get(ability.getClass());
        Map<Class<? extends PetEvent>, List<Method>> handlers = descriptor != null
                ? descriptor.handlers
                : buildHandlers(ability.getClass());
        for (Class<?> eventType = event.getClass();
             eventType != null && PetEvent.class.isAssignableFrom(eventType);
             eventType = eventType.getSuperclass()) {
            for (Method handler : handlers.getOrDefault(eventType.asSubclass(PetEvent.class), List.of())) {
                try {
                    handler.invoke(ability, event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Failed to invoke " + handler + " for " + event.getClass().getSimpleName(), e);
                }
            }
        }
    }

    private static Map<PetHandler, List<AbilityDescriptor>> load() {
        Map<PetHandler, List<AbilityDescriptor>> registry = new EnumMap<>(PetHandler.class);
        for (Class<?> clazz : new Reflections("net.swofty.type.skyblockgeneric.item.handlers.pet.abilities")
                .getTypesAnnotatedWith(PetAbilityRegistration.class)) {
            PetAbilityRegistration meta = clazz.getAnnotation(PetAbilityRegistration.class);
            Class<? extends PetAbility> type = clazz.asSubclass(PetAbility.class);
            AbilityDescriptor descriptor = new AbilityDescriptor(type, instantiate(clazz),
                    meta.minimumRarity(), meta.maximumRarity(), meta.order(), meta.implemented(), meta.notImplementedReason(),
                    buildHandlers(type));
            registry.computeIfAbsent(meta.pet(), _ -> new ArrayList<>()).add(descriptor);
            BY_ABILITY.put(type, descriptor);
        }
        registry.values().forEach(list -> list.sort(
                Comparator.comparingInt((AbilityDescriptor d) -> d.minimumRarity.ordinal())
                        .thenComparingInt(AbilityDescriptor::order)));
        return Map.copyOf(registry);
    }

    private static PetAbility instantiate(Class<?> clazz) {
        try {
            return (PetAbility) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Pet ability " + clazz.getName() + " must have a public no-arg constructor", e);
        }
    }

    private static Map<Class<? extends PetEvent>, List<Method>> buildHandlers(Class<? extends PetAbility> clazz) {
        Map<Class<? extends PetEvent>, List<Method>> handlers = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
            PetEventHandler meta = method.getAnnotation(PetEventHandler.class);
            if (meta == null) continue;
            if (!Modifier.isPublic(method.getModifiers()) || method.getReturnType() != void.class
                    || method.getParameterCount() != 1
                    || !PetEvent.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalStateException("@PetEventHandler " + method
                        + " must be a public void method with exactly one PetEvent parameter");
            }
            handlers.computeIfAbsent(method.getParameterTypes()[0].asSubclass(PetEvent.class), _ -> new ArrayList<>())
                    .add(method);
        }
        handlers.values().forEach(list -> list.sort(
                Comparator.comparingInt((Method m) -> m.getAnnotation(PetEventHandler.class).order())
                        .thenComparing(Method::getName)));
        return Map.copyOf(handlers);
    }

    private record AbilityDescriptor(Class<? extends PetAbility> type, PetAbility prototype, Rarity minimumRarity,
                                     Rarity maximumRarity, int order, boolean implemented, String notImplementedReason,
                                     Map<Class<? extends PetEvent>, List<Method>> handlers) {
    }
}
