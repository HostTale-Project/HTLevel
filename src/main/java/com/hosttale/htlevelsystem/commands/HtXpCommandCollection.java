package com.hosttale.htlevelsystem.commands;

import com.hosttale.htlevelsystem.experience.ExperienceService;
import com.hosttale.htlevelsystem.experience.ExperienceSnapshot;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Admin command collection for testing and managing player XP/levels.
 * Usage: /htxp {@literal <}get|setxp|givexp|takexp|setlvl|givelvl|takelvl{@literal >} --player <player> [amount]
 */
public final class HtXpCommandCollection extends AbstractCommandCollection {
    private static final String PERM = "htlevel.admin";

    public HtXpCommandCollection(ExperienceService service) {
        super("htxp", "HTLevelSystem admin XP tools");
        addAliases("htlevel", "htlvl");

        addSubCommand(new GetCommand(service));
        addSubCommand(new SetXpCommand(service));
        addSubCommand(new GiveXpCommand(service));
        addSubCommand(new TakeXpCommand(service));
        addSubCommand(new SetLevelCommand(service));
        addSubCommand(new GiveLevelCommand(service));
        addSubCommand(new TakeLevelCommand(service));
    }

    private abstract static class BaseXpCommand extends AbstractAsyncCommand {
        protected final ExperienceService service;
        protected final RequiredArg<com.hypixel.hytale.server.core.universe.PlayerRef> playerArg;

        protected BaseXpCommand(String name, String description, ExperienceService service) {
            super(name, description);
            this.service = service;
            this.playerArg = withRequiredArg("player", "Target player", ArgTypes.PLAYER_REF);
            requirePermission(HytalePermissions.fromCommand(PERM));
        }

        protected ExperienceSnapshot snapshot(CommandContext ctx) {
            UUID id = resolvePlayerId(ctx);
            return service.getOrCreate(id);
        }

        protected long toLong(int value) {
            return (long) value;
        }

        protected UUID resolvePlayerId(CommandContext ctx) {
            com.hypixel.hytale.server.core.universe.PlayerRef ref = playerArg.get(ctx);
            if (ref == null || ref.getUuid() == null) {
                throw new IllegalArgumentException("Could not resolve player");
            }
            return ref.getUuid();
        }

        protected UUID safeResolve(CommandContext ctx) {
            try {
                return resolvePlayerId(ctx);
            } catch (IllegalArgumentException ex) {
                ctx.sendMessage(Message.raw(ex.getMessage()));
                return null;
            }
        }

        protected java.util.concurrent.CompletableFuture<Void> done() {
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
    }

    private static final class GetCommand extends BaseXpCommand {
        GetCommand(ExperienceService service) {
            super("get", "Show XP and level for a player", service);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            ExperienceSnapshot snap = snapshot(context);
            if (snap == null) return done();
            context.sendMessage(Message.raw(
                String.format("XP: %d | Level: %d (max: %s)",
                    snap.experience(),
                    snap.level(),
                    service.getMaxLevel() > 0 ? service.getMaxLevel() : "uncapped"
                )
            ));
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }
    }

    private static final class SetXpCommand extends BaseXpCommand {
        private final RequiredArg<Integer> amount;

        SetXpCommand(ExperienceService service) {
            super("setxp", "Set absolute XP for a player", service);
            this.amount = withRequiredArg("amount", "XP amount", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            long value = toLong(amount.get(context));
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot snap = service.setExperience(playerId, value);
            context.sendMessage(Message.raw(
                String.format("Set XP to %d (Level %d)", snap.experience(), snap.level())
            ));
            return done();
        }
    }

    private static final class GiveXpCommand extends BaseXpCommand {
        private final RequiredArg<Integer> amount;

        GiveXpCommand(ExperienceService service) {
            super("givexp", "Give XP to a player", service);
            this.amount = withRequiredArg("amount", "XP amount", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            long delta = toLong(amount.get(context));
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot snap = service.addExperience(playerId, delta);
            context.sendMessage(Message.raw(
                String.format("Added %d XP -> total %d (Level %d)", delta, snap.experience(), snap.level())
            ));
            return done();
        }
    }

    private static final class TakeXpCommand extends BaseXpCommand {
        private final RequiredArg<Integer> amount;

        TakeXpCommand(ExperienceService service) {
            super("takexp", "Remove XP from a player", service);
            this.amount = withRequiredArg("amount", "XP amount", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            long delta = -Math.abs(toLong(amount.get(context)));
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot snap = service.addExperience(playerId, delta);
            context.sendMessage(Message.raw(
                String.format("Removed %d XP -> total %d (Level %d)", Math.abs(delta), snap.experience(), snap.level())
            ));
            return done();
        }
    }

    private static final class SetLevelCommand extends BaseXpCommand {
        private final RequiredArg<Integer> levelArg;

        SetLevelCommand(ExperienceService service) {
            super("setlvl", "Set absolute level for a player", service);
            this.levelArg = withRequiredArg("level", "Target level", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            int targetLevel = levelArg.get(context);
            long xp = service.experienceForLevel(targetLevel);
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot snap = service.setExperience(playerId, xp);
            context.sendMessage(Message.raw(
                String.format("Set level to %d (XP %d)", snap.level(), snap.experience())
            ));
            return done();
        }
    }

    private static final class GiveLevelCommand extends BaseXpCommand {
        private final RequiredArg<Integer> levelArg;

        GiveLevelCommand(ExperienceService service) {
            super("givelvl", "Increase player level", service);
            this.levelArg = withRequiredArg("levels", "Levels to add", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            ExperienceSnapshot before = snapshot(context);
            if (before == null) return done();
            int targetLevel = before.level() + levelArg.get(context);
            long xp = service.experienceForLevel(targetLevel);
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot after = service.setExperience(playerId, xp);
            context.sendMessage(Message.raw(
                String.format("Level +%d -> Level %d (XP %d)", after.level() - before.level(), after.level(), after.experience())
            ));
            return done();
        }
    }

    private static final class TakeLevelCommand extends BaseXpCommand {
        private final RequiredArg<Integer> levelArg;

        TakeLevelCommand(ExperienceService service) {
            super("takelvl", "Decrease player level", service);
            this.levelArg = withRequiredArg("levels", "Levels to remove", ArgTypes.INTEGER);
        }

        @Override
        protected java.util.concurrent.CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
            ExperienceSnapshot before = snapshot(context);
            if (before == null) return done();
            int targetLevel = Math.max(0, before.level() - levelArg.get(context));
            long xp = service.experienceForLevel(targetLevel);
            UUID playerId = safeResolve(context);
            if (playerId == null) return done();
            ExperienceSnapshot after = service.setExperience(playerId, xp);
            context.sendMessage(Message.raw(
                String.format("Level -%d -> Level %d (XP %d)", before.level() - after.level(), after.level(), after.experience())
            ));
            return done();
        }
    }
}
