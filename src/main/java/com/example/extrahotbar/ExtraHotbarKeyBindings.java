package com.example.extrahotbar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ExtraHotbarKeyBindings {

    // 切换选择的额外槽位
    public static final KeyMapping KEY_CYCLE_SLOT = new KeyMapping(
            "key.extrahotbar.cycle_slot",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.extrahotbar"
    );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_CYCLE_SLOT);
        ExtraHotbarMod.LOGGER.info("Extra Hotbar key bindings registered");
    }
}