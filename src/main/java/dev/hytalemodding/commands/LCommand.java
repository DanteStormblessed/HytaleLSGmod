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
import dev.hytalemodding.session.LsgSessionManager;
import dev.hytalemodding.ui.LsgLoginPage;
import dev.hytalemodding.ui.LsgMainPage;

import javax.annotation.Nonnull;

public class LCommand extends AbstractPlayerCommand {

    private final LsgApiClient apiClient;

    public LCommand(LsgApiClient apiClient) {
        super("l", "Abre el panel de LifeSync-Games (Login o Principal)");
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
            if (LsgSessionManager.getInstance().hasSession(playerRef.getUuid())) {
                player.getPageManager().openCustomPage(ref, store, new LsgMainPage(playerRef, apiClient));
            } else {
                player.getPageManager().openCustomPage(ref, store, new LsgLoginPage(playerRef, apiClient));
            }
        } else {
            context.sendMessage(Message.raw("Este comando solo puede ser ejecutado por un jugador."));
        }
    }
}
