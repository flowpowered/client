package org.spoutcraft.client.network.message;

import com.flowpowered.networking.Message;

public abstract class ChannelMessage implements Message {
    private final Channel[] channels;

    public ChannelMessage() {
        this.channels = new Channel[0];
    }

    public ChannelMessage(Channel[] channels) {
        this.channels = channels;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    public Channel[] getChannels() {
        return channels;
    }

    /**
     * An enum of all the message channels.
     */
    public static enum Channel {
        UNIVERSE,
        INTERFACE,
        NETWORK,
        PHYSICS
    }
}


