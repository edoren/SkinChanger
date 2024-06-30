package me.edoren.skin_changer.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.edoren.skin_changer.common.SharedPool;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("SameReturnValue")
public class SkinsCommand {
    static final Component ISSUER = Component.literal("SkinChanger");

    public static ArgumentBuilder<CommandSourceStack, ?> setCommand(Function3<CommandSourceStack, Player, String, Integer> setFunction,
                                                                    Function<CommandContext<CommandSourceStack>, Player> getTarget) {
        return Commands.literal("set").then(Commands.argument("arg", MessageArgument.message()).executes((context) ->
                setFunction.apply(context.getSource(), getTarget.apply(context), MessageArgument.getMessage(context, "arg").getString())
        ));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> clearCommand(Function2<CommandSourceStack, Player, Integer> clearFunction,
                                                                      Function<CommandContext<CommandSourceStack>, Player> getTarget) {
        return Commands.literal("clear").executes((context) ->
                clearFunction.apply(context.getSource(), getTarget.apply(context))
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skin")
                .then(setCommand(
                        SkinsCommand::setPlayerSkin,
                        (context) -> context.getSource().getPlayerOrException())
                )
                .then(clearCommand(
                        SkinsCommand::clearPlayerSkin,
                        (context) -> context.getSource().getPlayerOrException())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(setCommand(
                                        SkinsCommand::setPlayerSkin,
                                        (context) -> EntityArgument.getPlayer(context, "target"))
                                )
                                .then(clearCommand(
                                        SkinsCommand::clearPlayerSkin,
                                        (context) -> EntityArgument.getPlayer(context, "target"))
                                )
                        )
                )
        );
        dispatcher.register(Commands.literal("cape")
                .then(setCommand(
                        SkinsCommand::setPlayerCape,
                        (context) -> context.getSource().getPlayerOrException())
                )
                .then(clearCommand(
                        SkinsCommand::clearPlayerCape,
                        (context) -> context.getSource().getPlayerOrException())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(setCommand(
                                        SkinsCommand::setPlayerCape,
                                        (context) -> EntityArgument.getPlayer(context, "target"))
                                )
                                .then(clearCommand(
                                        SkinsCommand::clearPlayerCape,
                                        (context) -> EntityArgument.getPlayer(context, "target"))
                                )
                        )
                )
        );
    }

    private static Integer setPlayerSkin(CommandSourceStack source, Entity targetEntity, String arg) {
        CommandSource sourcePlayer = source.getServer();
        if (source.getEntity() != null) {
            sourcePlayer = source.getEntity();
        }
        Player targetPlayer = (Player) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerSkinByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerSkinByName(sourcePlayer, targetPlayer, arg);
        }
    }

    private static Integer setPlayerSkinByName(CommandSource sourcePlayer, Player targetPlayer, String playerName) {
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.loading")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkinByName(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.load_failed")));
            } else {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.load_succeeded")));
            }
        });
        return 1;
    }

    private static Integer setPlayerSkinByURL(CommandSource sourcePlayer, Player targetPlayer, URL url) {
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.loading")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkinByURL(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.load_failed")));
            } else {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.load_succeeded")));
            }
        });
        return 1;
    }

    private static Integer setPlayerCape(CommandSourceStack source, Entity targetEntity, String arg) {
        CommandSource sourcePlayer = source.getServer();
        if (source.getEntity() != null) {
            sourcePlayer = source.getEntity();
        }
        Player targetPlayer = (Player) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerCapeByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerCapeByName(sourcePlayer, targetPlayer, arg);
        }
    }

    private static Integer setPlayerCapeByName(CommandSource sourcePlayer, Player targetPlayer, String playerName) {
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.loading")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCapeByName(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.load_failed")));
            } else {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.load_succeeded")));
            }
        });
        return 1;
    }

    private static Integer setPlayerCapeByURL(CommandSource sourcePlayer, Player targetPlayer, URL url) {
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.loading")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCapeByURL(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.load_failed")));
            } else {
                sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.load_succeeded")));
            }
        });
        return 1;
    }

    private static Integer clearPlayerSkin(CommandSourceStack source, Entity targetEntity) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.removing")));
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().clearPlayerSkin(targetPlayer.getGameProfile());
            sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.skin.remove_succeeded")));
        });
        return 1;
    }

    private static Integer clearPlayerCape(CommandSourceStack source, Entity targetEntity) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.removing")));
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().clearPlayerCape(targetPlayer.getGameProfile());
            sourcePlayer.sendSystemMessage(Component.translatable("chat.type.announcement", ISSUER, Component.translatable("commands.skin_changer.cape.remove_succeeded")));
        });
        return 1;
    }

    @FunctionalInterface
    public interface Function<T, R> {
        R apply(T t) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface Function2<T, U, R> {
        R apply(T t, U u) throws CommandSyntaxException;
    }

    @FunctionalInterface
    public interface Function3<T, U, V, R> {
        R apply(T t, U u, V v) throws CommandSyntaxException;
    }
}