package org.spoutcraft.client.networking;

import java.net.SocketAddress;

import com.flowpowered.networking.NetworkClient;
import com.flowpowered.networking.protocol.Protocol;
import com.flowpowered.networking.session.BasicSession;
import com.flowpowered.networking.session.Session;
import io.netty.channel.Channel;

/**
 * The network entry point for the client. Handles connecting to the server as well as creating {@link Session}s.
 */
public class GameNetworkClient extends NetworkClient {
    public GameNetworkClient(SocketAddress remoteAddress) {
        super(remoteAddress, new ClientProtocol("Client", 25565));
    }

    @Override
    public Session newSession(Channel channel, Protocol protocol) {
        return new ClientSession(channel, protocol);
    }

    @Override
    public void sessionInactivated(Session session) {
        //TODO Clear all session references here
        //TODO Show disconnected GUI to client
    }
}
