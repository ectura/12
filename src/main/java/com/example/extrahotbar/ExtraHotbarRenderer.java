package com.example.extrahotbar;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.function.Function;

public class ExtraHotbarRenderer {

    // 使用原版的热键栏纹理资源
    private static final ResourceLocation WIDGETS_LOCATION = ResourceLocation.parse("minecraft:textures/gui/widgets.png");

    // 创建渲染类型获取器
    private static final Function<ResourceLocation, RenderType> GUI_RENDER_TYPE = loc -> RenderType.gui();

    // 热键栏纹理中的 UV 坐标
    private static final int HOTBAR_TEX_WIDTH = 182;
    private static final int SLOT_WIDTH = 20;
    private static final int SLOT_HEIGHT = 22;
    private static final int ITEM_OFFSET_X = 3;
    private static final int ITEM_OFFSET_Y = 3;

    // 选择框纹理中的 UV 坐标
    private static final int SELECTION_TEX_U = 0;
    private static final int SELECTION_TEX_V = 22;
    private static final int SELECTION_TEX_WIDTH = 24;
    private static final int SELECTION_TEX_HEIGHT = 24;

    /**
     * 渲染界面中的额外快捷栏
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.options.hideGui) {
                return;
            }

            renderExtraHotbar(
                    event.getGuiGraphics(),
                    minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight(),
                    minecraft.player
            );
        } catch (Exception e) {
            ExtraHotbarMod.LOGGER.error("Error rendering extra hotbar", e);
        }
    }

    /**
     * 渲染背包屏幕中的额外快捷栏
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            renderExtraHotbar(
                    event.getGuiGraphics(),
                    event.getScreen().width,
                    event.getScreen().height,
                    minecraft.player
            );
        } catch (Exception e) {
            ExtraHotbarMod.LOGGER.error("Error rendering extra hotbar on screen", e);
        }
    }

    /**
     * 渲染额外快捷栏的核心方法
     */
    private static void renderExtraHotbar(GuiGraphics guiGraphics, int screenWidth, int screenHeight, Player player) {
        int slotCount = ExtraHotbarMod.EXTRA_SLOTS_COUNT;
        int selectedExtraSlot = ExtraHotbarMod.getSelectedExtraSlot();
        float opacity = 1.0F;

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, opacity);

        // 分成左右两部分显示 - 热键栏两侧
        int halfSlots = Math.min(slotCount, 9) / 2;
        int remainingSlots = Math.min(slotCount, 9) - halfSlots;

        // 计算原版热键栏位置
        int vanillaHotbarX = (screenWidth - HOTBAR_TEX_WIDTH) / 2;
        int vanillaHotbarY = screenHeight - 22;

        // 左侧额外槽位
        for (int i = 0; i < halfSlots; i++) {
            int slotX = vanillaHotbarX - (halfSlots - i) * SLOT_WIDTH;
            int slotY = vanillaHotbarY;

            // 渲染槽位背景
            guiGraphics.blit(GUI_RENDER_TYPE, WIDGETS_LOCATION, slotX, slotY,
                    (float)0, (float)0, SLOT_WIDTH, SLOT_HEIGHT, 256, 256);

            // 如果是选中槽位，渲染选择框
            if (i == selectedExtraSlot) {
                guiGraphics.blit(GUI_RENDER_TYPE, WIDGETS_LOCATION, slotX - 1, slotY - 1,
                        (float)SELECTION_TEX_U, (float)SELECTION_TEX_V,
                        SELECTION_TEX_WIDTH, SELECTION_TEX_HEIGHT, 256, 256);
            }

            // 渲染物品
            int slotIndex = ExtraHotbarMod.getExtraSlotStartIndex() + i;
            if (slotIndex < player.getInventory().items.size()) {
                ItemStack stack = player.getInventory().items.get(slotIndex);
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, slotX + ITEM_OFFSET_X, slotY + ITEM_OFFSET_Y);
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, slotX + ITEM_OFFSET_X, slotY + ITEM_OFFSET_Y);
                }
            }
        }

        // 右侧额外槽位
        for (int i = 0; i < remainingSlots; i++) {
            int slotX = vanillaHotbarX + HOTBAR_TEX_WIDTH + i * SLOT_WIDTH;
            int slotY = vanillaHotbarY;

            // 渲染槽位背景
            guiGraphics.blit(GUI_RENDER_TYPE, WIDGETS_LOCATION, slotX, slotY,
                    (float)0, (float)0, SLOT_WIDTH, SLOT_HEIGHT, 256, 256);

            // 如果是选中槽位，渲染选择框
            if (i + halfSlots == selectedExtraSlot) {
                guiGraphics.blit(GUI_RENDER_TYPE, WIDGETS_LOCATION, slotX - 1, slotY - 1,
                        (float)SELECTION_TEX_U, (float)SELECTION_TEX_V,
                        SELECTION_TEX_WIDTH, SELECTION_TEX_HEIGHT, 256, 256);
            }

            // 渲染物品
            int slotIndex = ExtraHotbarMod.getExtraSlotStartIndex() + i + halfSlots;
            if (slotIndex < player.getInventory().items.size()) {
                ItemStack stack = player.getInventory().items.get(slotIndex);
                if (!stack.isEmpty()) {
                    guiGraphics.renderItem(stack, slotX + ITEM_OFFSET_X, slotY + ITEM_OFFSET_Y);
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, slotX + ITEM_OFFSET_X, slotY + ITEM_OFFSET_Y);
                }
            }
        }

        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    /**
     * 检查鼠标点击是否在额外快捷栏的某个槽位上
     */
    public static int getClickedSlot(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int slotCount = ExtraHotbarMod.EXTRA_SLOTS_COUNT;

        // 计算原版热键栏位置
        int vanillaHotbarX = (screenWidth - HOTBAR_TEX_WIDTH) / 2;
        int vanillaHotbarY = screenHeight - 22;

        // 检查左侧部分
        int halfSlots = Math.min(slotCount, 9) / 2;
        for (int i = 0; i < halfSlots; i++) {
            int slotX = vanillaHotbarX - (halfSlots - i) * SLOT_WIDTH;

            if (mouseX >= slotX && mouseX < slotX + SLOT_WIDTH &&
                    mouseY >= vanillaHotbarY && mouseY < vanillaHotbarY + SLOT_HEIGHT) {
                return i;
            }
        }

        // 检查右侧部分
        int remainingSlots = Math.min(slotCount, 9) - halfSlots;
        for (int i = 0; i < remainingSlots; i++) {
            int slotX = vanillaHotbarX + HOTBAR_TEX_WIDTH + i * SLOT_WIDTH;

            if (mouseX >= slotX && mouseX < slotX + SLOT_WIDTH &&
                    mouseY >= vanillaHotbarY && mouseY < vanillaHotbarY + SLOT_HEIGHT) {
                return i + halfSlots;
            }
        }

        return -1;
    }
}