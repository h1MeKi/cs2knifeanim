package com.cs2knifeanim.animation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public class M9AnimationController {
    private static final M9AnimationController INSTANCE = new M9AnimationController();
    public static M9AnimationController getInstance() { return INSTANCE; }

    private int animationTicks = 0;
    private int animationDurationTicks = 0;
    private boolean isAnimating = false;

    private final Map<Player, Item> lastItemMap = new WeakHashMap<>();

    private M9AnimationController() {}

    // 判断是否为“首次切刀”（手持物品发生变化时）
    public boolean isFirstSwitch(Player player, ItemStack currentStack) {
        if (player == null || currentStack == null) return false;
        Item lastItem = lastItemMap.get(player);
        Item currentItem = currentStack.getItem();
        // 如果上次记录的物品与当前不同，说明触发了切刀
        return lastItem != currentItem;
    }

    public void markSwitched(Player player, ItemStack stack) {
        if (player != null && stack != null) {
            lastItemMap.put(player, stack.getItem());
        }
    }

    public void startAnimation(float durationSeconds) {
        this.animationDurationTicks = (int) (durationSeconds * 20);
        this.animationTicks = animationDurationTicks;
        this.isAnimating = true;
    }

    // 获取动画进度 (1.0 = 刚开始, 0.0 = 结束)
    public float getAnimationProgress(float partialTick) {
        if (!isAnimating) return -1;
        float progress = (animationTicks - partialTick) / (float) animationDurationTicks;
        return Math.max(0, Math.min(1, progress));
    }

    public void tick() {
        if (isAnimating) {
            animationTicks--;
            if (animationTicks <= 0) {
                isAnimating = false;
            }
        }
    }

    public void resetPlayer(Player player) {
        lastItemMap.remove(player);
    }
}