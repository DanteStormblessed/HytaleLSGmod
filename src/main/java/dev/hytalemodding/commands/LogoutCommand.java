package dev.hytalemodding.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.models.LoginResponse;
import dev.hytalemodding.session.LsgSessionManager;

import javax.annotation.Nonnull;

public class LogoutCommand extends AbstractPlayerCommand {

    public LogoutCommand() {
        super("logout", "Cierra la sesión activa de LifeSync-Games");
        requirePermission(HytalePermissions.fromCommand("Adventurer"));
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        LoginResponse removed = LsgSessionManager.getInstance().clearSession(playerRef.getUuid());
        if (removed != null) {
            String name = (removed.getPlayer() != null && removed.getPlayer().getName() != null)
                    ? removed.getPlayer().getName()
                    : "Usuario";
            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§e[LSG] Sesión de " + name + " cerrada correctamente."));
        } else {
            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§7[LSG] No tenías ninguna sesión activa iniciada."));
        }
    }
}
