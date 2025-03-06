package de.codepitbull.rcon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public record Response(int requestId, PacketTypeServer type, byte[] content) {

    public static Response fromBytes(byte[] bytes) {
        var wrapped = ByteBuffer.wrap(bytes);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);

        var length = wrapped.getInt();

        if (length - 10 > 0) {
            var requestId = wrapped.getInt();
            var command = wrapped.getInt();
            var result = new byte[length - 10];
            wrapped.get(result, wrapped.arrayOffset(), length - 10);

            return new Response(requestId, PacketTypeServer.valueOfId(command), result);
        } else {
            var requestId = wrapped.getInt();
            return new Response(requestId, PacketTypeServer.SERVERDATA_AUTH_RESPONSE, new byte[]{});
        }
    }

    ;

    @Override
    public String toString() {
        return "Response{" +
                "requestId=" + requestId +
                ", type=" + type +
                ", content=" + new String(content) +
                '}';
    }
}
