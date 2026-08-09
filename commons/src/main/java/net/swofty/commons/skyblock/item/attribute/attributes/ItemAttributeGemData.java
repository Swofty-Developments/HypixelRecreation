package net.swofty.commons.skyblock.item.attribute.attributes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemAttributeGemData extends ItemAttribute<ItemAttributeGemData.GemData> {

    @Override
    public String getKey() {
        return "gems";
    }

    @Override
    public GemData getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return new GemData(new ArrayList<>());
    }

    @Override
    public GemData loadFromString(String string) {
        GemData gemData = new GemData(new ArrayList<>());

        if (string.isEmpty()) {
            return gemData;
        }

        String[] split = string.split(",");
        for (String gem : split) {
            String[] gemSplit = gem.split(":");
            int index = Integer.parseInt(gemSplit[0]);
            ItemType filledWith = null;
            boolean unlocked = gemSplit.length > 2 && Boolean.parseBoolean(gemSplit[2]);

            if (!gemSplit[1].equals("null")) {
                filledWith = ItemType.valueOf(gemSplit[1]);
                unlocked = true;
            }

            gemData.putGem(new GemData.GemSlots(index, filledWith, unlocked));
        }

        return gemData;
    }

    @Override
    public String saveIntoString() {
        List<String> serializedGems = new ArrayList<>();

        this.value.slots.forEach(gem -> {
            String filledWith = gem.filledWith == null ? "null" : gem.filledWith.name();
            serializedGems.add(gem.index + ":" + filledWith + ":" + gem.unlocked);
        });

        return String.join(",", serializedGems);
    }

    public static class GemData {
        public List<GemSlots> slots;

        public GemData(List<GemSlots> slots) {
            this.slots = slots;
        }

        public void putGem(GemSlots slot) {
            slots.add(slot);
        }

        public boolean hasGem(int index) {
            for (GemSlots slot : slots) {
                if (slot.index == index) {
                    return true;
                }
            }
            return false;
        }

        public GemSlots getGem(int index) {
            for (GemSlots slot : slots) {
                if (slot.index == index) {
                    return slot;
                }
            }
            return null;
        }

        public boolean isSlotUnlocked(int index) {
            GemSlots slot = getGem(index);
            return slot != null && slot.unlocked;
        }

        public void unlockSlot(int index) {
            GemSlots existing = getGem(index);
            if (existing != null) {
                existing.unlocked = true;
            } else {
                putGem(new GemSlots(index, null, true));
            }
        }

        public void removeGem(int index) {
            GemSlots existing = getGem(index);
            if (existing != null) {
                existing.filledWith = null;
            }
        }

        @AllArgsConstructor
        @Getter
        @Setter
        public static class GemSlots {
            public int index;
            public @Nullable ItemType filledWith;
            public boolean unlocked;
        }
    }
}
