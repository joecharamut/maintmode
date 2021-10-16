package rocks.spaghetti.maintmode;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;

import java.util.ArrayList;

import static net.minecraft.command.argument.GameProfileArgumentType.gameProfile;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ServerMain implements DedicatedServerModInitializer {
    private boolean maintModeEnabled = false;
    private ArrayList<GameProfile> whitelistedProfiles = new ArrayList<>();

    @Override
    public void onInitializeServer() {
        CanJoinCallback.EVENT.register((address, profile) -> {
            if (maintModeEnabled) {
                if (whitelistedProfiles.contains(profile)) {
                    return ActionResult.PASS;
                }
                return ActionResult.FAIL;
            } else {
                return ActionResult.PASS;
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal("maint")
                    .requires(src -> src.hasPermissionLevel(4))

                    .then(literal("on").executes(ctx -> {
                        maintModeEnabled = true;

                        PlayerManager playerManager = ctx.getSource().getServer().getPlayerManager();
                        for (ServerPlayerEntity player : playerManager.getPlayerList()) {
                            if (!whitelistedProfiles.contains(player.getGameProfile())) {
                                player.networkHandler.disconnect(new TranslatableText("multiplayer.disconnect.not_whitelisted"));
                            }
                        }

                        ctx.getSource().sendFeedback(new LiteralText("Maintenance mode enabled"), true);
                        return Command.SINGLE_SUCCESS;
                    }))

                    .then(literal("off").executes(ctx -> {
                        maintModeEnabled = false;

                        ctx.getSource().sendFeedback(new LiteralText("Maintenance mode disabled"), true);
                        return Command.SINGLE_SUCCESS;
                    }))

                    .then(literal("allow").then(argument("username", gameProfile()).executes(ctx -> {
                        GameProfile profile = GameProfileArgumentType.getProfileArgument(ctx, "username").stream().findFirst().orElseThrow();
                        whitelistedProfiles.add(profile);

                        ctx.getSource().sendFeedback(new LiteralText(String.format("Player '%s' added to maintenance whitelist", profile.getName())), true);
                        return Command.SINGLE_SUCCESS;
                    })))
            );
        });
    }
}
