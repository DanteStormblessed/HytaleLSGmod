package dev.hytalemodding.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.models.LoginResponse;
import dev.hytalemodding.models.PlayerAttributePoints;
import dev.hytalemodding.session.LsgSessionManager;

import javax.annotation.Nonnull;

public class LsgMainPage extends InteractiveCustomUIPage<LsgMainPage.PageEventData> {

    private final LsgApiClient apiClient;

    public LsgMainPage(@Nonnull PlayerRef playerRef, @Nonnull LsgApiClient apiClient) {
        super(playerRef, CustomPageLifetime.CanDismiss, PageEventData.CODEC);
        this.apiClient = apiClient;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder commandBuilder,
            @Nonnull UIEventBuilder eventBuilder,
            @Nonnull Store<EntityStore> store
    ) {
        commandBuilder.append("Pages/LsgMain.ui");

        // Configurar saludo con datos de sesión
        LoginResponse session = LsgSessionManager.getInstance().getSession(playerRef.getUuid());
        if (session != null && session.getPlayer() != null) {
            String name = session.getPlayer().getName() != null ? session.getPlayer().getName() : playerRef.getUsername();
            String email = session.getPlayer().getEmail() != null ? session.getPlayer().getEmail() : "";
            commandBuilder.set("#PlayerGreetingLabel.Text", "Jugador: " + name + (!email.isBlank() ? " (" + email + ")" : ""));
        }

        // Navegación de pestañas
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabRewardsButton",
                EventData.of("Action", "TabRewards")
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabProfileButton",
                EventData.of("Action", "TabProfile")
        );

