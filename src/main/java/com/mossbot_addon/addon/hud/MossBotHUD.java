package com.mossbot_addon.addon.hud;

import com.mossbot_addon.addon.mossbot_addon;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;

public class MossBotHUD extends HudElement {
    public static final HudElementInfo<MossBotHUD> INFO = new HudElementInfo<>(mossbot_addon.HUD_GROUP, "mossbot-hud", "MossBot status HUD.", MossBotHUD::new);

    public MossBotHUD() {
        super(INFO);
    }
}
