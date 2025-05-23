package com.example.extrahotbar.mixin;

import com.example.extrahotbar.ExtraHotbarMod;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin {

    /**
     * 扩展物品栏大小 - 在初始化时注入
     */
    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            // 获取物品列表字段
            Inventory inventory = (Inventory)(Object)this;
            List<ItemStack> items = inventory.items;

            // 如果物品列表是 ArrayList，我们可以直接添加额外空槽位
            if (items instanceof ArrayList) {
                int existingSize = items.size();
                int extraSlotsToAdd = ExtraHotbarMod.EXTRA_SLOTS_COUNT;

                for (int i = 0; i < extraSlotsToAdd; i++) {
                    if (existingSize <= 36 + i) {  // 确保不重复添加
                        items.add(ItemStack.EMPTY);
                    }
                }
                ExtraHotbarMod.LOGGER.info("Extended player inventory with {} extra slots", extraSlotsToAdd);
            } else {
                ExtraHotbarMod.LOGGER.error("Failed to extend inventory: items list is not an ArrayList");
            }
        } catch (Exception e) {
            ExtraHotbarMod.LOGGER.error("Error extending inventory", e);
        }
    }
}