package de.codepitbull.rcon.proto;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * <a href="https://developer.valvesoftware.com/wiki/Source_RCON_Protocol">RCON docs</a>
 *
 * Packet structure:
 * - size: 32-bit integer indicating the total size of the packet in bytes.
 * - request id: 32-bit integer that uniquely identifies the packet and is returned in the response packet to match requests with responses.
 * - type: 32-bit integer specifying the packet type
 * - body: the actual command
 * - terminator: packet ends with two null bytes (\0\0)
 */

public class Packet {
    public static final byte[] TERMINATOR = new byte[]{0, 0};

    public static byte[] encode(int requestId, int type, String command) {
        var bodyBytes = command != null && !command.isEmpty() ? command.getBytes(StandardCharsets.UTF_8) : new byte[]{};
        var size = 4 + 4 + 4 + bodyBytes.length + 2;
        var buffy = ByteBuffer.allocate(size);
        buffy.order(ByteOrder.LITTLE_ENDIAN);
        buffy.putInt(size - 4);
        buffy.putInt(requestId);
        buffy.putInt(type);
        buffy.put(bodyBytes);
        buffy.put(TERMINATOR);
        return buffy.array();
    }
}
