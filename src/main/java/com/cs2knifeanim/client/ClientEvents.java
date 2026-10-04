package com.cs2knifeanim.client;

import com.cs2knifeanim.animation.M9AnimationController;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = "cs2knifeanim", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            M9AnimationController.getInstance().resetPlayer(player);
        }
    }
}
