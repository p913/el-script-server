package ru.ptvi.elscriptserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ptvi.elscriptserver.domain.UploadingProcess;
import ru.ptvi.elscriptserver.repository.UploadingProcessRepository;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadingProcessService {
    private static final int PROCESS_OUTDATING_INTERVAL_IN_MINUTES = 3;

    private final UploadingProcessRepository uploadingProcessRepository;

    private final FinishedProcessService finishedProcessService;

    public OffsetDateTime estimateEndTime(UploadingProcess process) {
        long procDurSec = process.uploadStartTime().until(OffsetDateTime.now(), ChronoUnit.SECONDS);
        return process.uploadStartTime().plusSeconds(100 * procDurSec / Math.max(1, process.progress()));
    }

    public Collection<UploadingProcess> getAllCurrentUploadProcesses() {
        return uploadingProcessRepository.getAll();
    }

    public void bringProcessToFinishedStatus(UploadingProcess process) {
        uploadingProcessRepository.delete(process);

        finishedProcessService.addProcessAsFinished(process);
    }

    @Scheduled(fixedDelayString = "PT1M")
    public void bringOutdatedProcessesToFinishedStatus() {
        OffsetDateTime deleteBefore = OffsetDateTime.now().minusMinutes(PROCESS_OUTDATING_INTERVAL_IN_MINUTES);
        uploadingProcessRepository.getAll()
                .stream()
                .filter(p -> !p.connectionAlive() && p.connectionStartTime().isBefore(deleteBefore))
                .forEach(this::bringProcessToFinishedStatus);
    }

}
