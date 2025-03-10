package de.codepitbull.rcon.adapter;

import com.hivemq.adapter.sdk.api.ProtocolAdapter;
import com.hivemq.adapter.sdk.api.ProtocolAdapterInformation;
import com.hivemq.adapter.sdk.api.factories.ProtocolAdapterFactory;
import com.hivemq.adapter.sdk.api.factories.ProtocolAdapterFactoryInput;
import com.hivemq.adapter.sdk.api.model.ProtocolAdapterInput;
import de.codepitbull.rcon.adapter.config.RconProtocolAdapterSpecificConfiguration;
import org.jetbrains.annotations.NotNull;

public class RconProtocolAdapterFactory implements ProtocolAdapterFactory<RconProtocolAdapterSpecificConfiguration> {

    final boolean writingEnabled;

    public RconProtocolAdapterFactory(final @NotNull ProtocolAdapterFactoryInput input) {
        this.writingEnabled = input.isWritingEnabled();
    }

    @Override
    public @NotNull ProtocolAdapterInformation getInformation() {
        return RconProtocolAdapterInformation.INSTANCE;
    }

    @Override
    public @NotNull ProtocolAdapter createAdapter(@NotNull ProtocolAdapterInformation protocolAdapterInformation, @NotNull ProtocolAdapterInput<RconProtocolAdapterSpecificConfiguration> protocolAdapterInput) {
        return new RconProtocolAdapter(protocolAdapterInformation, protocolAdapterInput);
    }
}
