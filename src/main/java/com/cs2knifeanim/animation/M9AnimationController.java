package com.cs2knifeanim.animation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public class M9AnimationController {
    private static final M9AnimationController INSTANCE = new M9AnimationController();
    public static M9AnimationController getInstance() { return INSTANCE; }

    private float animationProgress = -1;
    private float animationSpeed = 0.05f; // 默认速度

    private final Map<Player, Item> lastItemMap = new WeakHashMap<>();

    private M9AnimationController() {}

    public boolean isFirstSwitch(Player player, ItemStack currentStack) {
        if (player == null || currentStack == null) return false;
        Item lastItem = lastItemMap.get(player);
        Item currentItem = currentStack.getItem();
        return lastItem != currentItem;
    }

    public void markSwitched(Player player, ItemStack stack) {
        if (player != null && stack != null) {
            lastItemMap.put(player, stack.getItem());
        }
    }

    public void startAnimation(float durationSeconds) {
        this.animationProgress = 1.0f;
        // 根据冷却时间计算动画播放速度（确保时长与冷却挂钩）
        this.animationSpeed = 0.05f * (1.0f / durationSeconds);
    }

    public float getAnimationProgress(float partialTick) {
        if (animationProgress < 0) return -1;
        animationProgress -= animationSpeed * partialTick;
        if (animationProgress < 0) {
            animationProgress = -1;
            return -1;
        }
        return animationProgress;
    }

    public void resetPlayer(Player player) {
        lastItemMap.remove(player);
    }
}
