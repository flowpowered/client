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
        // TEST CODE
        final WorldSnapshot world = game.getUniverse().getActiveWorldSnapshot();

        if (world == null) {
            for (Model model : chunkModels.values()) {
                Renderer.removeModel(model);
            }
            chunkModels.clear();
            return;
        }

        final Map<Vector3i, ChunkSnapshot> chunks = world.getChunks();

        for (Iterator<Entry<Vector3i, Model>> iterator = chunkModels.entrySet().iterator(); iterator.hasNext(); ) {
            final Entry<Vector3i, Model> chunkModel = iterator.next();
            if (!chunks.containsKey(chunkModel.getKey())) {
                // TODO: recycle the vertex arrays?
                final Model model = chunkModel.getValue();
                Renderer.removeModel(model);
                model.getVertexArray().destroy();
                iterator.remove();
            }
        }

        for (ChunkSnapshot chunk : chunks.values()) {
            if (!chunkModels.containsKey(chunk.getPosition())) {
                addChunkModel(world, chunk);
            }
        }

        if (Display.isCloseRequested()) {
            // TODO: untie Main from the game code
            Main.exit();
        }
        processInput(1f / 20);
        Renderer.render();
    }

    private void addChunkModel(WorldSnapshot world, ChunkSnapshot chunk) {
        final Model model = Renderer.addSolid(mesher.mesh(new ChunkSnapshotGroup(chunk, world)).build(), chunk.getPosition().mul(16).toFloat(), Quaternionf.IDENTITY);
        chunkModels.put(chunk.getPosition(), model);
    }

    @Override
    public void onStop() {
        System.out.println("Interface stop");

        Renderer.dispose();
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
