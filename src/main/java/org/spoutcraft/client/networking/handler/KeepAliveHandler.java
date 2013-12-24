package org.spoutcraft.client.networking.handler;

import com.flowpowered.networking.MessageHandler;
import com.flowpowered.networking.session.Session;
import org.spoutcraft.client.networking.message.KeepAliveMessage;

public class KeepAliveHandler implements MessageHandler<KeepAliveMessage> {
    @Override
    public void handle(Session session, KeepAliveMessage message) {
        session.send(new KeepAliveMessage(message.getRandom()));
    }
}
