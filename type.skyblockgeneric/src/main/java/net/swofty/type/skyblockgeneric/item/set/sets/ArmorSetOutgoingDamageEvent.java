package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.ValueUpdateEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetEffectDispatcher;

public final class ArmorSetOutgoingDamageEvent extends SkyBlockValueEvent {
    @Override
    public Class<? extends ValueUpdateEvent> getValueEvent() {
        return PlayerDamageMobValueUpdateEvent.class;
    }

    @Override
    public void run(ValueUpdateEvent valueEvent) {
        PlayerDamageMobValueUpdateEvent event = (PlayerDamageMobValueUpdateEvent) valueEvent;
        float damage = ((Number) event.getValue()).floatValue();
        event.setValue(ArmorSetEffectDispatcher.modifyOutgoingDamage(event.getPlayer(), event.getMob(), damage));
    }
}
