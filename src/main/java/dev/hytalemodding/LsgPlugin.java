package dev.hytalemodding;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.commands.LsgCommandCollection;
import dev.hytalemodding.config.LsgConfig;
import dev.hytalemodding.events.ExampleEvent;

import javax.annotation.Nonnull;

public class LsgPlugin extends JavaPlugin {

    private static Config<LsgConfig> config = null;
    private static LsgApiClient apiClient = null;

    public LsgPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        config = this.withConfig("lsg_config", LsgConfig.CODEC);
    }

    @Override
    protected void setup() {
        config.save();

        apiClient = new LsgApiClient(config.get().getAuthUrl(), config.get().getCoreApiUrl());

        // Registramos la colección de comandos de LifeSync-Games (/lsg ...)
        this.getCommandRegistry().registerCommand(new LsgCommandCollection(apiClient));
        // Registramos el comando rápido /l
        this.getCommandRegistry().registerCommand(new dev.hytalemodding.commands.LCommand(apiClient));

        // Listener de chat para abrir panel con la tecla / comando "L"
        this.getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent.class, event -> {
            String content = event.getContent();
            if (content != null) {
                String trimmed = content.trim();
                if (trimmed.equalsIgnoreCase("l") || trimmed.equalsIgnoreCase("/l")) {
                    event.setCancelled(true);
                    dev.hytalemodding.util.LsgGuiOpener.open(event.getSender(), apiClient);
                }
            }
        });

        if (getConfig().get().isEnabledWelcomeMessage()) {
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, ExampleEvent::onPlayerReady);
        }
    }

    public static Config<LsgConfig> getConfig() {
        return config;
    }

    public static LsgApiClient getApiClient() {
        return apiClient;
    }
}