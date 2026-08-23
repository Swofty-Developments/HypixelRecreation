package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ScreenTracker {
    private final EventSink sink;
    private final Map<Integer, ItemStack> snapshot = new HashMap<>();

    private String currentKey;
    private String currentTitle;
    private int currentContainerId = -1;
    private int screenCount;
    private int changeCount;
    private int clickCount;

    ScreenTracker(EventSink sink) {
        this.sink = sink;
    }

    int screenCount() {
        return screenCount;
    }

    int changeCount() {
        return changeCount;
    }

    int clickCount() {
        return clickCount;
    }

    void tick(Minecraft client) {
        Screen screen = client.gui.screen();
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) {
            if (currentKey != null) {
                emitClose("screen-changed");
            }
            return;
        }

        List<Slot> slots = containerSlots(containerScreen);
        String title = StringUtility.toLegacyString(containerScreen.getTitle());
        int containerId = containerScreen.getMenu().containerId;
        String key = containerId + "|" + title + "|" + slots.size();

        if (!key.equals(currentKey)) {
            if (currentKey != null) {
                emitClose("replaced");
            }
            openScreen(containerScreen, slots, title, containerId, key);
            return;
        }

        for (Slot slot : slots) {
            ItemStack now = slot.getItem();
            ItemStack before = snapshot.get(slot.index);
            if (before != null && ItemStack.matches(before, now)) {
                continue;
            }

            snapshot.put(slot.index, now.copy());
            changeCount++;

            JsonObject payload = new JsonObject();
            payload.addProperty("containerId", containerId);
            payload.addProperty("title", title);
            payload.addProperty("slot", slot.index);
            payload.add("from", before == null ? null : CaptureJson.item(before));
            payload.add("to", CaptureJson.item(now));
            sink.emit("gui", "gui:change", payload);
        }
    }

    void onOpenScreenPacket(ClientboundOpenScreenPacket packet) {
        JsonObject payload = CaptureJson.fields(packet);
        sink.emit("gui", "gui:openScreenPacket", payload);
    }

    void onContainerData(ClientboundContainerSetDataPacket packet) {
        JsonObject payload = CaptureJson.fields(packet);
        sink.emit("gui", "gui:property", payload);
    }

    void onSlotClick(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ContainerInput input) {
        clickCount++;

        JsonObject payload = new JsonObject();
        payload.addProperty("containerId", screen.getMenu().containerId);
        payload.addProperty("title", StringUtility.toLegacyString(screen.getTitle()));
        payload.addProperty("slot", slotId);
        payload.addProperty("slotIndex", slot == null ? -1 : slot.index);
        payload.addProperty("button", button);
        payload.addProperty("input", input == null ? "UNKNOWN" : input.name());
        payload.add("item", slot == null ? null : CaptureJson.item(slot.getItem()));
        sink.emit("gui", "gui:click", payload);
    }

    void onScreenClosed() {
        if (currentKey != null) {
            emitClose("closed");
        }
    }

    void reset() {
        currentKey = null;
        currentTitle = null;
        currentContainerId = -1;
        snapshot.clear();
    }

    private void openScreen(AbstractContainerScreen<?> screen, List<Slot> slots, String title,
                            int containerId, String key) {
        currentKey = key;
        currentTitle = title;
        currentContainerId = containerId;
        screenCount++;
        snapshot.clear();

        JsonObject payload = new JsonObject();
        payload.addProperty("containerId", containerId);
        payload.addProperty("title", title);
        payload.add("titleJson", CaptureJson.componentTree(screen.getTitle()));
        payload.addProperty("menu", screen.getMenu().getClass().getSimpleName());
        payload.addProperty("slotCount", slots.size());
        payload.addProperty("totalSlots", screen.getMenu().slots.size());
        payload.addProperty("inventoryType", inventoryType(slots.size()));

        JsonObject items = new JsonObject();
        for (Slot slot : slots) {
            ItemStack stack = slot.getItem();
            snapshot.put(slot.index, stack.copy());
            if (!stack.isEmpty()) {
                items.add(String.valueOf(slot.index), CaptureJson.item(stack));
            }
        }
        payload.add("slots", items);
        sink.emit("gui", "gui:open", payload);
    }

    private void emitClose(String reason) {
        JsonObject payload = new JsonObject();
        payload.addProperty("containerId", currentContainerId);
        payload.addProperty("title", currentTitle);
        payload.addProperty("reason", reason);

        JsonObject finalItems = new JsonObject();
        for (Map.Entry<Integer, ItemStack> entry : snapshot.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                finalItems.add(String.valueOf(entry.getKey()), CaptureJson.item(entry.getValue()));
            }
        }
        payload.add("finalSlots", finalItems);
        sink.emit("gui", "gui:close", payload);
        reset();
    }

    private static List<Slot> containerSlots(AbstractContainerScreen<?> screen) {
        List<Slot> slots = screen.getMenu().slots;
        int containerSize = Math.max(0, slots.size() - 36);
        if (containerSize == 0) {
            containerSize = slots.size();
        }
        return slots.subList(0, Math.min(containerSize, slots.size()));
    }

    private static String inventoryType(int slotCount) {
        return switch (slotCount) {
            case 5 -> "ANVIL";
            case 9 -> "CHEST_1_ROW";
            case 18 -> "CHEST_2_ROW";
            case 27 -> "CHEST_3_ROW";
            case 36 -> "CHEST_4_ROW";
            case 45 -> "CHEST_5_ROW";
            case 54 -> "CHEST_6_ROW";
            default -> "UNKNOWN_" + slotCount;
        };
    }
}
