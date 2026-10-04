package com.cs2knifeanim.animation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.WeakHashMap;

public class M9AnimationController {
    private static final M9AnimationController INSTANCE = new M9AnimationController();
    public static M9AnimationController getInstance() { return INSTANCE; }

    private float animationProgress = -1; // -1 表示没有动画
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

    public void startAnimation() {
        this.animationProgress = 1.0f; // 从1.0开始，然后递减到0
    }

    public float getAnimationProgress(float partialTick) {
        if (animationProgress < 0) return -1;
        // 每帧减少进度，具体速度可以调整
        animationProgress -= 0.05f * partialTick;
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
