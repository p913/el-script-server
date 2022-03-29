package ru.ptvi.elscriptserver.event;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public class ChannelClosed {
    String channelId;
}
