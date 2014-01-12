/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.physics;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.flowpowered.commons.ticking.TickingElement;
import org.lwjgl.input.*;
import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.Input;
import org.spoutcraft.client.network.message.ChannelMessage;
import org.spoutcraft.client.network.message.play.JoinGameMessage;
import org.spoutcraft.client.network.message.play.PositionLookMessage;
import org.spoutcraft.client.network.message.play.SpawnPositionMessage;
import org.spoutcraft.client.nterface.snapshot.CameraSnapshot;
import org.spoutcraft.client.physics.entity.Entity;
import org.spoutcraft.client.physics.entity.Player;
import org.spoutcraft.client.physics.snapshot.EntitySnapshot;
import org.spoutcraft.client.physics.snapshot.PlayerSnapshot;
import org.spoutcraft.client.util.AnnotatedMessageHandler;
import org.spoutcraft.client.util.AnnotatedMessageHandler.Handle;

import org.spout.math.vector.Vector3f;

/**
 *
 */
public class Physics extends TickingElement {
    public static final int TPS = 60;
    /**
     * Player speed in block per seconds
     */
    private static final float PLAYER_SPEED = 0.2f;
    private final Game game;
    private final AtomicReference<Player> player = new AtomicReference<>(null);
    private final AtomicReference<PlayerSnapshot> playerSnapshot = new AtomicReference<>(null);
    private final Map<Integer, Entity> entities = new ConcurrentHashMap<>();
    private final Map<Integer, EntitySnapshot> entitySnapshots = new ConcurrentHashMap<>();
    private final AnnotatedMessageHandler handler;

    public Physics(Game game) {
        super("physics", TPS);
        this.game = game;
        handler = new AnnotatedMessageHandler(this);
    }

    @Override
    public void onStart() {
        System.out.println("Physics start");
    }

    @Override
    public void onTick(long dt) {
        updatePlayer(dt / 1000000000f);
        updateSnapshots();

        final Iterator<ChannelMessage> messages = game.getNetwork().getChannel(ChannelMessage.Channel.PHYSICS);
        while (messages.hasNext()) {
            final ChannelMessage message = messages.next();
            handleMessage(message);
            messages.remove();
        }
        // TODO: process messages that spawn the player to create and set the field
        // TODO: process messages that set player position, head rotation, and other data
    }

    private void handleMessage(ChannelMessage message) {
        handler.handle(message);
        message.markChannelRead(ChannelMessage.Channel.PHYSICS);
    }

    @Handle
    public void handleJoinGame(JoinGameMessage message) {
        player.set(new Player(message.getPlayerId(), game.getNetwork().getSession().getUsername(), game.getUniverse().getActiveWorldSnapshot(), new Vector3f(0, 18, 0), game.getNetwork().getSession()));
    }

    @Handle
    public void handleSpawnPosition(SpawnPositionMessage message) {
        if (game.getNetwork().isRunning()) {
            // TODO Might be wrong spot
            // TODO Spout Math Quaternion should have a getYaw/Pitch/Roll()?
            // TODO DDoS you'll need to create the yaw/pitch correctly per Mojang's strange strange trig math
            game.getNetwork().getSession().send(new PositionLookMessage(message.getX(), message.getY(), message.getZ(), 0f, 0f, true, message.getY() + 1));
        }
    }

    @Handle
    public void handlePositionLook(PositionLookMessage message) {
        player.get().setPosition(new Vector3f(message.getX(), message.getY(), message.getZ()));
    }

    private void updateSnapshots() {
        final Player player = this.player.get();
        if (player == null) {
            playerSnapshot.set(null);
        } else {
            PlayerSnapshot playerSnapshot = this.playerSnapshot.get();
            if (playerSnapshot == null) {
                playerSnapshot = new PlayerSnapshot(player);
                this.playerSnapshot.set(playerSnapshot);
            }
            playerSnapshot.update(player);
        }
        for (Iterator<EntitySnapshot> iterator = entitySnapshots.values().iterator(); iterator.hasNext(); ) {
            if (!entities.containsKey(iterator.next().getId())) {
                iterator.remove();
            }
        }
        for (Entry<Integer, Entity> entry : entities.entrySet()) {
            final int id = entry.getKey();
            final Entity entity = entry.getValue();
            EntitySnapshot entitySnapshot = entitySnapshots.get(id);
            if (entitySnapshot == null) {
                entitySnapshot = new EntitySnapshot(entity);
                entitySnapshots.put(id, entitySnapshot);
            }
            entitySnapshot.update(entity);
        }
    }

    private void updatePlayer(float dt) {
        final Player player = this.player.get();
        // No player, no position information to update
        if (player == null) {
            return;
        }
        // Get the input
        final Input input = game.getInput();
        // Only use the input if active
        if (!input.isActive()) {
            return;
        }
        // Get the camera snapshot
        final CameraSnapshot camera = game.getInterface().getCameraSnapshot();
        // Get the direction vectors
        final Vector3f right = camera.getRight();
        final Vector3f up = camera.getUp();
        final Vector3f forward = camera.getForward();
        // Get the old player position
        Vector3f position = player.getPosition();
        // Adjust the player speed to the FPS
        final float speed = PLAYER_SPEED * TPS * dt;
        // Calculate the new player position
        if (input.isKeyDown(Keyboard.KEY_W)) {
            position = position.add(forward.mul(speed));
        }
        if (input.isKeyDown(Keyboard.KEY_S)) {
            position = position.add(forward.mul(-speed));
        }
        if (input.isKeyDown(Keyboard.KEY_A)) {
            position = position.add(right.mul(speed));
        }
        if (input.isKeyDown(Keyboard.KEY_D)) {
            position = position.add(right.mul(-speed));
        }
        if (input.isKeyDown(Keyboard.KEY_SPACE)) {
            position = position.add(up.mul(speed));
        }
        if (input.isKeyDown(Keyboard.KEY_LSHIFT)) {
            position = position.add(up.mul(-speed));
        }
        // Update the player position
        player.setPosition(position);
    }

    @Override
    public void onStop() {
        System.out.println("Physics stop");
    }

    public PlayerSnapshot getPlayerSnapshot() {
        return playerSnapshot.get();
    }

    public EntitySnapshot getEntitySnapshot(int id) {
        return entitySnapshots.get(id);
    }

    public Game getGame() {
        return game;
    }
}
