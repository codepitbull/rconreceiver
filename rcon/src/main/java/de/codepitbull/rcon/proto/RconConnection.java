package de.codepitbull.rcon.proto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static de.codepitbull.rcon.proto.Result.failure;
import static de.codepitbull.rcon.proto.Result.success;


/**
 * Connection to an RCON server.
 *
 * Sticking to TCP/IP since I don't want to deal with multi packages (and their implementation differences for different RCON sevrer impls)
 */
public class RconConnection {

    private static final Logger log = LoggerFactory.getLogger(RconConnection.class);

    private final Map<Integer, CompletableFuture<Response>> requestIdToPacket;

    private final int receiveBufferSize;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final URI serverAddress;
    private final String password;

    private volatile Socket socket;
    private volatile Thread receiver;

    public RconConnection(URI serverAddress, String password) {
        this(serverAddress, 4096, password);
    }

    public RconConnection(URI serverAddress, int receiveBufferSize, String password) {
        this.serverAddress = serverAddress;
        this.receiveBufferSize = receiveBufferSize;
        this.requestIdToPacket = new ConcurrentHashMap<>();
        this.password = password;
    }

    public int nextRequestId() {
        return counter.incrementAndGet();
    }

    public Result connect() {
        if(socket != null) {
            return failure("This socket was already started. Also, restarting is not supported.");
        }
        try {
            this.socket = new Socket(serverAddress.getHost(), serverAddress.getPort());
            if(password != null) {
                Result authenticate = authenticate(password);
                if(authenticate instanceof Result.Failure) {
                    return authenticate;
                }
            }
            this.receiver = new Thread(this::receive);
            this.receiver.start();
            log.info("Connected to " + serverAddress);
        } catch (IOException e) {
            return failure("Problem connecting to " + serverAddress + ": " + e.getMessage(), e);
        }
        return success();
    }

    public Result disconnect() {
        try {
            this.socket.close();
            this.socket = null;
        } catch (IOException e) {
            return failure("Problem closing socket for " + serverAddress + ": " + e.getMessage(), e);
        }
        try {
            this.receiver.interrupt();
            this.receiver.join();
            this.receiver = null;
        } catch (InterruptedException e) {
            return failure("Problem closing receiver for " + serverAddress + ": " + e.getMessage(), e);
        }
        return success();
    }

    public CompletableFuture<Response> send(RawCommand command) {
        int requestId = nextRequestId();
        log.trace("Using requestId {}", requestId);
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        var ret = new CompletableFuture<Response>();
                        requestIdToPacket.put(requestId, ret);
                        socket.getOutputStream().write(Packet.encode(requestId, command.type().getPacketTypeId(), command.content()));
                        socket.getOutputStream().flush();
                        return ret;
                    } catch (IOException e) {
                        log.error("Something went wrong writing to the socket", e);
                        throw new RuntimeException(e);
                    }
                })
                .thenCompose(f -> f); //bring the inner future to the outside
    }

    public Result authenticate(String password) {
        int requestId = nextRequestId();
        log.info("Using requestId {}", requestId);
        try {
            socket.getOutputStream().write(Packet.encode(requestId, PacketTypeClient.SERVERDATA_AUTH.getPacketTypeId(), password));
            socket.getOutputStream().flush();
            byte[] buff = new byte[receiveBufferSize];
            socket.getInputStream().read(buff, 0, buff.length);
            var res = Response.fromBytes(buff);
            var receivedRequestId = res.requestId();
            if(requestId == receivedRequestId) {
                log.info("Successfully connected and authenticated.");
                return success();
            } else {
                log.error("Failed connecting, wrong credentials provided.");
                return failure("Wrong credentials provided.");
            }
        } catch (IOException e) {
            log.error("Something went wrong interacting with socket", e);
            return failure("Something went wrong interacting with socket", e);
        }
    }

    public void receive() {
        byte[] buff = new byte[receiveBufferSize];
        try {
            while(!Thread.currentThread().isInterrupted() && socket.isConnected() && socket.getInputStream().read(buff, 0, buff.length) != -1) {
                var res = Response.fromBytes(buff);
                var requestId = res.requestId();
                CompletableFuture<Response> request = requestIdToPacket.remove(requestId);
                if(request == null) {
                    if(requestId == -1) {
                        throw new RuntimeException("Server indicated a problem, terminating.");
                    }
                    log.error("Unmatched requestId {}", res.requestId());
                } else {
                    request.complete(res);
                }
            }
        } catch (IOException e) {
            if (socket.isClosed()) {
                log.info("Socket closed, stopping to consume");
            } else {
                log.error("Something went wrong, stopping to consume", e);
            }
        }
    }


}
