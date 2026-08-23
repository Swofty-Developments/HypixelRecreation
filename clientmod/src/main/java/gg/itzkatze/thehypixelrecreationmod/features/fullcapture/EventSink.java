package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.JsonObject;

@FunctionalInterface
interface EventSink {
    void emit(String stream, String type, JsonObject payload);
}