        // Canje de pociones especiales
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RedeemStaminaButton",
                EventData.of("Action", "RedeemStamina")
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RedeemHealthButton",
                EventData.of("Action", "RedeemHealth")
        );

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RedeemManaButton",
                EventData.of("Action", "RedeemMana")
        );

        // Botón de actualizar puntos en perfil
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#RefreshProfileButton",
                EventData.of("Action", "RefreshProfile")
        );

        // Botón de cierre nativo (la X de la ventana creada con @CloseButton = true)
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "Close")
        );

        // Cargar puntos en segundo plano al abrir
        fetchAndDisplayPoints();
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data
    ) {
        String action = data.getAction();
        if (action == null) return;

        System.out.println("[LSG Main UI] Evento recibido: Action=" + action);

        switch (action) {
            case "Close":
                this.close();
                break;

            case "TabRewards":
                showTab(true);
                break;

            case "TabProfile":
                showTab(false);
                fetchAndDisplayPoints();
                break;

            case "RefreshProfile":
                fetchAndDisplayPoints();
                break;

            case "RedeemStamina":
                processRedemption(ref, store, "Potion_Stamina_Infinite", "Poción de Stamina Infinita", 2, 50);
                break;

            case "RedeemHealth":
                processRedemption(ref, store, "Potion_Health_Infinite", "Poción de Vida Infinita", 2, 75);
                break;

            case "RedeemMana":
                processRedemption(ref, store, "Potion_Mana_Infinite", "Poción de Maná Infinito", 4, 50);
                break;

            default:
                System.out.println("[LSG Main UI] Acción no reconocida: " + action);
                break;
        }
    }

    private void showTab(boolean showRewards) {
        UICommandBuilder cmd = new UICommandBuilder();
        cmd.set("#TabRewardsContent.Visible", showRewards);
        cmd.set("#TabProfileContent.Visible", !showRewards);
        sendUpdate(cmd, false);
    }

    private void processRedemption(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            String itemId,
            String itemName,
            int attributeId,
            int cost
    ) {
        LoginResponse session = LsgSessionManager.getInstance().getSession(playerRef.getUuid());
        if (session == null || session.getPlayer() == null) {
            UICommandBuilder err = new UICommandBuilder();
            err.set("#RewardStatusLabel.Text", "No hay una sesión activa de LifeSync-Games.");
            sendUpdate(err, false);
            return;
        }

        UICommandBuilder loading = new UICommandBuilder();
        loading.set("#RewardStatusLabel.Text", "Procesando canje en LSG...");
        sendUpdate(loading, false);

        int playerId = session.getPlayer().getId();
        String token = session.getAccessToken();

        apiClient.adjustPlayerPoints(token, playerId, attributeId, "DEBIT", cost, "Canje en Hytale: " + itemName)
                .thenAccept(success -> {
                    com.hypixel.hytale.server.core.universe.world.World world = com.hypixel.hytale.server.core.universe.Universe.get().getWorld(playerRef.getWorldUuid());
                    if (world != null) {
                        world.execute(() -> {
                            Ref<EntityStore> entityRef = playerRef.getReference();
                            if (entityRef != null && entityRef.isValid()) {
                                Player.giveItem(new ItemStack(itemId, 1), entityRef, entityRef.getStore());
                            }
                        });
                    }
                    playerRef.sendMessage(dev.hytalemodding.util.LsgMessage.format("§a[LSG] ¡Has canjeado 1x " + itemName + " con éxito!"));

                    UICommandBuilder ok = new UICommandBuilder();
                    ok.set("#RewardStatusLabel.Text", "¡Canje exitoso! Se ha agregado la poción a tu inventario.");
                    sendUpdate(ok, false);

                    fetchAndDisplayPoints();
                })
                .exceptionally(ex -> {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    if (msg == null || msg.isBlank()) msg = "Error al procesar el canje de puntos.";

                    if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
                        msg = "Servidor LSG desconectado (localhost:8012). Verifica los servicios.";
                    }

                    System.err.println("[LSG Main UI] Error en canje: " + msg);
                    UICommandBuilder err = new UICommandBuilder();
                    err.set("#RewardStatusLabel.Text", dev.hytalemodding.util.LsgMessage.stripColor(msg));
                    sendUpdate(err, false);
                    return null;
                });
    }

    private void fetchAndDisplayPoints() {
        LoginResponse session = LsgSessionManager.getInstance().getSession(playerRef.getUuid());
        if (session == null || session.getPlayer() == null) {
            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#ProfileStatusLabel.Text", "Inicia sesión para ver tus dimensiones.");
            sendUpdate(cmd, false);
            return;
        }

        UICommandBuilder loading = new UICommandBuilder();
        loading.set("#ProfileStatusLabel.Text", "Consultando balance multidimensional...");
        sendUpdate(loading, false);

        int playerId = session.getPlayer().getId();
        String token = session.getAccessToken();

        apiClient.getPlayerAttributePoints(token, playerId)
                .thenAccept(pointsList -> {
                    int fisico = 0;
                    int mental = 0;
                    int social = 0;
                    int afectivo = 0;

                    if (pointsList != null) {
                        for (PlayerAttributePoints p : pointsList) {
                            String name = p.getAttributeName() != null ? p.getAttributeName().toLowerCase() : "";
                            int balance = p.getBalanceLedger();
                            if (name.contains("fisic") || p.getAttributeId() == 2) {
                                fisico = balance;
                            } else if (name.contains("ment") || p.getAttributeId() == 4) {
                                mental = balance;
                            } else if (name.contains("soc") || p.getAttributeId() == 1) {
                                social = balance;
                            } else if (name.contains("afect") || p.getAttributeId() == 3) {
                                afectivo = balance;
                            }
                        }
                    }

                    UICommandBuilder update = new UICommandBuilder();
                    update.set("#FisicoPointsLabel.Text", fisico + " pts");
                    update.set("#MentalPointsLabel.Text", mental + " pts");
                    update.set("#SocialPointsLabel.Text", social + " pts");
                    update.set("#AfectivoPointsLabel.Text", afectivo + " pts");
                    update.set("#ProfileStatusLabel.Text", "Puntos actualizados correctamente.");
                    sendUpdate(update, false);
                })
                .exceptionally(ex -> {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    if (msg == null || msg.isBlank()) msg = "Servidor de puntos offline.";
                    if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
                        msg = "Servidor LSG desconectado (localhost:8012).";
                    }

                    UICommandBuilder err = new UICommandBuilder();
                    err.set("#FisicoPointsLabel.Text", "-- pts");
                    err.set("#MentalPointsLabel.Text", "-- pts");
                    err.set("#SocialPointsLabel.Text", "-- pts");
                    err.set("#AfectivoPointsLabel.Text", "-- pts");
                    err.set("#ProfileStatusLabel.Text", "(" + dev.hytalemodding.util.LsgMessage.stripColor(msg) + ")");
                    sendUpdate(err, false);
                    return null;
                });
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(
                        new KeyedCodec<>("Action", Codec.STRING),
                        (data, str, extra) -> data.action = str,
                        (data, extra) -> data.action
                ).add()
                .build();

        private String action;

        public String getAction() {
            return action;
        }
    }
}
