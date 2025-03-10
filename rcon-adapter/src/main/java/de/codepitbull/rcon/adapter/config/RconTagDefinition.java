package de.codepitbull.rcon.adapter.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.adapter.sdk.api.annotations.ModuleConfigField;
import com.hivemq.adapter.sdk.api.tag.TagDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RconTagDefinition implements TagDefinition {

    @JsonProperty(value = "x", required = true)
    @ModuleConfigField(title = "x coord",
            description = "x coord on the map",
            required = true)
    private final @NotNull double x;

    @JsonProperty(value = "y", required = true)
    @ModuleConfigField(title = "y coord",
            description = "y coord on the map",
            required = true)
    private final @NotNull double y;

    @JsonProperty(value = "id", required = true)
    @ModuleConfigField(title = "entity id",
            description = "entity id of constant combinator",
            required = true)
    private final @NotNull long id;

    @JsonProperty(value = "slot", required = true)
    @ModuleConfigField(title = "signal slot id",
            description = "id of the signal slot",
            required = true)
    private final @NotNull int slot;

    @JsonProperty(value = "signalName", required = true)
    @ModuleConfigField(title = "Signal name (e.g. signal-A)",
            description = "Name of the referenced signal",
            required = true)
    private final @NotNull String signalName;

    public RconTagDefinition(final @JsonProperty(value = "x", required = true) @NotNull double x,
                             final @JsonProperty(value = "y", required = true) @NotNull double y,
                             final @JsonProperty(value = "id", required = true) @NotNull long id,
                             final @JsonProperty(value = "slot", required = true) @NotNull int slot,
                             final @JsonProperty(value = "signalName", required = true) @NotNull String signalName) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.slot = slot;
        this.signalName = signalName;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public long getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public @NotNull String getSignalName() {
        return signalName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RconTagDefinition that = (RconTagDefinition) o;
        return Double.compare(getX(), that.getX()) == 0 && Double.compare(getY(), that.getY()) == 0 && getId() == that.getId() && getSlot() == that.getSlot() && Objects.equals(getSignalName(), that.getSignalName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getId(), getSlot(), getSignalName());
    }

    @Override
    public String toString() {
        return "RconTagDefinition{" +
                "x=" + x +
                ", y=" + y +
                ", id=" + id +
                ", slot=" + slot +
                ", signalName='" + signalName + '\'' +
                '}';
    }
}
