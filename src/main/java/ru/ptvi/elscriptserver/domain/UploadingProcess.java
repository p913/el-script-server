package ru.ptvi.elscriptserver.domain;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Builder(toBuilder = true)
public class UploadingProcess {
    @NonNull
    @EqualsAndHashCode.Include
    String equipmentId;

    @NonNull
    @EqualsAndHashCode.Include
    Script script;

    @NonNull
    OffsetDateTime uploadStartTime;

    @NonNull
    OffsetDateTime connectionStartTime;

    @NonNull
    String connectionId;

    OffsetDateTime connectionEndTime;

    int totalChunkCount;

    int requestCount;

    int retryCount;

    Set<Integer> uploadedChunks;

    public UploadingProcess(@NonNull String equipmentId, @NonNull Script script, @NonNull OffsetDateTime uploadStartTime,
                            @NonNull OffsetDateTime connectionStartTime, @NonNull String connectionId,
                            int totalChunkCount, int requestCount, int retryCount) {
        this.equipmentId = equipmentId;
        this.script = script;
        this.uploadStartTime = uploadStartTime;
        this.connectionStartTime = connectionStartTime;
        this.connectionId = connectionId;
        this.totalChunkCount = totalChunkCount;
        this.requestCount = requestCount;
        this.retryCount = retryCount;
        this.uploadedChunks = new HashSet<>();
        this.connectionEndTime = null;
    }

    public boolean connectionAlive() {
        return connectionEndTime == null;
    }

    public boolean isAllChunksUploaded() {
        return totalChunkCount == uploadedChunks.size()
                || ! script().exists();
    }

    public int progress() {
        return Math.min(100, 100 * uploadedChunks.size() / totalChunkCount);
    }
}
