package ru.ptvi.elscriptserver.event;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.ptvi.elscriptserver.domain.UploadingProcess;
import ru.ptvi.elscriptserver.repository.UploadingProcessRepository;
import ru.ptvi.elscriptserver.service.UploadingProcessService;
import ru.ptvi.elscriptserver.service.ScriptService;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
public class EventsListener {
    private final EventHandler<ChunkRequested> chunkRequestedEventHandler;

    private final EventHandler<ChannelClosed> channelClosedEventHandler;

    public EventsListener(UploadingProcessService uploadingProcessService, ScriptService scriptService, UploadingProcessRepository uploadingProcessRepository) {
        this.chunkRequestedEventHandler = new NonFirstChunkProcessEventHandler(uploadingProcessRepository)
                .setNext(new FirstChunkRequestedEventHandler(uploadingProcessRepository, scriptService)
                    .setNext(new FirstChunkResetProcessEventHandler(uploadingProcessRepository, scriptService)));

        this.channelClosedEventHandler = new ChannelClosedEventHandler(uploadingProcessRepository, uploadingProcessService);
    }

    @EventListener
    public void handleChannelClosed(ChannelClosed event) {
        channelClosedEventHandler.handle(event);
    }

    @EventListener
    public void handleChunkRequested(ChunkRequested event) {
        chunkRequestedEventHandler.handle(event);
    }

    public interface EventHandler <E> {
        void handle(E event);
    }

    public static abstract class EventHandlerInChainOfResponsibility<E> implements EventHandler<E> {
        private EventHandler<E> nextHandler = null;

        public EventHandler<E> setNext(EventHandler<E> nextHandler) {
            this.nextHandler = nextHandler;
            return this;
        }

        protected void callNextHandler(E event) {
            if (nextHandler != null)
                nextHandler.handle(event);
        }
    }

    @RequiredArgsConstructor
    public static class FirstChunkRequestedEventHandler extends EventHandlerInChainOfResponsibility<ChunkRequested> {
        private final UploadingProcessRepository uploadingProcessRepository;

        private final ScriptService scriptService;

        @Override
        public void handle(ChunkRequested event) {
            Optional<UploadingProcess> found = uploadingProcessRepository.getByEquipmentAndScript(event.equipmentId(), event.script().name());
            if (found.isEmpty()) {
                UploadingProcess process = new UploadingProcess(
                        event.equipmentId(),
                        event.script(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now(),
                        event.channelId(),
                        scriptService.calcCountOfChunks(event.script().size()),
                        1,
                        0);
                process.uploadedChunks().add(event.chunkNumber());
                uploadingProcessRepository.store(process);
            } else
                  callNextHandler(event);
        }
    }

    @RequiredArgsConstructor
    public static class FirstChunkResetProcessEventHandler extends EventHandlerInChainOfResponsibility<ChunkRequested> {
        private final UploadingProcessRepository uploadingProcessRepository;

        private final ScriptService scriptService;

        @Override
        public void handle(ChunkRequested event) {
            Optional<UploadingProcess> found = uploadingProcessRepository.getByEquipmentAndScript(event.equipmentId(), event.script().name());
            if (found.isPresent()
                    && event.chunkNumber() == 0
                    && found.get().uploadedChunks().stream().anyMatch(c -> c != 0)) {

                UploadingProcess process =  new UploadingProcess(
                        event.equipmentId(),
                        event.script(),
                        OffsetDateTime.now(),
                        OffsetDateTime.now(),
                        event.channelId(),
                        scriptService.calcCountOfChunks(event.script().size()),
                        1,
                        0);
                process.uploadedChunks().add(event.chunkNumber());
                uploadingProcessRepository.store(process);
            } else
                callNextHandler(event);
        }
    }

    @RequiredArgsConstructor
    public static class NonFirstChunkProcessEventHandler extends EventHandlerInChainOfResponsibility<ChunkRequested> {
        private final UploadingProcessRepository uploadingProcessRepository;

        @Override
        public void handle(ChunkRequested event) {
            Optional<UploadingProcess> found = uploadingProcessRepository.getByEquipmentAndScript(event.equipmentId(), event.script().name());
            if (found.isPresent()) {
                UploadingProcess process = found.get().toBuilder()
                        .requestCount(found.get().requestCount() + 1)
                        .connectionStartTime(found.get().connectionStartTime())
                        .connectionEndTime(null)
                        .connectionId(event.channelId())
                        .retryCount(found.get().uploadedChunks().contains(event.chunkNumber())
                                ? found.get().retryCount() + 1
                                : found.get().retryCount())
                        .build();
                process.uploadedChunks().add(event.chunkNumber());

                uploadingProcessRepository.store(process);
            } else
                callNextHandler(event);
        }
    }

    @RequiredArgsConstructor
    public static class ChannelClosedEventHandler extends EventHandlerInChainOfResponsibility<ChannelClosed> {
        private final UploadingProcessRepository uploadingProcessRepository;

        private final UploadingProcessService uploadingProcessService;

        @Override
        public void handle(ChannelClosed event) {
            Optional<UploadingProcess> process = uploadingProcessRepository.getByConnectionId(event.channelId());
            if (process.isPresent()) {
                if (process.get().isAllChunksUploaded()) {
                    uploadingProcessService.bringProcessToFinishedStatus(process.get());
                } else {
                    uploadingProcessRepository.store(process.get()
                            .toBuilder()
                            .connectionEndTime(OffsetDateTime.now())
                            .build());
                }
            } else
                callNextHandler(event);
        }
    }

}
