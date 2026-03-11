package com.requiredmod.requiredmod;

import com.mojang.logging.LogUtils;
import com.requiredmod.requiredmod.client.ClientUpdateNotifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(RequiredMod.MOD_ID)
public class RequiredMod {
    public static final String MOD_ID = "requiredmod";
    public static final String MOD_VERSION = "0.1.0";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RequiredMod() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientUpdateNotifier::init);
    }