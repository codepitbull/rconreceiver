package de.codepitbull.rcon.adapter.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.adapter.sdk.api.annotations.ModuleConfigField;
import com.hivemq.adapter.sdk.api.tag.Tag;
import com.hivemq.adapter.sdk.api.tag.TagDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RconTag implements Tag {

    @JsonProperty(value = "name", required = true)
    @ModuleConfigField(title = "name",
            description = "name of the tag to be used in mappings",
            format = ModuleConfigField.FieldType.MQTT_TAG,
            required = true)
    private final @NotNull String name;

    @JsonProperty(value = "description")
    @ModuleConfigField(title = "description",
            description = "A human readable description of the tag")
    private final @NotNull String description;

    @JsonProperty(value = "definition", required = true)
    @ModuleConfigField(title = "definition",
            description = "The actual definition of the tag on the device")
    private final @NotNull RconTagDefinition definition;

    public RconTag(
            @JsonProperty(value = "name", required = true) final @NotNull String name,
            @JsonProperty(value = "description") final @Nullable String description,
            @JsonProperty(value = "definition", required = true) final @NotNull RconTagDefinition definiton) {
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "no description present.");
        this.definition = definiton;
    }

    @Override
    public @NotNull RconTagDefinition getDefinition() {
        return definition;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "RconTag{" +
                "name='" +
                name +
                '\'' +
                ", description='" +
                description +
                '\'' +
                ", definition=" +
                definition +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RconTag rconTag = (RconTag) o;
        return Objects.equals(name, rconTag.name) &&
                Objects.equals(description, rconTag.description) &&
                Objects.equals(definition, rconTag.definition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, definition);
    }
}
