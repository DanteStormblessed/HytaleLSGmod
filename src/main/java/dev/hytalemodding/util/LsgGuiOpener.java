package dev.hytalemodding.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.session.LsgSessionManager;
import dev.hytalemodding.ui.LsgLoginPage;
import dev.hytalemodding.ui.LsgMainPage;

public final class LsgGuiOpener {

    private LsgGuiOpener() {}

    /**
     * Abre el panel correspondiente en el hilo del mundo:
     * Si el jugador ya ha iniciado sesión, abre LsgMainPage.
     * Si no ha iniciado sesión, abre LsgLoginPage.
     */
    public static void open(PlayerRef playerRef, LsgApiClient apiClient) {
        if (playerRef == null || playerRef.getWorldUuid() == null) return;

        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world != null) {
            world.execute(() -> {
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref != null && ref.isValid()) {
                    Store<EntityStore> store = ref.getStore();
                    Player player = store.getComponent(ref, Player.getComponentType());
                    if (player != null) {
                        if (LsgSessionManager.getInstance().hasSession(playerRef.getUuid())) {
                            player.getPageManager().openCustomPage(ref, store, new LsgMainPage(playerRef, apiClient));
                        } else {
                            player.getPageManager().openCustomPage(ref, store, new LsgLoginPage(playerRef, apiClient));
                        }
                    }
                }
            });
        }
    }

    /**
     * Abre de forma directa el panel principal (LsgMainPage) en el hilo del mundo.
     */
    public static void openMainPage(PlayerRef playerRef, LsgApiClient apiClient) {
        if (playerRef == null || playerRef.getWorldUuid() == null) return;

        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world != null) {
            world.execute(() -> {
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref != null && ref.isValid()) {
                    Store<EntityStore> store = ref.getStore();
                    Player player = store.getComponent(ref, Player.getComponentType());
                    if (player != null) {
                        player.getPageManager().openCustomPage(ref, store, new LsgMainPage(playerRef, apiClient));
                    }
                }
            });
        }
    }
}
