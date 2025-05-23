package com.example.extrahotbar;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ExtraHotbarMod.MODID)
public class ExtraHotbarMod {
    public static final String MODID = "extrahotbar";
    public static final Logger LOGGER = LogUtils.getLogger();

    // 额外物品栏的大小
    public static final int EXTRA_SLOTS_COUNT = 9;

    // 当前选中的额外槽位
    private static int selectedExtraSlot = 0;

    public ExtraHotbarMod(IEventBus modEventBus) {
        // 注册键绑定
        modEventBus.register(ExtraHotbarKeyBindings.class);

        // 注册客户端事件
        NeoForge.EVENT_BUS.register(ExtraHotbarRenderer.class);
        NeoForge.EVENT_BUS.register(ExtraHotbarClientEvents.class);

        LOGGER.info("Extra Hotbar mod initialized with {} extra slots", EXTRA_SLOTS_COUNT);
    }

    // 用于获取额外槽位的起始索引
    public static int getExtraSlotStartIndex() {
        return 36; // 原版物品栏有36个槽位
    }

    // 选中的槽位管理
    public static int getSelectedExtraSlot() {
        return selectedExtraSlot;
    }

    public static void setSelectedExtraSlot(int slot) {
        selectedExtraSlot = Math.floorMod(slot, EXTRA_SLOTS_COUNT);
    }

    public static void scrollExtraSlot(boolean up) {
        if (up) {
            selectedExtraSlot = Math.floorMod(selectedExtraSlot - 1, EXTRA_SLOTS_COUNT);
        } else {
            selectedExtraSlot = Math.floorMod(selectedExtraSlot + 1, EXTRA_SLOTS_COUNT);
        }
    }
}