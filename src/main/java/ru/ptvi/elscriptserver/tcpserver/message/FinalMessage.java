package ru.ptvi.elscriptserver.tcpserver.message;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class FinalMessage {
    String scriptName;

    int scriptSize;

    int scriptDataCrc;
}
