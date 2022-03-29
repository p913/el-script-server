package ru.ptvi.elscriptserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ptvi.elscriptserver.config.ScriptServerProperties;
import ru.ptvi.elscriptserver.domain.FinishedProcess;
import ru.ptvi.elscriptserver.domain.UploadingProcess;
import ru.ptvi.elscriptserver.repository.FinishedProcessRepository;

import java.time.OffsetDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FinishedProcessService {
    private final ScriptServerProperties scriptServerProperties;

    private final FinishedProcessRepository finishedProcessRepository;

    public void addProcessAsFinished(UploadingProcess process) {
        finishedProcessRepository.store(FinishedProcess.fromUploadingProcess(process));
    }

    public Collection<FinishedProcess> getAllFinishedProcesses() {
        return finishedProcessRepository.getAll();
    }

    @Scheduled(fixedDelayString = "PT2H")
    public void deleteOutdatedProcesses() {
        OffsetDateTime deleteBefore = OffsetDateTime.now().minusHours(scriptServerProperties.getFinishedScriptStoreHours());
        finishedProcessRepository.getAll()
                .stream()
                .filter(p -> p.uploadEndTime().isBefore(deleteBefore))
                .forEach(finishedProcessRepository::delete);
    }
}
