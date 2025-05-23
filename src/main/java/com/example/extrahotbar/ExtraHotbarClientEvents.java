package com.example.extrahotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

public class ExtraHotbarClientEvents {

    /**
     * 监听鼠标点击事件
     */
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        try {
            // 只处理鼠标左键和右键点击
            if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_1 && event.getButton() != GLFW.GLFW_MOUSE_BUTTON_2) {
                return;
            }

            Minecraft minecraft = Minecraft.getInstance();

            // 如果玩家为空或有屏幕打开，则不处理
            if (minecraft.player == null || minecraft.screen != null) {
                return;
            }

            // 获取屏幕信息
            int screenWidth = minecraft.getWindow().getGuiScaledWidth();
            int screenHeight = minecraft.getWindow().getGuiScaledHeight();

            // 获取鼠标位置
            double mouseX = minecraft.mouseHandler.xpos() * screenWidth / minecraft.getWindow().getScreenWidth();
            double mouseY = minecraft.mouseHandler.ypos() * screenHeight / minecraft.getWindow().getScreenHeight();

            // 获取鼠标点击的槽位
            int slotIndex = ExtraHotbarRenderer.getClickedSlot((int)mouseX, (int)mouseY, screenWidth, screenHeight);

            // 如果点击了有效的槽位
            if (slotIndex != -1) {
                // 设置选中的额外槽位
                ExtraHotbarMod.setSelectedExtraSlot(slotIndex);

                // 是否是右键点击
                boolean isRightClick = (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_2);

                // 是否按下了Shift键
                boolean isShiftDown = GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                        GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

                // 处理点击事件
                boolean handled = handleSlotClick(slotIndex, isRightClick, isShiftDown);

                // 取消原版处理
                if (handled) {
                    event.setCanceled(true);
                }
            }
        } catch (Exception e) {
            ExtraHotbarMod.LOGGER.error("Error handling mouse click", e);
        }
    }

    /**
     * 监听鼠标滚轮事件
     */
    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        try {
            Minecraft minecraft = Minecraft.getInstance();

            // 如果玩家为空或有屏幕打开，则不处理
            if (minecraft.player == null || minecraft.screen != null) {
                return;
            }

            // 按住Shift键时滚动鼠标滚轮切换额外槽位
            boolean isShiftDown = GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                    GLFW.glfwGetKey(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;

            if (isShiftDown) {
                double scrollDelta = event.getScrollDeltaY();

                if (scrollDelta < 0) {
                    // 向下滚动，选择下一个槽位
                    ExtraHotbarMod.scrollExtraSlot(false);
                    minecraft.player.displayClientMessage(Component.literal("§e选择额外槽位 " + (ExtraHotbarMod.getSelectedExtraSlot() + 1)), true);
                    event.setCanceled(true);
                } else if (scrollDelta > 0) {
                    // 向上滚动，选择上一个槽位
                    ExtraHotbarMod.scrollExtraSlot(true);
                    minecraft.player.displayClientMessage(Component.literal("§e选择额外槽位 " + (ExtraHotbarMod.getSelectedExtraSlot() + 1)), true);
                    event.setCanceled(true);
                }
            }
        } catch (Exception e) {
            ExtraHotbarMod.LOGGER.error("Error handling mouse scroll", e);
        }
    }

    /**
     * 处理额外槽位的点击
     */
    private static boolean handleSlotClick(int extraSlotIndex, boolean isRightClick, boolean isShiftDown) {
        Minecraft minecraft = Minecraft.getInstance();
        Inventory inventory = minecraft.player.getInventory();

        // 计算实际物品栏索引
        int inventorySlotIndex = ExtraHotbarMod.getExtraSlotStartIndex() + extraSlotIndex;

        // 检查索引是否有效
        if (inventorySlotIndex >= inventory.items.size()) {
            ExtraHotbarMod.LOGGER.warn("Invalid inventory slot index: {}", inventorySlotIndex);
            return false;
        }

        // 获取当前物品
        ItemStack slotStack = inventory.getItem(inventorySlotIndex);
        ItemStack handStack = minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);

        // 交互逻辑
        if (isShiftDown) {
            // Shift+点击：移动到热键栏
            if (!slotStack.isEmpty()) {
                // 寻找热键栏中的空槽位
                for (int i = 0; i < 9; i++) {
                    if (inventory.getItem(i).isEmpty()) {
                        // 将物品移到这个槽位
                        inventory.setItem(i, slotStack.copy());
                        inventory.setItem(inventorySlotIndex, ItemStack.EMPTY);
                        minecraft.player.displayClientMessage(Component.literal("§a物品已移动到热键栏槽位 " + (i + 1)), true);
                        return true;
                    }
                }
                minecraft.player.displayClientMessage(Component.literal("§c热键栏没有空槽位"), true);
            }
        } else if (isRightClick) {
            // 右键点击：拆分物品
            if (slotStack.isEmpty() && !handStack.isEmpty()) {
                // 把手中物品放一半到槽位
                int count = handStack.getCount();
                if (count > 1) {
                    int halfCount = count / 2;
                    ItemStack halfStack = handStack.copy();
                    halfStack.setCount(halfCount);
                    handStack.setCount(count - halfCount);
                    inventory.setItem(inventorySlotIndex, halfStack);
                    minecraft.player.displayClientMessage(Component.literal("§a放置了一半物品"), true);
                    return true;
                } else if (count == 1) {
                    // 只有一个物品，直接放入
                    inventory.setItem(inventorySlotIndex, handStack.copy());
                    handStack.setCount(0);
                    minecraft.player.displayClientMessage(Component.literal("§a放置了物品"), true);
                    return true;
                }
            } else if (!slotStack.isEmpty()) {
                // 从槽位拿一个物品
                if (handStack.isEmpty()) {
                    // 手上没物品，拿一个
                    ItemStack oneItem = slotStack.copy();
                    oneItem.setCount(1);
                    minecraft.player.setItemInHand(InteractionHand.MAIN_HAND, oneItem);
                    slotStack.shrink(1);
                    if (slotStack.isEmpty()) {
                        inventory.setItem(inventorySlotIndex, ItemStack.EMPTY);
                    }
                    minecraft.player.displayClientMessage(Component.literal("§a拿取了一个物品"), true);
                    return true;
                } else if (handStack.getItem() == slotStack.getItem()) {
                    // 尝试堆叠
                    if (handStack.getCount() < handStack.getMaxStackSize()) {
                        handStack.grow(1);
                        slotStack.shrink(1);
                        if (slotStack.isEmpty()) {
                            inventory.setItem(inventorySlotIndex, ItemStack.EMPTY);
                        }
                        minecraft.player.displayClientMessage(Component.literal("§a拿取了一个物品"), true);
                        return true;
                    }
                }
            }
        } else {
            // 左键点击：交换物品
            ItemStack handCopy = handStack.copy();
            ItemStack slotCopy = slotStack.copy();

            minecraft.player.setItemInHand(InteractionHand.MAIN_HAND, slotCopy);
            inventory.setItem(inventorySlotIndex, handCopy);

            minecraft.player.displayClientMessage(Component.literal("§a交换了物品"), true);
            return true;
        }

        return false;
    }
}