package dev.hytalemodding.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.ui.LsgLoginPage;

import javax.annotation.Nonnull;

public class LoginCommand extends AbstractPlayerCommand {

    private final LsgApiClient apiClient;

    public LoginCommand() {
        this(new LsgApiClient());
    }

    public LoginCommand(LsgApiClient apiClient) {
        super("login", "Abre la interfaz de inicio de sesión de LifeSync-Games");
        this.apiClient = apiClient;
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
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, new LsgLoginPage(playerRef, apiClient));
        } else {
            context.sendMessage(Message.raw("Este comando solo puede ser ejecutado por un jugador."));
        }
    }
}