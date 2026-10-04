package com.cs2knifeanim.mixin;

import com.cs2knifeanim.animation.M9AnimationController;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class M9BayonetAnimationMixin {

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"))
    private void onRenderHandsWithItems(float partialTick, PoseStack poseStack, AbstractClientPlayer player, InteractionHand hand, ItemStack stack, CallbackInfo ci) {
        if (hand != InteractionHand.MAIN_HAND || !isSword(stack)) return;

        M9AnimationController controller = M9AnimationController.getInstance();
        int selectedSlot = player.getInventory().getSelectedSlot();

        if (controller.isFirstSwitch(player, stack, selectedSlot)) {
            float animationDuration = 0.6f;
            controller.startAnimation(animationDuration);
            controller.markSwitched(player, stack, selectedSlot);
        }

        float animProgress = controller.getAnimationProgress(partialTick);

        if (animProgress >= 0 && animProgress <= 1) {
            float t = 1.0f - animProgress;
            float sinValue = (float) Math.sin(t * Math.PI);

            // 这里使用非常夸张的旋转，只要生效，画面一定会剧烈晃动
            float roll = -180f * sinValue;
            float pitch = -30f * sinValue;

            poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(roll)));
            poseStack.mulPose(new org.joml.Matrix4f().rotate(Axis.XP.rotationDegrees(pitch)));
        }
    }

    private boolean isSword(ItemStack stack) {
        return stack.is(Items.WOODEN_SWORD) || stack.is(Items.STONE_SWORD) ||
               stack.is(Items.GOLDEN_SWORD) || stack.is(Items.IRON_SWORD) ||
               stack.is(Items.DIAMOND_SWORD) || stack.is(Items.NETHERITE_SWORD);
    }
}
