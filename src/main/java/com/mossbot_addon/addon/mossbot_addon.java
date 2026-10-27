package com.mossbot_addon.addon;

import com.mossbot_addon.addon.commands.MossBotCommands;
import com.mossbot_addon.addon.hud.MossBotHUD;
import com.mossbot_addon.addon.modules.MossBot;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class mossbot_addon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Moss Boss");

    public static final Category CATEGORY = new Category("Moss Boss", Items.MOSS_BLOCK.getDefaultStack());
    public static final HudGroup HUD_GROUP = new HudGroup("Moss Boss");

    public static final Color ADDON_COLOR = new Color(141, 13, 255);

    public mossbot_addon() {
        this.name = "Moss Boss by JerkinOffInMyCar";
        this.authors = new String[]{"JerkinOffInMyCar"};
    }

    @Override
    public void onInitialize() {
        this.name = "Moss Boss by JerkinOffInMyCar";
        this.authors = new String[]{"JerkinOffInMyCar"};

        LOG.info("Initializing Moss Boss by JerkinOffInMyCar");

        Modules.get().add(new MossBot());
        Commands.add(new MossBotCommands());
        Hud.get().register(MossBotHUD.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.mossbot_addon.addon";
    }
}
