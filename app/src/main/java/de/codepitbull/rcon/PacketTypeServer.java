package de.codepitbull.rcon;

import java.util.HashMap;
import java.util.Map;

public enum PacketTypeServer {
    SERVERDATA_RESPONSE_VALUE(0),
    SERVERDATA_AUTH_RESPONSE(2);

    private static final Map<Integer, PacketTypeServer> BY_ID = new HashMap<>();

    static {
        for (PacketTypeServer e: values()) {
            BY_ID.put(e.packetTypeId, e);
        }
    }

    private final int packetTypeId;

    PacketTypeServer(int packetTypeId) {
        this.packetTypeId = packetTypeId;
    }

    public int getPacketTypeId() {
        return packetTypeId;
    }

    public static PacketTypeServer valueOfId(int id) {
        return BY_ID.get(id);
    }
}
