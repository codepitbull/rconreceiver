package de.codepitbull.rcon.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.adapter.sdk.api.ProtocolAdapterInformation;
import com.hivemq.adapter.sdk.api.model.*;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingInput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingOutput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingProtocolAdapter;
import com.hivemq.adapter.sdk.api.services.ModuleServices;
import com.hivemq.adapter.sdk.api.services.ProtocolAdapterMetricsService;
import com.hivemq.adapter.sdk.api.state.ProtocolAdapterState;
import de.codepitbull.rcon.adapter.config.RconProtocolAdapterSpecificConfiguration;
import de.codepitbull.rcon.adapter.config.RconTag;
import de.codepitbull.rcon.factorio.Commands;
import de.codepitbull.rcon.proto.RconConnection;
import de.codepitbull.rcon.proto.Result;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.IntStream.range;

public class RconProtocolAdapter implements BatchPollingProtocolAdapter {

    private final @NotNull ProtocolAdapterInformation adapterInformation;
    private final @NotNull RconProtocolAdapterSpecificConfiguration adapterConfig;
    private final @NotNull List<RconTag> tags;
    private final @NotNull List<Commands.ReadSignalFromConstantCombinator> tagCommands;
    private final @NotNull ProtocolAdapterState protocolAdapterState;
    private final @NotNull ProtocolAdapterMetricsService protocolAdapterMetricsService;
    private final @NotNull ModuleServices moduleServices;
    private final @NotNull String adapterId;

    private final ObjectMapper mapper;
    private final @NotNull RconConnection connection;

    public RconProtocolAdapter(
            final @NotNull ProtocolAdapterInformation adapterInformation,
            final @NotNull ProtocolAdapterInput<RconProtocolAdapterSpecificConfiguration> input) {
        this.adapterId = input.getAdapterId();
        this.adapterInformation = adapterInformation;
        this.adapterConfig = input.getConfig();
        this.protocolAdapterState = input.getProtocolAdapterState();
        this.tags = input.getTags().stream().map(tag -> (RconTag)tag).toList();
        this.tagCommands = tags.stream().map(tag -> {
            var def = tag.getDefinition();
            return new Commands.ReadSignalFromConstantCombinator(def.getId(), def.getX(), def.getY(), def.getSlot(), def.getSignalName());
        }).toList();
        this.protocolAdapterMetricsService = input.getProtocolAdapterMetricsHelper();
        this.moduleServices = input.moduleServices();
        this.connection = new RconConnection(URI.create(adapterConfig.getUri()), adapterConfig.getAuth().getPassword());
        this.mapper = new ObjectMapper();
    }

    @Override
    public void poll(@NotNull BatchPollingInput batchPollingInput, @NotNull BatchPollingOutput batchPollingOutput) {

        try {
            Commands
                    .readCombinatorSignal(mapper, connection, tagCommands)
                    .whenComplete((result, throwable) -> {
                        if(throwable == null) {
                            range(0, tags.size())
                                    .forEach(index -> {
                                        batchPollingOutput.addDataPoint(tags.get(index).getName(), result.get(index).min());
                                    });
                            batchPollingOutput.finish();
                        } else {
                            batchPollingOutput.fail(throwable, "WTF?");
                        }
                    }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPollingIntervalMillis() {
        return adapterConfig.getRconToMQTTConfig().getPollingIntervalMillis();
    }

    @Override
    public int getMaxPollingErrorsBeforeRemoval() {
        return adapterConfig.getRconToMQTTConfig().getMaxPollingErrorsBeforeRemoval();
    }

    @Override
    public @NotNull String getId() {
        return adapterId;
    }

    @Override
    public void start(@NotNull ProtocolAdapterStartInput protocolAdapterStartInput, @NotNull ProtocolAdapterStartOutput protocolAdapterStartOutput) {
        var result = connection.connect();
        if (result instanceof Result.Success) {
            protocolAdapterStartOutput.startedSuccessfully();
        } else if (result instanceof Result.Failure failure) {
            failure.throwable().ifPresentOrElse(
                    throwable -> protocolAdapterStartOutput.failStart(throwable, failure.message()),
                    () -> protocolAdapterStartOutput.failStart(new Throwable(failure.message()), failure.message())
            );
        }
    }

    @Override
    public void stop(@NotNull ProtocolAdapterStopInput protocolAdapterStopInput, @NotNull ProtocolAdapterStopOutput protocolAdapterStopOutput) {
        connection.disconnect();
    }

    @Override
    public @NotNull ProtocolAdapterInformation getProtocolAdapterInformation() {
        return adapterInformation;
    }
}
