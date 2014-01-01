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
package org.spoutcraft.client.nterface;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import org.spout.math.TrigMath;
import org.spout.math.imaginary.Quaternionf;
import org.spout.math.vector.Vector3f;
import org.spout.math.vector.Vector3i;
import org.spout.renderer.Camera;
import org.spout.renderer.GLVersioned.GLVersion;
import org.spout.renderer.data.Color;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.nterface.mesh.ParallelChunkMesher;
import org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel;
import org.spoutcraft.client.nterface.mesh.StandardChunkMesher;
import org.spoutcraft.client.nterface.render.Renderer;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;
import org.spoutcraft.client.util.ticking.TickingElement;

/**
 * Contains and manages the renderer, GUI and it's input and camera input. Meshes and renders chunks and entities.
 */
public class Interface extends TickingElement {
    public static final int TPS = 60;
    public static final float SPOT_CUTOFF = (float) (TrigMath.atan(100 / 50) / 2);
    private final Game game;
    private final ParallelChunkMesher mesher = new ParallelChunkMesher(new StandardChunkMesher());
    private final Map<Vector3i, ChunkModel> chunkModels = new HashMap<>();
    private long worldLastUpdateNumber;
    private final TObjectLongMap<Vector3i> chunkLastUpdateNumbers = new TObjectLongHashMap<>();

    /**
     * Constructs a new interface from the game.
     *
     * @param game The game
     */
    public Interface(Game game) {
        super("interface", TPS);
        this.game = game;
    }

    @Override
    public void onStart() {
        System.out.println("Interface start");

        // TEST CODE
        Renderer.setGLVersion(GLVersion.GL30);
        Renderer.init();
        Renderer.getCamera().setPosition(new org.spout.math.vector.Vector3f(0, 5, 10));
        Renderer.setLightPosition(new org.spout.math.vector.Vector3f(0, 50, 50));
        Renderer.setLightDirection(new org.spout.math.vector.Vector3f(0, -TrigMath.cos(SPOT_CUTOFF), -TrigMath.sin(SPOT_CUTOFF)));
        Renderer.setSolidColor(Color.BLUE);
        Mouse.setGrabbed(true);
    }

    @Override
    public void onTick() {
        if (Display.isCloseRequested()) {
            game.exit();
        }
        updateChunkModels(game.getUniverse().getActiveWorldSnapshot());
        processInput(1f / 20);
        Renderer.render();
    }

    @Override
    public void onStop() {
        System.out.println("Interface stop");

        mesher.shutdown();
        // Updating with a null world will clear all models
        updateChunkModels(null);
        Renderer.dispose();
    }

    private void updateChunkModels(WorldSnapshot world) {
        // If we have no world, remove all chunks
        if (world == null) {
            for (ChunkModel model : chunkModels.values()) {
                // Remove and destroy the model
                removeChunkModel(model, true);
            }
            chunkModels.clear();
            chunkLastUpdateNumbers.clear();
            worldLastUpdateNumber = 0;
            return;
        }
        // If the snapshot hasn't updated there's nothing to do
        if (world.getUpdateNumber() <= worldLastUpdateNumber) {
            return;
        }
        // Else, we need to update the chunk models
        final Map<Vector3i, ChunkSnapshot> chunks = world.getChunks();
        // Remove chunks we don't need anymore
        for (Iterator<Entry<Vector3i, ChunkModel>> iterator = chunkModels.entrySet().iterator(); iterator.hasNext(); ) {
            final Entry<Vector3i, ChunkModel> chunkModel = iterator.next();
            final Vector3i position = chunkModel.getKey();
            // If a model is not in the world chunk collection, we remove
            if (!chunks.containsKey(position)) {
                final ChunkModel model = chunkModel.getValue();
                // Remove the model, destroying it
                removeChunkModel(model, true);
                // Finally, remove the chunk from the collections
                iterator.remove();
                chunkLastUpdateNumbers.remove(position);
            }
        }
        // Next go through all the chunks, and update the chunks that are out of date
        for (ChunkSnapshot chunk : chunks.values()) {
            // If the chunk model is out of date
            if (chunk.getUpdateNumber() > chunkLastUpdateNumbers.get(chunk.getPosition())) {
                final Vector3i position = chunk.getPosition();
                // If we have a previous model remove it to be replaced
                final ChunkModel previous = chunkModels.get(position);
                if (previous != null) {
                    // Don't destroy the model, we'll keep it to render until the new chunk is ready
                    removeChunkModel(previous, false);
                }
                // Add the new model
                addChunkModel(chunk, previous);
            }
        }
        // Update the world update number
        worldLastUpdateNumber = world.getUpdateNumber();
        // Safety precautions
        if (Renderer.getModels().size() > chunkModels.size()) {
            System.out.println("There are more models in the renderer (" + Renderer.getModels().size() + ") than there are chunk models " + chunkModels.size() + "), leak?");
        }
    }

