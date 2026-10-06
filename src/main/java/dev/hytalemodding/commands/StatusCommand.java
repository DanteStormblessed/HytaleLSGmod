package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.models.LoginResponse;
import dev.hytalemodding.session.LsgSessionManager;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class StatusCommand extends AbstractAsyncCommand {

    private final LsgApiClient apiClient;

    public StatusCommand() {
        this(new LsgApiClient());
    }

    public StatusCommand(LsgApiClient apiClient) {
        super("status", "Verifica el estado de conexión con los servicios de LifeSync-Games");
        this.apiClient = apiClient;
        requirePermission(HytalePermissions.fromCommand("Adventurer"));
    }

    @Override
    protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
        context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§e[LSG] Verificando conexión con servicios locales de LifeSync-Games..."));

        CompletableFuture<String> authFuture = apiClient.checkAuthHealth()
                .exceptionally(e -> "Desconectado (" + e.getMessage() + ")");

        CompletableFuture<String> coreFuture = apiClient.checkCoreHealth()
                .exceptionally(e -> "Desconectado (" + e.getMessage() + ")");

        return CompletableFuture.allOf(authFuture, coreFuture).thenAccept(v -> {
            String authStatus = authFuture.join();
            String coreStatus = coreFuture.join();

            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§6========== [ Estado de Servicios LSG ] =========="));
            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§bLSG Auth (" + apiClient.getAuthBaseUrl() + "): " +
                    (authStatus.startsWith("OK") ? "§a" + authStatus : "§c" + authStatus)));
            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§bLSG Core API (" + apiClient.getCoreApiBaseUrl() + "): " +
                    (coreStatus.startsWith("OK") ? "§a" + coreStatus : "§c" + coreStatus)));

            if (context.isPlayer()) {
                PlayerRef playerRef = context.senderAs(PlayerRef.class);
                if (playerRef != null) {
                    LoginResponse session = LsgSessionManager.getInstance().getSession(playerRef.getUuid());
                    if (session != null && session.getPlayer() != null) {
                        context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§aSesión activa: " + session.getPlayer().getName() + " (" + session.getPlayer().getEmail() + ")"));
                        context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§7Roles: " + String.join(", ", session.getPlayer().getRoles())));
                    } else {
                        context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§7No has iniciado sesión. Usa §e/lsg login§7 para conectarte."));
                    }
                }
            }
            context.sendMessage(dev.hytalemodding.util.LsgMessage.format("§6==============================================="));
        });
    }
}