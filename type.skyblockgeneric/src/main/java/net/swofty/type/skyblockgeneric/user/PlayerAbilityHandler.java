package net.swofty.type.skyblockgeneric.user;

import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerAbilityHandler {
    private final SkyBlockPlayer player;
    private final Map<String, Long> abilityCooldowns = new HashMap<>();

    public PlayerAbilityHandler(SkyBlockPlayer player) {
        this.player = player;
    }

    public boolean canUseAbility(SkyBlockItem item, int coolDownDurationTicks) {
        String itemType = item.getAttributeHandler().getTypeAsString();
        Long lastUsedTime = abilityCooldowns.get(itemType);

        long cooldownDurationMillis = (long) effectiveCooldown(item, coolDownDurationTicks);
        return lastUsedTime == null || (System.currentTimeMillis() - lastUsedTime) >= cooldownDurationMillis;
    }

    public long getRemainingCooldown(SkyBlockItem item, int coolDownDurationTicks) {
        String itemType = item.getAttributeHandler().getTypeAsString();
        Long lastUsedTime = abilityCooldowns.get(itemType);
        long cooldownDurationMillis = (long) effectiveCooldown(item, coolDownDurationTicks);
        if (lastUsedTime == null) {
            return 0;
        }
        long timePassed = System.currentTimeMillis() - lastUsedTime;
        return timePassed < cooldownDurationMillis ? cooldownDurationMillis - timePassed : 0;
    }

    public void startAbilityCooldown(SkyBlockItem item) {
        abilityCooldowns.put(item.getAttributeHandler().getTypeAsString(), System.currentTimeMillis());
    }

    private double effectiveCooldown(SkyBlockItem item, int coolDownDurationTicks) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        PetEvent.AbilityCooldown event = player.getPetData()
                .dispatch(new PetEvent.AbilityCooldown(player, pet, item, coolDownDurationTicks * 50D));
        return event.cooldown();
    }
}
