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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.spout.renderer.model.Model;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.Main;
import org.spoutcraft.client.nterface.mesh.ChunkMesher;
import org.spoutcraft.client.nterface.mesh.ChunkSnapshotGroup;
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
    private final ChunkMesher mesher = new StandardChunkMesher();
    private final Map<Vector3i, Model> chunkModels = new HashMap<>();
    private long worldLastUpdateNumber;
    private final TObjectLongMap<Vector3i> chunkLastUpdateNumbers = new TObjectLongHashMap<>();

    /**
     * Constructs a new interface from the game.
     *
     * @param game The game
     */
    public Interface(Game game) {
        super(TPS);
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
            // TODO: untie Main from the game code
            Main.exit();
        }

        // TEST CODE
        updateChunkModels(game.getUniverse().getActiveWorldSnapshot());

        processInput(1f / 20);
        Renderer.render();
    }

    @Override
    public void onStop() {
        System.out.println("Interface stop");

        Renderer.dispose();
    }

    private void updateChunkModels(WorldSnapshot world) {
        // If we have no world, remove all chunks
        if (world == null) {
            for (Model model : chunkModels.values()) {
                removeChunkModel(model);
            }
            chunkModels.clear();
            return;
        }
        // If the snapshot hasn't updated there's nothing to do
        if (world.getUpdateNumber() <= worldLastUpdateNumber) {
            return;
        }
        // Else, we need to build a list of chunks to mesh
        final Set<ChunkSnapshot> toMesh = new HashSet<>();
        final Map<Vector3i, ChunkSnapshot> chunks = world.getChunks();
        // Remove chunks we don't need anymore
        for (Iterator<Entry<Vector3i, Model>> iterator = chunkModels.entrySet().iterator(); iterator.hasNext(); ) {
            final Entry<Vector3i, Model> chunkModel = iterator.next();
            final Vector3i position = chunkModel.getKey();
            // If a model is not in the world chunk collection, we remove
            if (!chunks.containsKey(position)) {
                final Model model = chunkModel.getValue();
                // Remove the model
                removeChunkModel(model);
                // Finally, remove the chunk from the collections
                iterator.remove();
                chunkLastUpdateNumbers.remove(position);
            }
        }
        // Next go through all the chunks, and add the chunks that are out of date
        for (ChunkSnapshot chunk : chunks.values()) {
            if (chunk.getUpdateNumber() > chunkLastUpdateNumbers.get(chunk.getPosition())) {
                toMesh.add(chunk);
            }
        }
        // Next mesh those chunks and replace the models that need updating
        for (ChunkSnapshot chunk : toMesh) {
            // Only mesh the existing ones
            final Vector3i position = chunk.getPosition();
            // If we have a previous model remove it to be replaced
            final Model previous = chunkModels.get(position);
            if (previous != null) {
                removeChunkModel(previous);
            }
            // Add the new model
            addChunkModel(chunk);
        }
        // Update the world update number
        worldLastUpdateNumber = world.getUpdateNumber();
        // Safety precautions
        if (Renderer.getModels().size() > chunkModels.size()) {
            System.out.println("They're more models in the renderer than they are chunk models, leak?");
        }
    }

    private void addChunkModel(ChunkSnapshot chunk) {
        final Vector3i position = chunk.getPosition();
        final Model model = Renderer.addSolid(mesher.mesh(new ChunkSnapshotGroup(chunk)).build(), position.mul(16).toFloat(), Quaternionf.IDENTITY);
        chunkModels.put(position, model);
        chunkLastUpdateNumbers.put(position, chunk.getUpdateNumber());
    }

    private void removeChunkModel(Model model) {
        Renderer.removeModel(model);
        // Destroy the vertex array TODO: recycle it?
        model.getVertexArray().destroy();
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
