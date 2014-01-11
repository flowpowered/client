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

import org.lwjgl.input.Keyboard;

import org.spout.math.vector.Vector3f;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.Input;
import org.spoutcraft.client.physics.entity.Entity;
import org.spoutcraft.client.physics.entity.Player;
import org.spoutcraft.client.physics.entity.snapshot.EntitySnapshot;
import org.spoutcraft.client.physics.entity.snapshot.PlayerSnapshot;

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

    public Physics(Game game) {
        super("physics", TPS);
        this.game = game;
    }

    @Override
    public void onStart() {
        System.out.println("Physics start");

        // TEST CODE
        player.set(new Player(0, "Spoutcrafty", null, new Vector3f(0, 18, 0), null));
    }

    @Override
    public void onTick(long dt) {
        updatePlayer(dt / 1000000000f);
        updateSnapshots();

        // TODO: process messages that spawn the player to create and set the field
        // TODO: process messages that set player position, head rotation, and other data
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
        // Update the player head rotation
        player.setHeadRotation(game.getInterface().getCameraRotation());
        // Get the input
        final Input input = game.getInput();
        // Only use the input if active
        if (!input.isActive()) {
            return;
        }
        // Get the direction vectors
        final Vector3f right = player.getRight();
        final Vector3f up = player.getUp();
        final Vector3f forward = player.getForward();
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
