package com.mossbot_addon.addon.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MossBotMixin {
    @Inject(at = @At("HEAD"), method = "init")
    private void onInit(CallbackInfo info) {
        // Mixin initialization hook
    }
}
