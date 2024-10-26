package me.edoren.skin_changer;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import me.edoren.skin_changer.client.ClientController;
import me.edoren.skin_changer.common.NetworkContext;
import me.edoren.skin_changer.server.ServerController;
import me.edoren.skin_changer.server.SkinsCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;

public final class SkinChanger {
    public SkinChanger() {
        LifecycleEvent.SETUP.register(this::onSetup);
        if (Platform.getEnvironment() == Env.CLIENT) {
            ClientLifecycleEvent.CLIENT_SETUP.register(this::onClientSetup);
        }
        LifecycleEvent.SERVER_STARTED.register(this::onServerStarted);
        CommandRegistrationEvent.EVENT.register(this::onCommandRegistration);
    }

    void onSetup() {
        NetworkContext.GetInstance().initialize();
    }

    void onClientSetup(Minecraft client) {
        ClientController.GetInstance().initialize();
    }

    void onServerStarted(MinecraftServer server) {
        ServerController.GetInstance().initialize(server);
    }

    void onCommandRegistration(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        SkinsCommand.register(commandSourceStackCommandDispatcher);
    }
}
