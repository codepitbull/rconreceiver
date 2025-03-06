package de.codepitbull.rcon;

import java.util.HashMap;
import java.util.Map;

public enum PacketTypeClient {
    SERVERDATA_EXECCOMMAND(2),
    SERVERDATA_AUTH(3);

    private final int packetTypeId;

    private static final Map<Integer, PacketTypeClient> BY_ID = new HashMap<>();

    static {
        for (PacketTypeClient e: values()) {
            BY_ID.put(e.packetTypeId, e);
        }
    }

    PacketTypeClient(int packetTypeId) {
        this.packetTypeId = packetTypeId;
    }

    public int getPacketTypeId() {
        return packetTypeId;
    }

    public static PacketTypeClient valueOfId(int id) {
        return BY_ID.get(id);
    }
}
