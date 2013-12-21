package org.spoutcraft.client.networking;

import com.flowpowered.networking.protocol.Protocol;
import com.flowpowered.networking.session.PulsingSession;
import io.netty.channel.Channel;

/**
 * Represents an open connection to the service. All {@link com.flowpowered.networking.Message}s are sent through the session
 */
public class ClientSession extends PulsingSession {
    public ClientSession(Channel channel, Protocol bootstrapProtocol) {
        super(channel, bootstrapProtocol);
    }
}