    private void addChunkModel(ChunkSnapshot chunk, ChunkModel previous) {
        final ChunkModel model = mesher.queue(chunk);
        final Vector3i position = chunk.getPosition();
        model.setPosition(position.mul(16).toFloat());
        model.setRotation(Quaternionf.IDENTITY);
        // The previous model is kept to prevent frames with missing chunks because they're being meshed
        model.setPrevious(previous);
        Renderer.addSolidModel(model);
        chunkModels.put(position, model);
        chunkLastUpdateNumbers.put(position, chunk.getUpdateNumber());
    }

    private void removeChunkModel(ChunkModel model, boolean destroy) {
        Renderer.removeModel(model);
        if (destroy) {
            // TODO: recycle the vertex array?
            model.destroy();
        }
    }

    /**
     * Returns the game.
     *
     * @return The game
     */
    public Game getGame() {
        return game;
    }

    // TEST CODE
    // TODO: properly handle user input
    private static float cameraPitch = 0;
    private static float cameraYaw = 0;
    private static float mouseSensitivity = 0.08f;
    private static float cameraSpeed = 0.2f;
    private static boolean mouseGrabbed = true;

    private static void processInput(float dt) {
        dt /= (1f / TPS);
        final boolean mouseGrabbedBefore = mouseGrabbed;
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_ESCAPE:
                        mouseGrabbed ^= true;
                        break;
                    case Keyboard.KEY_F2:
                        Renderer.saveScreenshot();
                }
            }
        }
        final Camera camera = Renderer.getCamera();
        if (Display.isActive()) {
            if (mouseGrabbed != mouseGrabbedBefore) {
                Mouse.setGrabbed(!mouseGrabbedBefore);
            }
            if (mouseGrabbed) {
                final float sensitivity = mouseSensitivity * dt;
                cameraPitch -= Mouse.getDX() * sensitivity;
                cameraPitch %= 360;
                final Quaternionf pitch = Quaternionf.fromAngleDegAxis(cameraPitch, 0, 1, 0);
                cameraYaw += Mouse.getDY() * sensitivity;
                cameraYaw %= 360;
                final Quaternionf yaw = Quaternionf.fromAngleDegAxis(cameraYaw, 1, 0, 0);
                camera.setRotation(pitch.mul(yaw));
            }
            final Vector3f right = camera.getRight();
            final Vector3f up = camera.getUp();
            final Vector3f forward = camera.getForward();
            Vector3f position = camera.getPosition();
            final float speed = cameraSpeed * dt;
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
                position = position.add(forward.mul(speed));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                position = position.add(forward.mul(-speed));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
                position = position.add(right.mul(speed));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
                position = position.add(right.mul(-speed));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
                position = position.add(up.mul(speed));
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                position = position.add(up.mul(-speed));
            }
            camera.setPosition(position);
        }
    }
}
