package dev.hytalemodding.session;

import dev.hytalemodding.models.LoginResponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LsgSessionManager {

    private static final LsgSessionManager INSTANCE = new LsgSessionManager();

    private final Map<UUID, LoginResponse> activeSessions = new ConcurrentHashMap<>();

    private LsgSessionManager() {}

    public static LsgSessionManager getInstance() {
        return INSTANCE;
    }

    public void setSession(UUID playerUuid, LoginResponse session) {
        if (playerUuid != null && session != null) {
            activeSessions.put(playerUuid, session);
        }
    }

    public LoginResponse getSession(UUID playerUuid) {
        if (playerUuid == null) return null;
        return activeSessions.get(playerUuid);
    }

    public boolean hasSession(UUID playerUuid) {
        return playerUuid != null && activeSessions.containsKey(playerUuid);
    }

    public LoginResponse clearSession(UUID playerUuid) {
        if (playerUuid == null) return null;
        return activeSessions.remove(playerUuid);
    }

    public String getAccessToken(UUID playerUuid) {
        LoginResponse response = getSession(playerUuid);
        return response != null ? response.getAccessToken() : null;
    }
}
