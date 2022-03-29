package ru.ptvi.elscriptserver.tcpserver.message;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class ChunkRequestMessage {
    String equipmentId;

    String scriptName;

    int chunkNumber;
}
