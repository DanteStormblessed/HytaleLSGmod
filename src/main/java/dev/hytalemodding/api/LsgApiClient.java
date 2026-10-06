package dev.hytalemodding.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.hytalemodding.LsgPlugin;
import dev.hytalemodding.models.LoginResponse;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class LsgApiClient {

    private final HttpClient httpClient;
    private final Gson gson;
    private String authBaseUrl;
    private String coreApiBaseUrl;

    public LsgApiClient() {
        this("http://localhost:8001", "http://localhost:8012");
        if (LsgPlugin.getConfig() != null && LsgPlugin.getConfig().get() != null) {
            this.authBaseUrl = LsgPlugin.getConfig().get().getAuthUrl();
            this.coreApiBaseUrl = LsgPlugin.getConfig().get().getCoreApiUrl();
        }
    }

    public LsgApiClient(String authBaseUrl, String coreApiBaseUrl) {
        this.authBaseUrl = sanitizeUrl(authBaseUrl);
        this.coreApiBaseUrl = sanitizeUrl(coreApiBaseUrl);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(6))
                .build();
        this.gson = new Gson();
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String getAuthBaseUrl() {
        if (LsgPlugin.getConfig() != null && LsgPlugin.getConfig().get() != null) {
            return sanitizeUrl(LsgPlugin.getConfig().get().getAuthUrl());
        }
        return authBaseUrl;
    }

    public String getCoreApiBaseUrl() {
        if (LsgPlugin.getConfig() != null && LsgPlugin.getConfig().get() != null) {
            return sanitizeUrl(LsgPlugin.getConfig().get().getCoreApiUrl());
        }
        return coreApiBaseUrl;
    }

    public CompletableFuture<LoginResponse> login(String username, String password) {
        String authUrl = getAuthBaseUrl() + "/login";
        System.out.println("[LSG ApiClient] ==================================================");
        System.out.println("[LSG ApiClient] Iniciando peticion POST a: " + authUrl);
        System.out.println("[LSG ApiClient] Username: '" + username + "' (longitud: " + (username != null ? username.length() : 0) + ")");
        System.out.println("[LSG ApiClient] Password longitud: " + (password != null ? password.length() : 0));
        if (password != null && password.matches("^\\*+$")) {
            System.err.println("[LSG ApiClient] [ALERTA] La contrasena recibida son literalmente asteriscos ('*')!");
        }

        String formData = "grant_type=password" +
                "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(authUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[LSG ApiClient] Respuesta recibida -> HTTP " + response.statusCode());
                    System.out.println("[LSG ApiClient] Body de respuesta: " + response.body());
                    System.out.println("[LSG ApiClient] ==================================================");
                    if (response.statusCode() == 200) {
                        return gson.fromJson(response.body(), LoginResponse.class);
                    } else {
                        String errorMsg = extractErrorMessage(response.body(), response.statusCode());
                        System.err.println("[LSG ApiClient] Error extraido: " + errorMsg);
                        throw new RuntimeException(errorMsg);
                    }
                });
    }

    public CompletableFuture<String> checkAuthHealth() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getAuthBaseUrl() + "/health"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return "OK (" + response.body() + ")";
                    } else {
                        return "Error HTTP " + response.statusCode();
                    }
                });
    }

    public CompletableFuture<String> checkCoreHealth() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getCoreApiBaseUrl() + "/health"))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return "OK (" + response.body() + ")";
                    } else {
                        return "Error HTTP " + response.statusCode();
                    }
                });
    }

    public CompletableFuture<String> getPlayerCoreProfile(String accessToken, int playerId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getCoreApiBaseUrl() + "/players/" + playerId))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        throw new RuntimeException("Error consultando perfil del jugador: HTTP " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<java.util.List<dev.hytalemodding.models.PlayerAttributePoints>> getPlayerAttributePoints(String accessToken, int playerId) {
        String url = getCoreApiBaseUrl() + "/players/" + playerId + "/attributes/points";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(6))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<java.util.List<dev.hytalemodding.models.PlayerAttributePoints>>(){}.getType();
                        return gson.fromJson(response.body(), listType);
                    } else {
                        String errorMsg = extractErrorMessage(response.body(), response.statusCode());
                        throw new RuntimeException("Error consultando puntos de atributos: " + errorMsg);
                    }
                });
    }

    public CompletableFuture<Boolean> adjustPlayerPoints(String accessToken, int playerId, int attributeId, String direction, int amount, String reason) {
        String url = getCoreApiBaseUrl() + "/players/" + playerId + "/points/adjust";
        JsonObject json = new JsonObject();
        json.addProperty("attribute_id", attributeId);
        json.addProperty("direction", direction);
        json.addProperty("amount", amount);
        if (reason != null) {
            json.addProperty("reason", reason);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(6))
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        return true;
                    } else {
                        String errorMsg = extractErrorMessage(response.body(), response.statusCode());
                        throw new RuntimeException(errorMsg);
                    }
                });
    }

    private String extractErrorMessage(String body, int statusCode) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null && json.has("detail")) {
                com.google.gson.JsonElement detailElement = json.get("detail");
                if (detailElement.isJsonPrimitive()) {
                    return detailElement.getAsString();
                } else if (detailElement.isJsonArray() && !detailElement.getAsJsonArray().isEmpty()) {
                    com.google.gson.JsonElement first = detailElement.getAsJsonArray().get(0);
                    if (first.isJsonObject() && first.getAsJsonObject().has("msg")) {
                        return first.getAsJsonObject().get("msg").getAsString();
                    }
                    return detailElement.toString();
                }
            }
        } catch (Exception ignored) {}
        return "Fallo de autenticacion (HTTP " + statusCode + ")";
    }
}