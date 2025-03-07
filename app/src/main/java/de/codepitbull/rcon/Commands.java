package de.codepitbull.rcon;

public class Commands {

    public static RawCommand auth(String password) {
        return new RawCommand(PacketTypeClient.SERVERDATA_AUTH, password);
    }

    public static RawCommand version() {
        return new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/version");
    }

    public static RawCommand help() {
        return new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/help");
    }

    public static RawCommand seed() {
        return new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/seed");
    }

    public static RawCommand cheat(String cmd) {
        return new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/cheat "+cmd);
    }

    public static RawCommand find(String entityType) {
        return new RawCommand(PacketTypeClient.SERVERDATA_EXECCOMMAND, "/find " + entityType);
    }

    public record RawCommand(PacketTypeClient type, String content) {};

}
