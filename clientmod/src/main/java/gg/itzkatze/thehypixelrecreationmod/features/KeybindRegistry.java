package gg.itzkatze.thehypixelrecreationmod.features;

import gg.itzkatze.thehypixelrecreationmod.features.guicapture.GuiCaptureRecorder;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeybindRegistry {
    public static KeyMapping checkSkinKey;
    public static KeyMapping copyLoreKey;
    public static KeyMapping copyChatKey;
    public static KeyMapping copyGuiKey;
    public static KeyMapping copyCurrentGuiKey;
    public static KeyMapping entityPacketInspectorKey;
    private static boolean chatCopyPressed = false;

    private KeybindRegistry() {
    }

    public static void register() {
        checkSkinKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.thehypixelrecreationmod.copyplayerheadskin",
                GLFW.GLFW_KEY_K,
                KeyMapping.Category.MISC
        ));

        copyCurrentGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.thehypixelrecreationmod.copycurrentgui",
                GLFW.GLFW_KEY_PAGE_DOWN,
                KeyMapping.Category.MISC
        ));

        copyLoreKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.thehypixelrecreationmod.copylore",
                GLFW.GLFW_KEY_L,
                KeyMapping.Category.MISC
        ));

        copyChatKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.thehypixelrecreationmod.copychat",
                GLFW.GLFW_KEY_P,
                KeyMapping.Category.MISC
        ));

        copyGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.thehypixelrecreationmod.copygui",
                GLFW.GLFW_KEY_B,
                KeyMapping.Category.MISC
        ));

        // Check keybinds inside of GUI's
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((scancode, key) -> {
                if (checkSkinKey.matches(key)) {
                    GetPlayerHeadSkin.checkHoveredItemForSkin(client);
                    return false;
                }
                if (copyLoreKey.matches(key)) {
                    CopyLoreFromItem.copyLore(client);
                    return false;
                }
                if (copyChatKey.matches(key)) {
                    chatCopyPressed = true;
                    return false;
                }
                if (copyGuiKey.matches(key)) {
                    GuiCaptureRecorder.toggle(client);
                    return false;
                }
                if (copyCurrentGuiKey.matches(key)) {
                    CopyCurrentGui.copyCurrentGui(client);
                    return false;
                }
                return true;
            });
        });
    }

    public static boolean wasChatCopyPressed() {
        if (chatCopyPressed) {
            chatCopyPressed = false;
            return true;
        }
        return false;
    }
}
