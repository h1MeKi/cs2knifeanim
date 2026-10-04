package com.cs2knifeanim;

import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Cs2knifeanim.MODID)
public class Cs2knifeanim {
    public static final String MODID = "cs2knifeanim";

    public Cs2knifeanim() {
        // 纯客户
        if (FMLEnvironment.dist == Dist.CLIENT) {
            // 客户端初始化逻辑（如果有需要可以在这里写）
        }
    }
}