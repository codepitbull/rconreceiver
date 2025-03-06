package de.codepitbull.rcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Connection to an RCON server.
 *
 * Sticking to TCP/IP since I don't want to deal with multi packages (and their implementation differences for different RCON sevrer impls)
 */
public class RconConnection {

    private static final Logger log = LoggerFactory.getLogger(RconConnection.class);

    private final Map<Integer, CompletableFuture<Response>> requestIdToPacket;

    private final int receiveBufferSize;
    private volatile Socket socket;
    private final AtomicInteger counter = new AtomicInteger(0);
    private Thread receiver;
    private final URI serverAddress;

    public RconConnection(URI serverAddress) {
        this(serverAddress, 4096);
    }

    public RconConnection(URI serverAddress, int receiveBufferSize) {
        this.serverAddress = serverAddress;
        this.receiveBufferSize = receiveBufferSize;
        this.requestIdToPacket = new ConcurrentHashMap<>();
    }

    public int nextRequestId() {
        return counter.incrementAndGet();
    }

    public void connect() {
        if(socket != null) {
            throw new IllegalStateException("This socket was already started. Also, restarting is not supported.");
        }
        try {
            this.socket = new Socket(serverAddress.getHost(), serverAddress.getPort());
            this.receiver = new Thread(this::receive);
            this.receiver.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect() {
        try {
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.receiver.interrupt();
            this.receiver.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Response> send(Commands.RawCommand command) {
        //eagerly grab the next id
        int requestId = nextRequestId();
        log.info("Using requestId {}", requestId);
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

    public void receive() {
        byte[] buff = new byte[receiveBufferSize];
        try {
            while(!Thread.currentThread().isInterrupted() && socket.isConnected() && socket.getInputStream().read(buff, 0, buff.length) != -1) {
                var res = Response.fromBytes(buff);
                requestIdToPacket.remove(res.requestId()).complete(res);
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
