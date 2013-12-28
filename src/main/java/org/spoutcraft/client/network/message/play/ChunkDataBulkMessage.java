package org.spoutcraft.client.network.message.play;

import org.spoutcraft.client.network.message.ChannelMessage;

public class ChunkDataBulkMessage extends ChannelMessage {
    @Override
    public boolean isAsync() {
        return true;
    }
}
