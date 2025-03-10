package de.codepitbull.rcon.proto;

public record RawCommand(PacketTypeClient type, String content) {}
