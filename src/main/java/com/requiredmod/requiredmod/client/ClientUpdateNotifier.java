package com.requiredmod.requiredmod.client;

import com.requiredmod.requiredmod.RequiredMod;
import com.requiredmod.requiredmod.update.UpdateInfo;
import com.requiredmod.requiredmod.update.UpdateService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = RequiredMod.MOD_ID, value = Dist.CLIENT)
public final class ClientUpdateNotifier {
    private static final UpdateService UPDATE_SERVICE = new UpdateService();
    private static Optional<UpdateInfo> pendingUpdate = Optional.empty();
    private static boolean checkStarted = false;
    private static boolean promptShown = false;

    private ClientUpdateNotifier() {
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientUpdateNotifier::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        if (checkStarted) {
            return;
        }
        checkStarted = true;
        UPDATE_SERVICE.checkForUpdates(RequiredMod.MOD_VERSION).thenAccept(result -> {
            pendingUpdate = result;
            result.ifPresent(update -> RequiredMod.LOGGER.info("Update found: {}", update.version()));
        });
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (promptShown || mc.player != null || mc.level != null) {
            return;
        }

        if (mc.screen instanceof TitleScreen && pendingUpdate.isPresent()) {
            promptShown = true;
            mc.setScreen(new UpdatePromptScreen(mc.screen, pendingUpdate.get(), UPDATE_SERVICE));
        }
    }
}