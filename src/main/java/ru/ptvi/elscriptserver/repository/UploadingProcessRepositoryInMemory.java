package ru.ptvi.elscriptserver.repository;

import org.springframework.stereotype.Repository;
import ru.ptvi.elscriptserver.domain.UploadingProcess;

import java.util.*;

@Repository
class UploadingProcessRepositoryInMemory implements UploadingProcessRepository {
    private final Set<UploadingProcess> processes = Collections.synchronizedSet(new HashSet<>());

    @Override
    public Collection<UploadingProcess> getAll() {
        return new ArrayList<>(processes);
    }

    @Override
    public Optional<UploadingProcess> getByEquipmentAndScript(String equipmentId, String scriptName) {
        return processes.stream()
                .filter(d -> d.equipmentId().equals(equipmentId) && d.script().name().equals(scriptName))
                .findFirst();
    }

    @Override
    public Optional<UploadingProcess> getByConnectionId(String connectionId) {
        return processes.stream()
                .filter(d -> d.connectionId().equals(connectionId))
                .findFirst();
    }

    @Override
    public void store(UploadingProcess process) {
            processes.remove(process);
            processes.add(process);
    }

    @Override
    public void delete(UploadingProcess process) {
        processes.remove(process);
    }
}
