package me.edoren.skin_changer.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.edoren.skin_changer.common.SharedPool;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.net.MalformedURLException;
import java.net.URL;

public class SkinsCommand {
    @FunctionalInterface
    interface Function<T, R> {
        R apply(T t) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface Function2<T, U, R> {
        R apply(T t, U u) throws CommandSyntaxException;
    }

    @FunctionalInterface
    interface Function3<T, U, V, R> {
        R apply(T t, U u, V v) throws CommandSyntaxException;
    }

    static final TextComponent ISSUER = new TextComponent("SkinChanger");

    public static ArgumentBuilder<CommandSourceStack, ?> setCommand(Function3<CommandSourceStack, Entity, String, Integer> setFunction,
                                                                    Function<CommandContext<CommandSourceStack>, Entity> getTarget) {
        return Commands.literal("set").then(Commands.argument("arg", MessageArgument.message()).executes((context) ->
                setFunction.apply(context.getSource(), getTarget.apply(context), MessageArgument.getMessage(context, "arg").getString())
        ));
    }

    public static ArgumentBuilder<CommandSourceStack, ?> cleanCommand(Function2<CommandSourceStack, Entity, Integer> cleanFunction,
                                                                      Function<CommandContext<CommandSourceStack>, Entity> getTarget) {
        return Commands.literal("clear").executes((context) ->
                cleanFunction.apply(context.getSource(), getTarget.apply(context))
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skin")
                .then(setCommand(
                        SkinsCommand::setPlayerSkin,
                        (context) -> context.getSource().getEntityOrException())
                )
                .then(cleanCommand(
                        SkinsCommand::cleanPlayerSkin,
                        (context) -> context.getSource().getEntityOrException())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermission(2))  // TODO: Check this
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(setCommand(
                                        SkinsCommand::setPlayerSkin,
                                        (context) -> EntityArgument.getEntity(context, "target"))
                                )
                                .then(cleanCommand(
                                        SkinsCommand::cleanPlayerSkin,
                                        (context) -> EntityArgument.getEntity(context, "target"))
                                )
                        )
                )
        );
        dispatcher.register(Commands.literal("cape")
                .then(setCommand(
                        SkinsCommand::setPlayerCape,
                        (context) -> context.getSource().getEntityOrException())
                )
                .then(cleanCommand(
                        SkinsCommand::cleanPlayerCape,
                        (context) -> context.getSource().getEntityOrException())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermission(2)) // TODO: Check this
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(setCommand(
                                        SkinsCommand::setPlayerCape,
                                        (context) -> context.getSource().getEntityOrException())
                                )
                                .then(cleanCommand(
                                        SkinsCommand::cleanPlayerCape,
                                        (context) -> context.getSource().getEntityOrException())
                                )
                        )
                )
        );
    }

    private static Integer setPlayerSkin(CommandSourceStack source, Entity targetEntity, String arg) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerSkinByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerSkinByName(sourcePlayer, targetPlayer, arg);
        }
    }

    private static Integer setPlayerSkinByName(Player sourcePlayer, Player targetPlayer, String playerName) {
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Loading skin...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkinByName(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Could not load the skin")), Util.NIL_UUID);
            } else {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Skin loaded successfully")), Util.NIL_UUID);
            }
        });
        return 1;
    }

    private static Integer setPlayerSkinByURL(Player sourcePlayer, Player targetPlayer, URL url) {
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Loading skin...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkinByURL(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Could not load the skin")), Util.NIL_UUID);
            } else {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Skin loaded successfully")), Util.NIL_UUID);
            }
        });
        return 1;
    }

    private static Integer setPlayerCape(CommandSourceStack source, Entity targetEntity, String arg) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerCapeByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerCapeByName(sourcePlayer, targetPlayer, arg);
            // sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Could not parse " + urlString + " as url")));
        }
    }

    private static Integer setPlayerCapeByName(Player sourcePlayer, Player targetPlayer, String playerName) {
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Loading cape...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCapeByName(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Could not load the cape")), Util.NIL_UUID);
            } else {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Cape loaded successfully")), Util.NIL_UUID);
            }
        });
        return 1;
    }

    private static Integer setPlayerCapeByURL(Player sourcePlayer, Player targetPlayer, URL url) {
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Loading cape...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCapeByURL(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Could not load the cape")), Util.NIL_UUID);
            } else {
                sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Cape loaded successfully")), Util.NIL_UUID);
            }
        });
        return 1;
    }

    private static Integer cleanPlayerSkin(CommandSourceStack source, Entity targetEntity) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Removing skin...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().cleanPlayerSkin(targetPlayer.getGameProfile());
            sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Skin removed successfully")), Util.NIL_UUID);
        });
        return 1;
    }

    private static Integer cleanPlayerCape(CommandSourceStack source, Entity targetEntity) throws CommandSyntaxException {
        Player sourcePlayer = (Player) source.getEntityOrException();
        Player targetPlayer = (Player) targetEntity;
        sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Removing cape...")), Util.NIL_UUID);
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().cleanPlayerCape(targetPlayer.getGameProfile());
            sourcePlayer.sendMessage(new TranslatableComponent("chat.type.announcement", ISSUER, new TextComponent("Cape removed successfully")), Util.NIL_UUID);
        });
        return 1;
    }
}