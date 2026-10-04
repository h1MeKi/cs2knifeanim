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
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(modid = "cs2knifeanim", value = Dist.CLIENT)
public class RenderHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !isSword(event.getItemStack())) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        M9AnimationController controller = M9AnimationController.getInstance();
        int selectedSlot = player.getInventory().getSelectedSlot();

        if (controller.isFirstSwitch(player, event.getItemStack(), selectedSlot)) {
            float animationDuration = 0.6f;
            controller.startAnimation(animationDuration);
            controller.markSwitched(player, event.getItemStack(), selectedSlot);
        }

        float animProgress = controller.getAnimationProgress(event.getPartialTick());

        if (animProgress >= 0 && animProgress <= 1) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();

            float t = 1.0f - animProgress;
            float sinValue = (float) Math.sin(t * Math.PI);

            // 大幅度测试数值
            poseStack.translate(0, -0.5f * sinValue, 0);
            poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(-180f * sinValue)));
            poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(-90f * sinValue)));

            poseStack.popPose();
        }
    }

    private static boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
               stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) ||
               stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
    }
}
