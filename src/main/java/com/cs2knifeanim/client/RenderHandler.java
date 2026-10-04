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
        // 1. 过滤：只处理主手和剑
        if (event.getHand() != InteractionHand.MAIN_HAND || !isSword(event.getItemStack())) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        M9AnimationController controller = M9AnimationController.getInstance();
        int selectedSlot = player.getInventory().getSelectedSlot();

        // 2. 判断切刀
        if (controller.isFirstSwitch(player, event.getItemStack(), selectedSlot)) {
            float animationDuration = 0.6f; // 固定 0.6 秒，排除冷却机制干扰
            controller.startAnimation(animationDuration);
            controller.markSwitched(player, event.getItemStack(), selectedSlot);
            
            // 【关键排查点】每次触发切刀，控制台都会打印这句话
            System.out.println("[CS2 Knife] Animation started! Slot: " + selectedSlot + ", Duration: " + animationDuration);
        }

        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            // 【关键排查点】如果动画在播放，控制台会疯狂打印进度
            // System.out.println("[CS2 Knife] Anim Progress: " + animProgress); 
            
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose(); // 保存当前渲染状态
            applyM9BayonetTransform(poseStack, animProgress);
            poseStack.popPose();  // 恢复渲染状态，避免影响其他渲染
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
