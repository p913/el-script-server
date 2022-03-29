package ru.ptvi.elscriptserver.domain;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class ScriptChunk {
    @NonNull
    Script script;

    int number;

    boolean isLast;

    @NonNull
    byte[] data;
}
