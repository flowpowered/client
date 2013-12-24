package org.spoutcraft.client.networking.message;

import com.flowpowered.networking.Message;

public class KeepAliveMessage implements Message {
    private final int random;

    public KeepAliveMessage(int random) {
        this.random = random;
    }

    public int getRandom() {
        return random;
    }

    @Override
    public boolean isAsync() {
        return true;
    }
}
