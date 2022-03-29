package ru.ptvi.elscriptserver.domain;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

@Value
@Accessors(fluent = true)
@Builder(toBuilder = true)
@AllArgsConstructor
public class FinishedProcess {
    @NonNull
    @EqualsAndHashCode.Include
    String equipmentId;

    @NonNull
    @EqualsAndHashCode.Include
    Script script;

    @NonNull
    @EqualsAndHashCode.Include
    OffsetDateTime uploadStartTime;

    @NonNull
    OffsetDateTime uploadEndTime;

    int totalChunkCount;

    int uploadedChunkCount;

    int requestCount;

    int retryCount;

    public static FinishedProcess fromUploadingProcess(UploadingProcess uploadingProcess) {
        return new FinishedProcess(
                uploadingProcess.equipmentId(),
                uploadingProcess.script(),
                uploadingProcess.uploadStartTime(),
                uploadingProcess.connectionEndTime() == null ? OffsetDateTime.now() : uploadingProcess.connectionEndTime(),
                uploadingProcess.totalChunkCount(),
                uploadingProcess.uploadedChunks().size(),
                uploadingProcess.requestCount(),
                uploadingProcess.retryCount()
        );
    }
}
