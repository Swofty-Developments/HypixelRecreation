package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class PlayerTracker {
    private static final int POSITION_INTERVAL_TICKS = 2;

    private final EventSink sink;
    private final List<ItemStack> inventory = new ArrayList<>();

    private boolean initialised;
    private int selectedSlot = -1;
    private String lastState;
    private int positionCountdown;
    private int changeCount;

    PlayerTracker(EventSink sink) {
        this.sink = sink;
    }

    int changeCount() {
        return changeCount;
    }

    void reset() {
        initialised = false;
        inventory.clear();
        selectedSlot = -1;
        lastState = null;
    }

    void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        trackInventory(player);
        trackState(client, player);
        trackPosition(player);
    }

    private void trackInventory(LocalPlayer player) {
        Inventory bag = player.getInventory();
        int size = bag.getContainerSize();

        while (inventory.size() < size) {
            inventory.add(ItemStack.EMPTY);
        }

        if (!initialised) {
            initialised = true;
            JsonObject payload = new JsonObject();
            JsonObject slots = new JsonObject();
            for (int slot = 0; slot < size; slot++) {
                ItemStack stack = bag.getItem(slot);
                inventory.set(slot, stack.copy());
                if (!stack.isEmpty()) {
                    slots.add(String.valueOf(slot), CaptureJson.item(stack));
                }
            }
            payload.addProperty("size", size);
            payload.addProperty("selectedSlot", bag.getSelectedSlot());
            payload.add("slots", slots);
            sink.emit("inventory", "inv:snapshot", payload);
            selectedSlot = bag.getSelectedSlot();
            return;
        }

        for (int slot = 0; slot < size; slot++) {
            ItemStack now = bag.getItem(slot);
            ItemStack before = inventory.get(slot);
            if (ItemStack.matches(before, now)) {
                continue;
            }

            inventory.set(slot, now.copy());
            changeCount++;

            JsonObject payload = new JsonObject();
            payload.addProperty("slot", slot);
            payload.add("from", CaptureJson.item(before));
            payload.add("to", CaptureJson.item(now));
            sink.emit("inventory", "inv:change", payload);
        }

        if (bag.getSelectedSlot() != selectedSlot) {
            selectedSlot = bag.getSelectedSlot();
            JsonObject payload = new JsonObject();
            payload.addProperty("selectedSlot", selectedSlot);
            payload.add("item", CaptureJson.item(bag.getSelectedItem()));
            sink.emit("inventory", "inv:held", payload);
        }
    }

    private void trackState(Minecraft client, LocalPlayer player) {
        JsonObject payload = new JsonObject();
        payload.addProperty("health", CaptureJson.round(player.getHealth()));
        payload.addProperty("maxHealth", CaptureJson.round(player.getMaxHealth()));
        payload.addProperty("absorption", CaptureJson.round(player.getAbsorptionAmount()));
        payload.addProperty("food", player.getFoodData().getFoodLevel());
        payload.addProperty("saturation", CaptureJson.round(player.getFoodData().getSaturationLevel()));
        payload.addProperty("air", player.getAirSupply());
        payload.addProperty("experienceLevel", player.experienceLevel);
        payload.addProperty("experienceProgress", CaptureJson.round(player.experienceProgress));
        payload.addProperty("gameMode", client.gameMode == null ? "UNKNOWN" : client.gameMode.getPlayerMode().name());
        payload.addProperty("pose", player.getPose().name());
        payload.addProperty("vehicle", player.getVehicle() == null
                ? null
                : player.getVehicle().getType().toShortString());

        JsonArray effects = new JsonArray();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("effect", String.valueOf(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value())));
            entry.addProperty("amplifier", effect.getAmplifier());
            entry.addProperty("duration", effect.getDuration());
            effects.add(entry);
        }
        payload.add("effects", effects);

        String fingerprint = payload.toString();
        if (fingerprint.equals(lastState)) {
            return;
        }
        lastState = fingerprint;
        sink.emit("player", "player:state", payload);
    }

    private void trackPosition(LocalPlayer player) {
        if (--positionCountdown > 0) {
            return;
        }
        positionCountdown = POSITION_INTERVAL_TICKS;

        JsonObject payload = new JsonObject();
        payload.addProperty("x", CaptureJson.round(player.getX()));
        payload.addProperty("y", CaptureJson.round(player.getY()));
        payload.addProperty("z", CaptureJson.round(player.getZ()));
        payload.addProperty("yaw", CaptureJson.round(player.getYRot()));
        payload.addProperty("pitch", CaptureJson.round(player.getXRot()));
        payload.addProperty("onGround", player.onGround());
        payload.addProperty("sprinting", player.isSprinting());
        payload.addProperty("sneaking", player.isCrouching());
        sink.emit("player", "player:pos", payload);
    }
}
