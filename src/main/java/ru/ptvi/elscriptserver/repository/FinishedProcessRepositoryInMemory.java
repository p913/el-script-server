package ru.ptvi.elscriptserver.repository;

import org.springframework.stereotype.Repository;
import ru.ptvi.elscriptserver.domain.FinishedProcess;

import java.util.*;

@Repository
class FinishedProcessRepositoryInMemory implements FinishedProcessRepository {
    private final Set<FinishedProcess> processes = Collections.synchronizedSet(new HashSet<>());

    @Override
    public Collection<FinishedProcess> getAll() {
        return new ArrayList<>(processes);
    }

    @Override
    public void store(FinishedProcess process) {
        processes.remove(process);
        processes.add(process);
    }

    @Override
    public void delete(FinishedProcess process) {
        processes.remove(process);
    }
}
