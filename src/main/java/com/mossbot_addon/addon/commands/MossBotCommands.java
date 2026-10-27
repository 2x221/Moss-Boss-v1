package com.mossbot_addon.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

public class MossBotCommands extends Command {

    public MossBotCommands() {
        super("mossbot", "Auto Moss Bot commands.");
    }

    @Override
    public void build(com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("name").then(argument("nameArgument", StringArgumentType.word()).executes(context -> {
            info("MossBot argument executed!");
            return SINGLE_SUCCESS;
        })));
    }
}
