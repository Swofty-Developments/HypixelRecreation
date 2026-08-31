package net.swofty.type.skyblockgeneric.data.datapoints;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.protocol.serializers.UnderstandableSkyBlockItemSerializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.entity.PetEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetAbilityRegistry;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatapointPetData extends SkyBlockDatapoint<DatapointPetData.UserPetData> {
    private static final Serializer serializer = new Serializer<UserPetData>() {
        @Override
        public String serialize(UserPetData value) {
            JSONObject jsonObject = new JSONObject();
            HashMap<SkyBlockItem, Boolean> petsMap = value.getPetsMap();

            for (SkyBlockItem item : petsMap.keySet()) {
                boolean enabled = petsMap.get(item);
                jsonObject.put(new UnderstandableSkyBlockItemSerializer().serialize(item.toUnderstandable()), enabled);
            }

            return jsonObject.toString();
        }

        @Override
        public UserPetData deserialize(String json) {
            JSONObject jsonObject = new JSONObject(json);
            HashMap<SkyBlockItem, Boolean> petsMap = new HashMap<>();

            jsonObject.keySet().forEach(key -> {
                UnderstandableSkyBlockItem item = new UnderstandableSkyBlockItemSerializer().deserialize(key);
                boolean enabled = jsonObject.getBoolean(key);
                petsMap.put(new SkyBlockItem(item), enabled);
            });

            return new UserPetData(petsMap);
        }

        @Override
        public UserPetData clone(UserPetData value) {
            return new UserPetData(value.petsMap);
        }
    };

    public DatapointPetData(String key, UserPetData value) {
        super(key, value, serializer);
    }

    public DatapointPetData(String key) {
        super(key, new UserPetData(new HashMap<>()), serializer);
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true) // Due to protocols serializing "enabledPet"
    public static class UserPetData {
        private HashMap<SkyBlockItem, Boolean> petsMap;
        private PetEntityImpl enabledPetEntityImpl = null;
        @Getter(AccessLevel.NONE)
        private final transient Map<SkyBlockItem, List<PetAbility>> abilities = new HashMap<>();

        public UserPetData() {
            this.petsMap = new HashMap<>();
        }

        public UserPetData(HashMap<SkyBlockItem, Boolean> pets) {
            this.petsMap = pets;
        }

        public void addPet(SkyBlockItem pet) {
            petsMap.put(pet, false);
        }

        public void setEnabled(ItemType type, boolean enabled) {
            // Set all previous true pets to false
            petsMap.keySet().forEach(pet -> petsMap.put(pet, false));

            // Set the new pet to the new state
            petsMap.keySet().stream().filter(pet -> pet.getAttributeHandler().getPotentialType() == type).findFirst().ifPresent(pet -> petsMap.put(pet, enabled));

            if (enabledPetEntityImpl != null)
                enabledPetEntityImpl.remove();
        }

        public void updatePetEntityImpl(SkyBlockPlayer player) {
            if (enabledPetEntityImpl != null) {
                enabledPetEntityImpl.kill();
                enabledPetEntityImpl.remove();
            }
            if (player == null) return;

            SkyBlockItem enabledPet = getEnabledPet();
            if (enabledPet != null) {
                enabledPetEntityImpl = new PetEntityImpl(player, enabledPet);
                enabledPetEntityImpl.setInstance(player.getInstance(), player.getPosition());
            }
        }

        public void isEnabled(ItemType type) {
            petsMap.keySet().stream().filter(pet -> pet.getAttributeHandler().getPotentialType() == type).findFirst().ifPresent(petsMap::get);
        }

        public void deselectCurrent() {
            petsMap.keySet().forEach(pet -> petsMap.put(pet, false));
        }

        public @Nullable SkyBlockItem getEnabledPet() {
            return petsMap.keySet().stream().filter(petsMap::get).findFirst().orElse(null);
        }

        public boolean isActive(SkyBlockItem pet) {
            return Boolean.TRUE.equals(petsMap.get(pet)) || pet.getComponent(PetComponent.class).isPassive();
        }

        public List<SkyBlockItem> getActivePets() {
            List<SkyBlockItem> active = new ArrayList<>();
            SkyBlockItem enabled = getEnabledPet();
            if (enabled != null) active.add(enabled);
            for (SkyBlockItem pet : petsMap.keySet()) {
                if (pet != enabled && pet.getComponent(PetComponent.class).isPassive()) active.add(pet);
            }
            return active;
        }

        public List<PetAbility> getAbilities(SkyBlockItem pet) {
            abilities.keySet().removeIf(cached -> !isActive(cached));
            return abilities.computeIfAbsent(pet, PetAbilityRegistry::getAbilities);
        }

        public <E extends PetEvent> E dispatch(E event) {
            for (SkyBlockItem pet : getActivePets()) {
                event.pet(pet); // rebind for passive and enabled pets
                for (PetAbility ability : getAbilities(pet)) {
                    PetAbilityRegistry.invoke(ability, event);
                }
            }
            return event;
        }

        public @Nullable SkyBlockItem getPet(ItemType type) {
            return petsMap.keySet().stream().filter(pet -> pet.getAttributeHandler().getPotentialType() == type).findFirst().orElse(null);
        }

        public void removePet(ItemType petType) {
            petsMap.keySet().removeIf(pet -> pet.getAttributeHandler().getPotentialType() == petType);
        }
    }
}
