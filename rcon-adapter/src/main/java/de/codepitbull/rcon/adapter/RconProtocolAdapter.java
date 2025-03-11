package de.codepitbull.rcon.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.adapter.sdk.api.ProtocolAdapterInformation;
import com.hivemq.adapter.sdk.api.model.*;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingInput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingOutput;
import com.hivemq.adapter.sdk.api.polling.batch.BatchPollingProtocolAdapter;
import com.hivemq.adapter.sdk.api.schema.TagSchemaCreationInput;
import com.hivemq.adapter.sdk.api.schema.TagSchemaCreationOutput;
import com.hivemq.adapter.sdk.api.services.ModuleServices;
import com.hivemq.adapter.sdk.api.services.ProtocolAdapterMetricsService;
import com.hivemq.adapter.sdk.api.state.ProtocolAdapterState;
import com.hivemq.adapter.sdk.api.writing.WritingInput;
import com.hivemq.adapter.sdk.api.writing.WritingOutput;
import com.hivemq.adapter.sdk.api.writing.WritingPayload;
import com.hivemq.adapter.sdk.api.writing.WritingProtocolAdapter;
import de.codepitbull.rcon.adapter.config.RconProtocolAdapterSpecificConfiguration;
import de.codepitbull.rcon.adapter.config.RconTag;
import de.codepitbull.rcon.factorio.Commands;
import de.codepitbull.rcon.proto.RconConnection;
import de.codepitbull.rcon.proto.Result;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

public class RconProtocolAdapter implements BatchPollingProtocolAdapter, WritingProtocolAdapter {

    private static final Logger log = LoggerFactory.getLogger(RconProtocolAdapter.class.getName());

    private final @NotNull ProtocolAdapterInformation adapterInformation;
    private final @NotNull RconProtocolAdapterSpecificConfiguration adapterConfig;
    private final @NotNull List<RconTag> tags;
    private final @NotNull Map<String, RconTag> tagNameToTag;
    private final @NotNull List<Commands.ReadSignalFromConstantCombinator> tagCommands;
    private final @NotNull ProtocolAdapterState protocolAdapterState;
    private final @NotNull ProtocolAdapterMetricsService protocolAdapterMetricsService;
    private final @NotNull ModuleServices moduleServices;
    private final @NotNull String adapterId;

    private final ObjectMapper mapper;
    private final @NotNull RconConnection connection;

    private final @NotNull String SCHEMA = """
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {
                "value": {
                  "type": "integer"
                }
              },
              "required": ["value"]
            }
            """;

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
        this.tagNameToTag = tags.stream().collect(Collectors.toMap(RconTag::getName, tag -> tag));
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
                            log.error("Problem interacting with RCON");
                            batchPollingOutput.fail(throwable, "Problem interacting with RCON");
                        }
                    }).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Problem during execution", e);
            batchPollingOutput.fail(e, "Problem during execution");
        }
    }

    @Override
    public void write(@NotNull WritingInput writingInput, @NotNull WritingOutput writingOutput) {
        var tagToWrite = tagNameToTag.get(writingInput.getWritingContext().getTagName());
        var def = tagToWrite.getDefinition();
        Commands
                .setCombinatorSignal(mapper, connection, List.of(
                    new Commands.WriteSignalToConstantCombinator(
                            def.getId(),
                            def.getX(),
                            def.getY(),
                            def.getSlot(),
                            def.getSignalName(),
                            ((RconWritingPayload)writingInput.getWritingPayload()).getValue())))
                .whenComplete((result, throwable) -> {
                    if (throwable == null) {
                        writingOutput.finish();
                    } else {
                        log.error("Problem writing to RCON", throwable);
                        writingOutput.fail(throwable, "Problem writing to RCON");
                    }
                });

    }

    @Override
    public @NotNull Class<? extends WritingPayload> getMqttPayloadClass() {
        return RconWritingPayload.class;
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
            protocolAdapterState.setConnectionStatus(ProtocolAdapterState.ConnectionStatus.CONNECTED);
            protocolAdapterStartOutput.startedSuccessfully();
        } else if (result instanceof Result.Failure failure) {
            failure.throwable().ifPresentOrElse(
                    throwable -> {
                        protocolAdapterStartOutput.failStart(throwable, failure.message());
                    },
                    () -> protocolAdapterStartOutput.failStart(new Throwable(failure.message()), failure.message())
            );
        }
    }

    @Override
    public void stop(@NotNull ProtocolAdapterStopInput protocolAdapterStopInput, @NotNull ProtocolAdapterStopOutput protocolAdapterStopOutput) {
        protocolAdapterState.setConnectionStatus(ProtocolAdapterState.ConnectionStatus.DISCONNECTED);
        connection.disconnect();
    }

    @Override
    public @NotNull ProtocolAdapterInformation getProtocolAdapterInformation() {
        return adapterInformation;
    }

    @Override
    public void createTagSchema(@NotNull TagSchemaCreationInput input, @NotNull TagSchemaCreationOutput output) {
        try {
            output.finish(mapper.readTree(SCHEMA));
        } catch (JsonProcessingException e) {
            output.fail(e, "Failed parsing schema");
        }

    }


}
