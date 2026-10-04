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
            float animationDuration = 1.0f; // 加长到 1 秒，容易观察
            controller.startAnimation(animationDuration);
            controller.markSwitched(player, event.getItemStack(), selectedSlot);
            
            // 【强制日志】切刀触发时打印
            System.out.println("[CS2 Knife] Triggered! Slot: " + selectedSlot);
        }

        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            // 【强制日志】如果动画在执行，会每秒打印几十次
            // System.out.println("[CS2 Knife] Animating! Progress: " + animProgress);
            
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose(); 
            applyM9BayonetTransform(poseStack, animProgress);
            poseStack.popPose(); 
        }
    }

    private static void applyM9BayonetTransform(PoseStack poseStack, float progress) {
        float t = 1.0f - progress;
        float sinValue = (float) Math.sin(t * Math.PI);

        // ⚠️ 极端测试数值：大幅平移和旋转，只要生效，屏幕就会剧烈晃动
        float translateY = -2.0f * sinValue; 
        float translateX = 2.0f * sinValue;
        poseStack.translate(translateX, translateY, 0);

        float roll = -360f * sinValue; // 整整转一圈
        float pitch = -90f * sinValue;

        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(roll)));
        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(pitch)));
    }

    private static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
               stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) ||
               stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
    }
}
