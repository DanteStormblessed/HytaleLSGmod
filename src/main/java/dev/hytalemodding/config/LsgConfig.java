package dev.hytalemodding.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LsgConfig {

    public static final BuilderCodec<LsgConfig> CODEC = BuilderCodec.builder(LsgConfig.class, LsgConfig::new)
            .append(
                new KeyedCodec<>("EnableWelcomeMessage", Codec.BOOLEAN),
                (cfg, b, extra) -> cfg.enabledWelcomeMessage = b != null && b,
                (cfg, extra) -> cfg.enabledWelcomeMessage
            ).add()
            .append(
                new KeyedCodec<>("AuthUrl", Codec.STRING),
                (cfg, s, extra) -> cfg.authUrl = (s != null && !s.isBlank()) ? s : "http://localhost:8001",
                (cfg, extra) -> cfg.authUrl
            ).add()
            .append(
                new KeyedCodec<>("CoreApiUrl", Codec.STRING),
                (cfg, s, extra) -> cfg.coreApiUrl = (s != null && !s.isBlank()) ? s : "http://localhost:8012",
                (cfg, extra) -> cfg.coreApiUrl
            ).add()
            .build();

    private boolean enabledWelcomeMessage = true;
    private String authUrl = "http://localhost:8001";
    private String coreApiUrl = "http://localhost:8012";

    public LsgConfig() {}

    public boolean isEnabledWelcomeMessage() {
        return enabledWelcomeMessage;
    }

    public String getAuthUrl() {
        return authUrl != null && !authUrl.isBlank() ? authUrl : "http://localhost:8001";
    }

    public String getCoreApiUrl() {
        return coreApiUrl != null && !coreApiUrl.isBlank() ? coreApiUrl : "http://localhost:8012";
    }
}
