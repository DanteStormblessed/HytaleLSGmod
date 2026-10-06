package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import dev.hytalemodding.api.LsgApiClient;

public class LsgCommandCollection extends AbstractCommandCollection {

    public LsgCommandCollection() {
        this(new LsgApiClient());
    }

    public LsgCommandCollection(LsgApiClient apiClient) {
        super("lsg", "Comandos principales del mod LifeSync-Games");
        requirePermission(HytalePermissions.fromCommand("Adventurer"));

        this.addSubCommand(new StatusCommand(apiClient));
        this.addSubCommand(new LoginCommand(apiClient));
        this.addSubCommand(new GuiCommand(apiClient));
        this.addSubCommand(new LogoutCommand());
    }
}
