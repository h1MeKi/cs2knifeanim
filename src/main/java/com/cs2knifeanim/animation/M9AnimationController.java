package com.cs2knifeanim.animation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public class M9AnimationController {
    private static final M9AnimationController INSTANCE = new M9AnimationController();
    public static M9AnimationController getInstance() { return INSTANCE; }

    private long animationStartTime = -1;
    private float animationDuration = 0.8f;

    private final Map<Player, Item> lastItemMap = new WeakHashMap<>();
    private final Map<Player, Integer> lastSlotMap = new WeakHashMap<>(); // 记录选中槽位

    private M9AnimationController() {}

    // 判断是否为首次切刀（物品变化 或 选中槽位变化）
    public boolean isFirstSwitch(Player player, ItemStack currentStack, int currentSlot) {
        if (player == null || currentStack == null) return false;
        Item lastItem = lastItemMap.get(player);
        Integer lastSlot = lastSlotMap.get(player);
        Item currentItem = currentStack.getItem();
        
        // 如果物品类型变了，或者选中的快捷栏槽位变了（滚轮切换），都触发动画
        return lastItem != currentItem || lastSlot == null || lastSlot != currentSlot;
    }

    public void markSwitched(Player player, ItemStack stack, int currentSlot) {
        if (player != null && stack != null) {
            lastItemMap.put(player, stack.getItem());
            lastSlotMap.put(player, currentSlot);
        }
    }

    public void startAnimation(float durationSeconds) {
        this.animationStartTime = System.currentTimeMillis();
        this.animationDuration = durationSeconds;
    }

    public float getAnimationProgress(float partialTick) {
        if (animationStartTime < 0) return -1;
        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = 1.0f - (elapsed / (animationDuration * 1000.0f));
        if (progress < 0) {
            animationStartTime = -1;
            return -1;
        }
        return progress;
    }

    public void resetPlayer(Player player) {
        lastItemMap.remove(player);
        lastSlotMap.remove(player);
    }
}
