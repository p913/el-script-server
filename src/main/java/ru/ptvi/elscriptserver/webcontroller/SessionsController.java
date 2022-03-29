package ru.ptvi.elscriptserver.webcontroller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ptvi.elscriptserver.domain.UploadingProcess;
import ru.ptvi.elscriptserver.domain.FinishedProcess;
import ru.ptvi.elscriptserver.service.FinishedProcessService;
import ru.ptvi.elscriptserver.service.UploadingProcessService;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class SessionsController {
    private final UploadingProcessService uploadingProcessService;

    private final FinishedProcessService finishedProcessService;

    private final DomainToDtoConverter domainToDtoConverter = new DomainToDtoConverter();

    @GetMapping("/r/sessions")
    SessionsDto getAllSessions() {
        return new SessionsDto(
                uploadingProcessService.getAllCurrentUploadProcesses()
                        .stream()
                        .map(domainToDtoConverter::uploadingProcessToActiveSession)
                        .sorted()
                        .collect(Collectors.toList()),
                finishedProcessService.getAllFinishedProcesses()
                        .stream()
                        .map(domainToDtoConverter::finishedProcessToFinishedSessionDto)
                        .sorted()
                        .collect(Collectors.toList()));
    }

    @Getter
    @AllArgsConstructor
    static class SessionsDto {
        Collection<ActiveSessionDto> active;

        Collection<FinishedSessionDto> finished;
    }

    @Value
    static class ActiveSessionDto implements Comparable<ActiveSessionDto> {
        @NonNull
        String equipmentId;

        @NonNull
        String scriptName;

        int scriptSize;

        @NonNull
        OffsetDateTime sessionStartTime;

        @NonNull
        OffsetDateTime estimatedEndTime;

        Integer connectionDuration;

        int progress;

        int requestCount;

        int retryCount;

        @Override
        public int compareTo(ActiveSessionDto o) {
            return - sessionStartTime.compareTo(o.getSessionStartTime());
        }
    }

    @Value
    static class FinishedSessionDto implements Comparable<FinishedSessionDto> {
        @NonNull
        String equipmentId;

        @NonNull
        String scriptName;

        int scriptSize;

        @NonNull
        OffsetDateTime sessionStartTime;

        int sessionDuration;

        @NonNull
        FinishedSessionStatus sessionStatus;

        @Override
        public int compareTo(FinishedSessionDto o) {
            return - sessionStartTime.compareTo(o.getSessionStartTime());
        }
    }

    enum FinishedSessionStatus {
        SUCCESS,
        INCOMPLETE,
        SCRIPT_MISSED;
    }

    class DomainToDtoConverter {
        ActiveSessionDto uploadingProcessToActiveSession(UploadingProcess uploadingProcess) {
            return new ActiveSessionDto(uploadingProcess.equipmentId(),
                    uploadingProcess.script().name(),
                    uploadingProcess.script().size(),
                    uploadingProcess.uploadStartTime(),
                    uploadingProcessService.estimateEndTime(uploadingProcess),
                    (uploadingProcess.connectionAlive()
                            ? (int)uploadingProcess.connectionStartTime().until(OffsetDateTime.now(), ChronoUnit.SECONDS)
                            : null),
                    uploadingProcess.progress(),
                    uploadingProcess.requestCount(),
                    uploadingProcess.retryCount());
        }

        FinishedSessionDto finishedProcessToFinishedSessionDto(FinishedProcess finishedProcess) {
            return new FinishedSessionDto(finishedProcess.equipmentId(),
                    finishedProcess.script().name(),
                    finishedProcess.script().size(),
                    finishedProcess.uploadStartTime(),
                    (int)finishedProcess.uploadStartTime().until(finishedProcess.uploadEndTime(), ChronoUnit.SECONDS),
                     finishedProcessToFinishedSessionStatus(finishedProcess));
        }

        FinishedSessionStatus finishedProcessToFinishedSessionStatus(FinishedProcess finishedProcess) {
            if (!finishedProcess.script().exists())
                return FinishedSessionStatus.SCRIPT_MISSED;
            else if (finishedProcess.totalChunkCount() == finishedProcess.uploadedChunkCount())
                return FinishedSessionStatus.SUCCESS;
            else
                return FinishedSessionStatus.INCOMPLETE;
        }
    }

}
