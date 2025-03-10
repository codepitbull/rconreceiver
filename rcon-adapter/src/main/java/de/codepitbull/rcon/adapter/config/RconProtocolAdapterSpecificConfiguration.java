package de.codepitbull.rcon.adapter.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.adapter.sdk.api.annotations.ModuleConfigField;
import com.hivemq.adapter.sdk.api.config.ProtocolSpecificAdapterConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RconProtocolAdapterSpecificConfiguration implements ProtocolSpecificAdapterConfig {

    private static final @NotNull String ID_REGEX = "^([a-zA-Z_0-9-_])*$";

    @JsonProperty(value = "id", required = true, access = JsonProperty.Access.WRITE_ONLY)
    @ModuleConfigField(title = "Identifier",
            description = "Unique identifier for this protocol adapter",
            format = ModuleConfigField.FieldType.IDENTIFIER,
            required = true,
            stringPattern = ID_REGEX,
            stringMinLength = 1,
            stringMaxLength = 1024)
    private @Nullable String id;

    @JsonProperty(value = "uri", required = true)
    @ModuleConfigField(title = "RCON Server URI",
            description = "URI of the RCON server to connect to",
            format = ModuleConfigField.FieldType.URI,
            required = true)
    private final @NotNull String uri;

    @JsonProperty("auth")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final @Nullable Auth auth;

    @JsonProperty(value = "rconToMqtt", required = true)
    @ModuleConfigField(title = "RCON To MQTT Config",
            description = "The configuration for a data stream from RCON to MQTT",
            required = true)
    private final @Nullable RconToMqttConfig rconToMQTTConfig;

    @JsonCreator
    public RconProtocolAdapterSpecificConfiguration(
            final @JsonProperty(value = "uri", required = true) @NotNull String uri,
            final @JsonProperty("auth") @Nullable Auth auth,
            final @JsonProperty(value = "rconToMqtt", required = true) @Nullable RconToMqttConfig rconToMQTTConfig) {
        this.uri = uri;
        this.auth = auth;
        this.rconToMQTTConfig = rconToMQTTConfig;
    }

    public @NotNull String getUri() {
        return uri;
    }

    public @Nullable Auth getAuth() {
        return auth;
    }

    public @Nullable RconToMqttConfig getRconToMQTTConfig() {
        return rconToMQTTConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RconProtocolAdapterSpecificConfiguration that = (RconProtocolAdapterSpecificConfiguration) o;
        return Objects.equals(getUri(), that.getUri()) && Objects.equals(getAuth(), that.getAuth()) && Objects.equals(getRconToMQTTConfig(), that.getRconToMQTTConfig());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUri(), getAuth(), getRconToMQTTConfig());
    }

    @Override
    public String toString() {
        return "RconProtocolAdapterSpecificConfiguration{" +
                "uri='" + uri + '\'' +
                ", auth=" + auth +
                ", rconToMQTTConfig=" + rconToMQTTConfig +
                '}';
    }
}
