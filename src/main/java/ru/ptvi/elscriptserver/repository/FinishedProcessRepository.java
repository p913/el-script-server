package ru.ptvi.elscriptserver.repository;

import ru.ptvi.elscriptserver.domain.FinishedProcess;

import java.util.Collection;

public interface FinishedProcessRepository {
    Collection<FinishedProcess> getAll();

    void store(FinishedProcess process);

    void delete(FinishedProcess process);
}
