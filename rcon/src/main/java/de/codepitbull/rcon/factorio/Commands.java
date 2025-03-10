package de.codepitbull.rcon.factorio;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.codepitbull.rcon.proto.PacketTypeClient;
import de.codepitbull.rcon.proto.RawCommand;
import de.codepitbull.rcon.proto.RconConnection;
import de.codepitbull.rcon.proto.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Commands {

    public static CompletableFuture<StringResponse> version(final ObjectMapper mapper, final RconConnection rcon) {
        return rcon
                .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/version"))
                .thenApply(res -> new StringResponse(new String(res.content()).trim()));
    }

    public static CompletableFuture<StringResponse> help(final ObjectMapper mapper, final RconConnection rcon) {
        return rcon
                .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/help"))
                .thenApply(res -> new StringResponse(new String(res.content()).trim()));
    }

    public static CompletableFuture<LongResponse> seed(final ObjectMapper mapper, final RconConnection rcon) {
        return rcon
                .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/seed"))
                .thenApply(res -> new LongResponse(Long.valueOf(new String(res.content()).trim())));
    }

    public static CompletableFuture<EmptyResponse> cheat(final ObjectMapper mapper, final RconConnection rcon, String cmd) {
        return rcon
                .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/cheat " + cmd))
                .thenApply(res -> new EmptyResponse());
    }

    public static CompletableFuture<List<Location>> find(final ObjectMapper mapper, final RconConnection rcon, String entityType) {
        return rcon
                .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/find " + entityType))
                .thenApply(res -> responseToJson(mapper, res, new TypeReference<>() {}));
    }

    public static CompletableFuture<List<CombinatorState>> readCombinatorSignal(final ObjectMapper mapper, final RconConnection rcon, List<ReadSignalFromConstantCombinator> readSignalFromConstantCombinators) {
        try {
            return rcon
                    .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/get_combinators_signals " + mapper.writeValueAsString(readSignalFromConstantCombinators)))
                    .thenApply(res -> responseToJson(mapper, res, new TypeReference<>() {}));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<StringResponse> setCombinatorSignal(final ObjectMapper mapper, final RconConnection rcon, List<WriteSignalToConstantCombinator> writeSignalToConstantCombinators) {
        try {
            return rcon
                    .send(new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/set_combinators_signals " + mapper.writeValueAsString(writeSignalToConstantCombinators)))
                    .thenApply(res -> new StringResponse(new String(res.content()).trim()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T responseToJson(final ObjectMapper mapper, final Response res, final TypeReference<T> typeReference) {
        try {
            return (T) mapper.readValue(res.content(), typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public record CombinatorState(int min){};
    public record Slot(Value value, int min){};
    public record Value(String name, String type, String quality, String comparator){};

    public record StringResponse(String value){};
    public record LongResponse(long value){};
    public record EmptyResponse(){};
    public record Location(String n, long id, double x, double y){};
    
    public record WriteSignalToConstantCombinator(long id, double x, double y, int s, String sn, int min){};
    public record ReadSignalFromConstantCombinator(long id, double x, double y, int s, String sn){};
}
