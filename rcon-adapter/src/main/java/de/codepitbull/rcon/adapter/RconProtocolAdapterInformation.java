package de.codepitbull.rcon.adapter;

import com.hivemq.adapter.sdk.api.ProtocolAdapterCapability;
import com.hivemq.adapter.sdk.api.ProtocolAdapterCategory;
import com.hivemq.adapter.sdk.api.ProtocolAdapterInformation;
import com.hivemq.adapter.sdk.api.ProtocolAdapterTag;
import com.hivemq.adapter.sdk.api.config.ProtocolSpecificAdapterConfig;
import com.hivemq.adapter.sdk.api.tag.Tag;
import de.codepitbull.rcon.adapter.config.RconProtocolAdapterSpecificConfiguration;
import de.codepitbull.rcon.adapter.config.RconTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class RconProtocolAdapterInformation implements ProtocolAdapterInformation {

    public static final @NotNull ProtocolAdapterInformation INSTANCE = new RconProtocolAdapterInformation();


    @Override
    public @NotNull String getProtocolName() {
        return "rcon";
    }

    @Override
    public @NotNull String getProtocolId() {
        return "rcon";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "RCON";
    }

    @Override
    public @NotNull String getDescription() {
        return "RCON server protocol for factorio";
    }

    @Override
    public @NotNull String getUrl() {
        return "";
    }

    @Override
    public @NotNull String getVersion() {
        return "1";
    }

    @Override
    public @NotNull String getLogoUrl() {
        return "";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jochen Mader";
    }

    @Override
    public @Nullable ProtocolAdapterCategory getCategory() {
        return ProtocolAdapterCategory.SIMULATION;
    }

    @Override
    public @Nullable List<ProtocolAdapterTag> getTags() {
        return List.of();
    }

    @Override
    public @NotNull Class<? extends Tag> tagConfigurationClass() {
        return RconTag.class;
    }

    @Override
    public @NotNull Class<? extends ProtocolSpecificAdapterConfig> configurationClassNorthbound() {
        return RconProtocolAdapterSpecificConfiguration.class;
    }

    @Override
    public @NotNull Class<? extends ProtocolSpecificAdapterConfig> configurationClassNorthAndSouthbound() {
        return RconProtocolAdapterSpecificConfiguration.class;
    }

    @Override
    public int getCurrentConfigVersion() {
        return 1;
    }

    @Override
    public @NotNull EnumSet<ProtocolAdapterCapability> getCapabilities() {
        return EnumSet.of(ProtocolAdapterCapability.READ,
                ProtocolAdapterCapability.WRITE,
                ProtocolAdapterCapability.DISCOVER,
                ProtocolAdapterCapability.COMBINE);
    }
}
