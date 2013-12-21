package org.spoutcraft.client.networking;

import com.flowpowered.networking.Codec;
import com.flowpowered.networking.exception.UnknownPacketException;
import com.flowpowered.networking.protocol.HandlerLookupService;
import com.flowpowered.networking.protocol.Protocol;
import io.netty.buffer.ByteBuf;

/**
 * The protocol used for client communication.
 */
public class ClientProtocol extends Protocol {
    private final ClientHandlerLookupService service;

    public ClientProtocol(String name, int defaultPort) {
        super(name, defaultPort, 50);
        service = new ClientHandlerLookupService();

        //TODO Bind messages to handlers here
    }

    @Override
    public Codec<?> readHeader(ByteBuf byteBuf) throws UnknownPacketException {
        return null;
    }

    @Override
    public ByteBuf writeHeader(Codec<?> codec, ByteBuf byteBuf, ByteBuf byteBuf2) {
        return null;
    }
}

final class ClientHandlerLookupService extends HandlerLookupService {
    protected ClientHandlerLookupService() {
        //TODO Bind messages to handlers here
        //bind()
    }
}
