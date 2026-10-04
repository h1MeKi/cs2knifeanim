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
        // 只处理主手，且必须是剑类物品
        if (event.getHand() != InteractionHand.MAIN_HAND || !isSword(event.getItemStack())) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        M9AnimationController controller = M9AnimationController.getInstance();
        int selectedSlot = player.getInventory().getSelectedSlot(); // 获取当前选中的快捷栏槽位

        // 判断是否是首次切刀（物品变化 或 槽位变化）
        if (controller.isFirstSwitch(player, event.getItemStack(), selectedSlot)) {
            // 基于攻击冷却时间计算动画长度
            float attackSpeed = player.getCurrentItemAttackStrengthDelay();
            float animationDuration = Math.min(attackSpeed / 20.0f, 0.8f); // 最长0.8秒

            controller.startAnimation(animationDuration);
            controller.markSwitched(player, event.getItemStack(), selectedSlot);
            
            // 调试日志：触发时打印
            System.out.println("[CS2 Knife] Animation started! Slot: " + selectedSlot + ", Duration: " + animationDuration + "s");
        }

        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            applyM9BayonetTransform(event.getPoseStack(), animProgress);
        }
    }

      private static void applyM9BayonetTransform(PoseStack poseStack, float progress) {
        // progress 从 1.0（开始）到 0.0（结束），我们转换为 t 从 0.0 到 1.0
        float t = 1.0f - progress;

        // 使用正弦函数，让平移和旋转在动画中间达到最大值，两端平滑归位
        float sinValue = (float) Math.sin(t * Math.PI);

        // 1. 抬手/下压效果：轻微下移再回位（模拟抽刀）
        float translateY = -0.2f * sinValue; 
        poseStack.translate(0, translateY, 0);

        // 2. 旋转效果：Z轴翻滚（手腕转动）+ X轴倾斜（刀刃翻转）
        float roll = -180f * sinValue; // 绕 Z 轴翻滚 180 度
        float pitch = -30f * sinValue; // 绕 X 轴倾斜 30 度

        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(roll)));
        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(pitch)));

        // 3. 向右轻微摆动，让动作更自然
        float translateX = 0.1f * sinValue;
        poseStack.translate(translateX, 0, 0);
    }

    private static float easeOutCubic(float t) { return 1 - (float) Math.pow(1 - t, 3); }
    private static float easeInOutQuad(float t) { return t < 0.5f ? 2*t*t : 1 - (float) Math.pow(-2*t + 2, 2) / 2; }
    private static float easeOutBack(float t) {
        float c1 = 1.70158f, c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    private static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
               stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) ||
               stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
    }
}
