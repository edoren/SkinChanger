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
            ClientLifecycleEvent.CLIENT_STOPPING.register(this::onClientStopping);
        }
        LifecycleEvent.SERVER_STARTED.register(this::onServerStarted);
        LifecycleEvent.SERVER_STOPPED.register(this::onServerStopped);
        CommandRegistrationEvent.EVENT.register(this::onCommandRegistration);
    }

    void onSetup() {
        NetworkContext.GetInstance().initialize();
    }

    void onClientSetup(Minecraft client) {
        ClientController.GetInstance().initialize();
    }

    void onClientStopping(Minecraft client) {
        ClientController.GetInstance().deinitialize();
    }

    void onServerStarted(MinecraftServer server) {
        ServerController.GetInstance().initialize(server);
    }

    void onServerStopped(MinecraftServer server) {
        ServerController.GetInstance().deinitialize(server);
    }

    void onCommandRegistration(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        SkinsCommand.register(commandSourceStackCommandDispatcher);
    }
}
