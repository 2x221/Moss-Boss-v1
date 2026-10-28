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
import meteordevelopment.meteorclient.utils.ReflectInit;
import meteordevelopment.meteorclient.utils.render.PlayerHeadTexture;
import meteordevelopment.meteorclient.utils.render.PlayerHeadUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

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

        bypassMeteorPlayerHeadBug();

        Modules.get().add(new MossBot());
        Commands.add(new MossBotCommands());
        Hud.get().register(MossBotHUD.INFO);
    }

    private void bypassMeteorPlayerHeadBug() {
        try {
            // 1. Remove PlayerHeadUtils.init from Meteor's ReflectInit task list if stored
            for (Field f : ReflectInit.class.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(null);
                if (val instanceof List<?> list) {
                    list.removeIf(item -> item.toString().contains("PlayerHeadUtils"));
                }
            }
        } catch (Throwable ignored) {}

        try {
            // 2. Pre-populate PlayerHeadUtils.STEVE_HEAD with a blank instance so GUI never NPEs
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafe.get(null);

            PlayerHeadTexture dummy = (PlayerHeadTexture) unsafe.allocateInstance(PlayerHeadTexture.class);
            Field steveField = PlayerHeadUtils.class.getDeclaredField("STEVE_HEAD");
            steveField.setAccessible(true);
            steveField.set(null, dummy);
        } catch (Throwable ignored) {}
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
