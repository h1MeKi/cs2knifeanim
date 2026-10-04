package com.cs2knifeanim.client;

import com.cs2knifeanim.animation.M9AnimationController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = "cs2knifeanim", value = Dist.CLIENT)
public class RenderHandler {

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !isSword(event.getItemStack())) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        M9AnimationController controller = M9AnimationController.getInstance();
        int selectedSlot = player.getInventory().getSelectedSlot();

        if (controller.isFirstSwitch(player, event.getItemStack(), selectedSlot)) {
            // 修复核心：不再直接使用可能为 0 的冷却值，而是限制在 0.4~0.8 秒之间
            float attackSpeed = player.getCurrentItemAttackStrengthDelay();
            float animationDuration = Math.min(0.8f, Math.max(0.4f, attackSpeed / 20.0f));

            controller.startAnimation(animationDuration);
            controller.markSwitched(player, event.getItemStack(), selectedSlot);
            
            System.out.println("[CS2 Knife] Animation started! Duration: " + animationDuration + "s");
        }

        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            applyM9BayonetTransform(event.getPoseStack(), animProgress);
        }
    }

    private static void applyM9BayonetTransform(PoseStack poseStack, float progress) {
        // progress 从 1.0（开始）到 0.0（结束），转换为 t 从 0.0 到 1.0
        float t = 1.0f - progress;
        // 正弦波：让平移和旋转在动画中间达到最大值，两端平滑归零
        float sinValue = (float) Math.sin(t * Math.PI);

        // 1. 抬手/下压：轻微下移再回位
        float translateY = -0.2f * sinValue; 
        // 2. 向右轻微摆动
        float translateX = 0.1f * sinValue;
        poseStack.translate(translateX, translateY, 0);

        // 3. 旋转：Z轴翻滚 180 度 + X轴倾斜 30 度
        float roll = -180f * sinValue;
        float pitch = -30f * sinValue;

        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(roll)));
        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(pitch)));
    }

    private static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
               stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) ||
               stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
    }
}
