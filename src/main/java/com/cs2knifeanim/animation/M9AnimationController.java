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
    private float animationDuration = 0.6f;

    private final Map<Player, Item> lastItemMap = new WeakHashMap<>();
    private final Map<Player, Integer> lastSlotMap = new WeakHashMap<>();

    private M9AnimationController() {}

    public boolean isFirstSwitch(Player player, ItemStack currentStack, int currentSlot) {
        if (player == null || currentStack == null) return false;
        Item lastItem = lastItemMap.get(player);
        Integer lastSlot = lastSlotMap.get(player);
        Item currentItem = currentStack.getItem();
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
        // 保护机制：时长绝对不能低于 0.2 秒，否则除以 0 会让动画消失
        this.animationDuration = Math.max(0.2f, durationSeconds); 
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
