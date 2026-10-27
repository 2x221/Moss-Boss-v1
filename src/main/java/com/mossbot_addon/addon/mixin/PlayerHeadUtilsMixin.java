package com.mossbot_addon.addon.mixin;

import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerHeadUtils.class, remap = false)
public class PlayerHeadUtilsMixin {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private static void cancelHeadInit(CallbackInfo ci) {
        ci.cancel();
    }
}
