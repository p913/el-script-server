package ru.ptvi.elscriptserver.event;

import lombok.Value;
import lombok.experimental.Accessors;
import ru.ptvi.elscriptserver.domain.Script;

@Value
@Accessors(fluent = true)
public class ChunkRequested {
    String channelId;

    String equipmentId;

    Script script;

    int chunkNumber;
}
