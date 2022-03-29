package ru.ptvi.elscriptserver.repository;

import ru.ptvi.elscriptserver.domain.UploadingProcess;

import java.util.Collection;
import java.util.Optional;

public interface UploadingProcessRepository {
    Collection<UploadingProcess> getAll();

    Optional<UploadingProcess> getByEquipmentAndScript(String equipmentId, String scriptName);

    Optional<UploadingProcess> getByConnectionId(String connectionId);

    void store(UploadingProcess process);

    void delete(UploadingProcess process);
}
