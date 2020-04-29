package me.edoren.skin_changer.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.edoren.skin_changer.common.SharedPool;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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

    static final ITextComponent ISSUER = new StringTextComponent("SkinChanger");

    public static ArgumentBuilder<CommandSource, ?> setCommand(Function3<CommandSource, Entity, String, Integer> setFunction,
                                                               Function<CommandContext<CommandSource>, Entity> getTarget) {
        return Commands.literal("set").then(Commands.argument("arg", MessageArgument.message()).executes((context) ->
                setFunction.apply(context.getSource(), getTarget.apply(context), MessageArgument.getMessage(context, "arg").getString())
        ));
    }

    public static ArgumentBuilder<CommandSource, ?> cleanCommand(Function2<CommandSource, Entity, Integer> cleanFunction,
                                                                 Function<CommandContext<CommandSource>, Entity> getTarget) {
        return Commands.literal("clear").executes((context) ->
                cleanFunction.apply(context.getSource(), getTarget.apply(context))
        );
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("skin")
                .then(setCommand(
                        SkinsCommand::setPlayerSkin,
                        (context) -> context.getSource().assertIsEntity())
                )
                .then(cleanCommand(
                        SkinsCommand::cleanPlayerSkin,
                        (context) -> context.getSource().assertIsEntity())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermissionLevel(2))
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
                        (context) -> context.getSource().assertIsEntity())
                )
                .then(cleanCommand(
                        SkinsCommand::cleanPlayerCape,
                        (context) -> context.getSource().assertIsEntity())
                )
                .then(Commands.literal("player")
                        .requires((player) -> player.hasPermissionLevel(2))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(setCommand(
                                        SkinsCommand::setPlayerCape,
                                        (context) -> context.getSource().assertIsEntity())
                                )
                                .then(cleanCommand(
                                        SkinsCommand::cleanPlayerCape,
                                        (context) -> context.getSource().assertIsEntity())
                                )
                        )
                )
        );
    }

    private static Integer setPlayerSkin(CommandSource source, Entity targetEntity, String arg) throws CommandSyntaxException {
        PlayerEntity sourcePlayer = (PlayerEntity) source.assertIsEntity();
        PlayerEntity targetPlayer = (PlayerEntity) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerSkinByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerSkinByName(sourcePlayer, targetPlayer, arg);
        }
    }

    private static Integer setPlayerSkinByName(PlayerEntity sourcePlayer, PlayerEntity targetPlayer, String playerName) {
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Loading skin...")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkin(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Could not load the skin")));
            } else {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Skin loaded successfully")));
            }
        });
        return 1;
    }

    private static Integer setPlayerSkinByURL(PlayerEntity sourcePlayer, PlayerEntity targetPlayer, URL url) {
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Loading skin...")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerSkin(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Could not load the skin")));
            } else {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Skin loaded successfully")));
            }
        });
        return 1;
    }

    private static Integer setPlayerCape(CommandSource source, Entity targetEntity, String arg) throws CommandSyntaxException {
        PlayerEntity sourcePlayer = (PlayerEntity) source.assertIsEntity();
        PlayerEntity targetPlayer = (PlayerEntity) targetEntity;
        try {
            URL url = new URL(arg);
            return setPlayerCapeByURL(sourcePlayer, targetPlayer, url);
        } catch (MalformedURLException e) {
            return setPlayerCapeByName(sourcePlayer, targetPlayer, arg);
            // sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Could not parse " + urlString + " as url")));
        }
    }

    private static Integer setPlayerCapeByName(PlayerEntity sourcePlayer, PlayerEntity targetPlayer, String playerName) {
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Loading cape...")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCape(targetPlayer.getGameProfile(), playerName, true)) {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Could not load the cape")));
            } else {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Cape loaded successfully")));
            }
        });
        return 1;
    }

    private static Integer setPlayerCapeByURL(PlayerEntity sourcePlayer, PlayerEntity targetPlayer, URL url) {
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Loading cape...")));
        SharedPool.get().execute(() -> {
            if (!SkinProviderController.GetInstance().setPlayerCape(targetPlayer.getGameProfile(), url, true)) {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Could not load the cape")));
            } else {
                sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Cape loaded successfully")));
            }
        });
        return 1;
    }

    private static Integer cleanPlayerSkin(CommandSource source, Entity targetEntity) throws CommandSyntaxException {
        PlayerEntity sourcePlayer = (PlayerEntity) source.assertIsEntity();
        PlayerEntity targetPlayer = (PlayerEntity) targetEntity;
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Removing skin...")));
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().cleanPlayerSkin(targetPlayer.getGameProfile());
            sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Skin removed successfully")));
        });
        return 1;
    }

    private static Integer cleanPlayerCape(CommandSource source, Entity targetEntity) throws CommandSyntaxException {
        PlayerEntity sourcePlayer = (PlayerEntity) source.assertIsEntity();
        PlayerEntity targetPlayer = (PlayerEntity) targetEntity;
        sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Removing cape...")));
        SharedPool.get().execute(() -> {
            SkinProviderController.GetInstance().cleanPlayerCape(targetPlayer.getGameProfile());
            sourcePlayer.sendMessage(new TranslationTextComponent("chat.type.announcement", ISSUER, new StringTextComponent("Cape removed successfully")));
        });
        return 1;
    }
}