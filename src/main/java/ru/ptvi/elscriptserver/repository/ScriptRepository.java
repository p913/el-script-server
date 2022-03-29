package ru.ptvi.elscriptserver.repository;

import ru.ptvi.elscriptserver.domain.Script;

import java.util.Optional;

public interface ScriptRepository {
    Optional<Script> findByName(String name);

    byte[] getChunkData(Script script, int offset, int length);
}
