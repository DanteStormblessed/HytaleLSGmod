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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.api.LsgApiClient;
import dev.hytalemodding.session.LsgSessionManager;

import javax.annotation.Nonnull;

public class LsgLoginPage extends InteractiveCustomUIPage<LsgLoginPage.PageEventData> {

    private final LsgApiClient apiClient;

    public LsgLoginPage(@Nonnull PlayerRef playerRef, @Nonnull LsgApiClient apiClient) {
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
        commandBuilder.append("Pages/LsgLogin.ui");

        // Evento al presionar botón Iniciar Sesión
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#LoginButton",
                EventData.of("Action", "Login")
                        .append("@Username", "#UsernameField.Value")
                        .append("@Password", "#PasswordField.Value")
        );

        // Evento al presionar Enter en el campo de contraseña
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Validating,
                "#PasswordField",
                EventData.of("Action", "Login")
                        .append("@Username", "#UsernameField.Value")
                        .append("@Password", "#PasswordField.Value")
        );

        // Evento al presionar botón Cancelar
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelButton",
                EventData.of("Action", "Cancel")
        );

        // Evento al presionar el botón X de la ventana
        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CloseButton",
                EventData.of("Action", "Cancel")
        );
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull String rawData
    ) {
        System.out.println("[LSG UI] ==================================================");
        System.out.println("[LSG UI] RAW JSON RECIBIDO DEL CLIENTE: " + rawData);
        super.handleDataEvent(ref, store, rawData);
    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull PageEventData data
    ) {
        System.out.println("[LSG UI] Evento decodificado: Action=" + data.getAction());
        if ("Cancel".equalsIgnoreCase(data.getAction())) {
            this.close();
            return;
        }

        if ("Login".equalsIgnoreCase(data.getAction())) {
            String username = data.getUsername();
            String password = data.getPassword();

            System.out.println("[LSG UI] Procesando login: Username='" + username + "'" +
                    ", Password length=" + (password != null ? password.length() : "null") +
                    ", Es solo asteriscos? " + (password != null && password.matches("^\\*+$")));

            final String finalUsername = username != null ? username.trim() : "";
            final String finalPassword = password;

            if (finalUsername.isEmpty() || finalPassword == null || finalPassword.trim().isEmpty()) {
                UICommandBuilder err = new UICommandBuilder();
                err.set("#StatusLabel.Text", "Por favor ingresa usuario y contrasena.");
                sendUpdate(err, false);
                return;
            }

            // Notificamos estado de carga en la UI
            UICommandBuilder loading = new UICommandBuilder();
            loading.set("#StatusLabel.Text", "Conectando con LSG...");
            sendUpdate(loading, false);

            apiClient.login(finalUsername, finalPassword)
                    .thenAccept(loginResponse -> {
                        String displayName = (loginResponse.getPlayer() != null && loginResponse.getPlayer().getName() != null)
                                ? loginResponse.getPlayer().getName()
                                : finalUsername;

                        // Guardamos la sesión activa en memoria
                        LsgSessionManager.getInstance().setSession(playerRef.getUuid(), loginResponse);

                        // Enviamos mensaje de éxito al chat del juego usando formato nativo Hytale
                        playerRef.sendMessage(dev.hytalemodding.util.LsgMessage.format("§a[LSG] ¡Sesion iniciada con exito! Bienvenido, " + displayName + "."));

                        UICommandBuilder ok = new UICommandBuilder();
                        ok.set("#StatusLabel.Text", "¡Autenticado correctamente!");
                        sendUpdate(ok, false);

                        // Abrimos automáticamente el panel principal de LifeSync-Games de forma segura en el hilo del mundo
                        dev.hytalemodding.util.LsgGuiOpener.openMainPage(playerRef, apiClient);
                    })
                    .exceptionally(ex -> {
                        String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                        if (msg == null || msg.isBlank()) msg = "Error en la autenticacion.";

                        System.err.println("[LSG UI] Error recibido en callback: " + msg);
                        UICommandBuilder err = new UICommandBuilder();
                        err.set("#StatusLabel.Text", dev.hytalemodding.util.LsgMessage.stripColor(msg));
                        sendUpdate(err, false);
                        return null;
                    });
        }
    }

    public static class PageEventData {
        public static final BuilderCodec<PageEventData> CODEC = BuilderCodec.builder(PageEventData.class, PageEventData::new)
                .append(
                        new KeyedCodec<>("Action", Codec.STRING),
                        (data, str, extra) -> data.action = str,
                        (data, extra) -> data.action
                ).add()
                .append(
                        new KeyedCodec<>("@Username", Codec.STRING),
                        (data, str, extra) -> data.username = str,
                        (data, extra) -> data.username
                ).add()
                .append(
                        new KeyedCodec<>("@Password", Codec.STRING),
                        (data, str, extra) -> data.password = str,
                        (data, extra) -> data.password
                ).add()
                .build();

        private String action;
        private String username;
        private String password;

        public String getAction() { return action; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}
