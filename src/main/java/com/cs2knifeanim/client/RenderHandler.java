package com.cs2knifeanim.client;

import com.cs2knifeanim.animation.M9AnimationController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.InteractionHand;
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

        M9AnimationController controller = M9AnimationController.getInstance();
        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            // 应用M9刺刀动画变换
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

        // 位置偏移（抬手）
        poseStack.translate(0, -0.2 * (1 - raiseEase), 0);

        // 旋转（M9刺刀特有的旋转动作）
        float rotationAngle = -180 * spinEase + 360 * settleEase * 0.5f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
        poseStack.mulPose(Axis.XP.rotationDegrees(-30 * spinEase));

        // 微调稳定
        poseStack.translate(0.05 * settleEase, 0.05 * settleEase, 0);
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
