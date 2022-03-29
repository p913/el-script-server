package ru.ptvi.elscriptserver.domain;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Script {
    @NonNull
    @EqualsAndHashCode.Include
    String name;

    int size;

    @NonNull
    LocalDateTime created;

    public boolean exists() {
        return size >= 0;
    }
}
