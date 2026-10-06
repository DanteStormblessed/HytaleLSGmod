package dev.hytalemodding.models;

public class LoginResponse {
    private String access_token;
    private String token_type;
    private int expires_in_seconds;
    private String expires_at;
    private Player player;

    // Getters
    public String getAccessToken() { return access_token; }
    public String getTokenType() { return token_type; }
    public int getExpiresInSeconds() { return expires_in_seconds; }
    public String getExpiresAt() { return expires_at; }
    public Player getPlayer() { return player; }
}