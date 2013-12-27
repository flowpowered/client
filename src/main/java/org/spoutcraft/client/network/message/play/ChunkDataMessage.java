package org.spoutcraft.client.network.message.play;

import org.spoutcraft.client.network.message.ChannelMessage;

public class ChunkDataMessage extends ChannelMessage {

    @Override
    public boolean isAsync() {
        return true;
    }
}
