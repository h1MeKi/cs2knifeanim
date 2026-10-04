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
        // 阶段1: 抬手 (0.0 - 0.3)
        float raiseProgress = Math.min(progress / 0.3f, 1.0f);
        // 阶段2: 旋转 (0.3 - 0.7)
        float spinProgress = Math.max(0, Math.min((progress - 0.3f) / 0.4f, 1.0f));
        // 阶段3: 稳定 (0.7 - 1.0)
        float settleProgress = Math.max(0, (progress - 0.7f) / 0.3f);

        float raiseEase = easeOutCubic(raiseProgress);
        float spinEase = easeInOutQuad(spinProgress);
        float settleEase = easeOutBack(settleProgress);

        // 1. 抬手：向上移动 (Minecraft Y轴正方向向上)
        poseStack.translate(0, 0.3 * raiseEase, 0);

        // 2. 旋转：改用 Y轴 和 X轴，做出明显的刺刀翻转效果
        float yRotation = -120 * spinEase; // 绕Y轴旋转
        float xRotation = -45 * spinEase;  // 绕X轴旋转

        // 应用旋转（这里从 ZP 改成了 YP 和 XP）
        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.YP.rotationDegrees(yRotation)));
        poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(xRotation)));

        // 3. 稳定：微调
        poseStack.translate(0.1 * settleEase, 0, 0);
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
