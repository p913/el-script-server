package ru.ptvi.elscriptserver.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ptvi.elscriptserver.domain.Script;
import ru.ptvi.elscriptserver.domain.ScriptChunk;
import ru.ptvi.elscriptserver.repository.ScriptRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScriptService {
    private static final int CHUNK_SIZE = 256;

    private final ScriptRepository scriptRepository;

    public Script getByName(String name) {
        return scriptRepository.findByName(name)
                .orElse(getNonExistingScript(name));
    }

    private Script getNonExistingScript(String scriptName) {
        return new Script(scriptName, -1, LocalDateTime.now());
    }

    public ScriptChunk getChunk(Script script, int number) {
        if (script.exists())
            return getRealChunk(script, number);
        else
            return getFakeChunkForStopDownloading(script, number);
    }

    private ScriptChunk getRealChunk(Script script, int number) {
        int offset = CHUNK_SIZE * number;
        int length = script.size() - offset;
        if (length < 0)
            throw new RuntimeException(String.format("Requested chunk #%d points out of file size (%d bytes)",
                    number, script.size()));
        else if (length == 0)
            return new ScriptChunk(script, number, true, new byte[0]);
        else if (length == CHUNK_SIZE)
            return new ScriptChunk(script, number, true,
                    scriptRepository.getChunkData(script, offset, length));
        else
            return new ScriptChunk(script, number, false,
                    scriptRepository.getChunkData(script, offset, Math.min(CHUNK_SIZE, length)));
    }

    // Терминал делает много retry при запросе первого куска скрипта, чтобы их прекратить
    // и остановить загрузку вернем  некий кусок, по которому терминал должен признать скрипт невалидным
    private ScriptChunk getFakeChunkForStopDownloading(Script script, int number) {
        return new ScriptChunk(script, number, true, new byte[0]);
    }

    // Куски по 256 байт. Последний кусок не должен иметь размер 256 байт - так терминал определяет,
    // что кусок последний. Если скрипт всего 256 байт или кратен 256 байт, то последний кусок нулевой длины.
    public int calcCountOfChunks(int scriptSize) {
        return scriptSize % CHUNK_SIZE == 0
                ? scriptSize / CHUNK_SIZE
                : scriptSize / CHUNK_SIZE + 1;
    }
}
