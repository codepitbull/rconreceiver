package de.codepitbull.rcon.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.adapter.sdk.api.writing.WritingPayload;

public class RconWritingPayload implements WritingPayload {

    @JsonProperty("value")
    private final int value;

    public RconWritingPayload(final @JsonProperty("value") int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
