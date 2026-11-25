package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class RegisteredArmorSet implements ArmorSet {
    private final ArmorSetRegistry registry;
    private final List<ArmorSetEffect> effects;

    public RegisteredArmorSet(ArmorSetRegistry registry, List<ArmorSetEffect> effects) {
        this.registry = registry;
        this.effects = List.copyOf(effects);
    }

    @Override
    public ArmorSetRegistry getRegistry() {
        return registry;
    }

    @Override
    public String getName() {
        return effects.isEmpty() ? registry.getDisplayName() : effects.getFirst().getName();
    }

    @Override
    public ArrayList<String> getDescription() {
        return effects.isEmpty() ? new ArrayList<>() : new ArrayList<>(effects.getFirst().getDescription(ArmorSetContext.preview(registry, Set.of())));
    }

    @Override
    public List<ArmorSetEffect> getEffects() {
        return effects;
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player) {
        ArmorSetContext context = ArmorSetContext.of(registry, player);
        ItemStatistics statistics = ItemStatistics.empty();
        for (ArmorSetEffect effect : effects) {
            if (effect.isActive(context)) {
                statistics = ItemStatistics.add(statistics, effect.getStatistics(context));
            }
        }
        return statistics;
    }
}
