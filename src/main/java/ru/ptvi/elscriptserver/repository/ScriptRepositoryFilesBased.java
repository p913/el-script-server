package ru.ptvi.elscriptserver.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.ptvi.elscriptserver.config.ScriptServerProperties;
import ru.ptvi.elscriptserver.domain.Script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class ScriptRepositoryFilesBased implements ScriptRepository {

    private final ScriptServerProperties scriptServerProperties;

    @Override
    public Optional<Script> findByName(String name) {
        File file = new File(scriptServerProperties.getPathToScripts(), name);
        if (!file.exists())
            return Optional.empty();
        else
            return Optional.of(
                    new Script(name,
                            (int)file.length(),
                            LocalDateTime.ofEpochSecond(file.lastModified() / 1000, 0, ZoneOffset.UTC))
            );
    }

    @Override
    public byte[] getChunkData(Script script, int offset, int length) {
        File file = new File(scriptServerProperties.getPathToScripts(), script.name());
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            byte[] buf = new byte[length];
            if (offset != randomAccessFile.skipBytes(offset))
                throw new IOException(String.format("Can't skip %d bytes", offset));
            if (length != randomAccessFile.read(buf))
                throw new IOException(String.format("Can't read %d bytes", length));
            return buf;
        } catch (IOException e) {
            throw new RuntimeException(
                    String.format("Can't get %d bytes of script file '%s' (size %d bytes) at offset %d",
                            length, file.getAbsolutePath(), script.size(), offset),
                    e);
        }
    }
}
