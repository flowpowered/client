/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.flowpowered.commons.ViewFrustum;
import com.flowpowered.commons.ticking.TickingElement;
import com.flowpowered.math.TrigMath;
import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import org.lwjgl.input.Keyboard;

import org.spout.renderer.api.Camera;
import org.spout.renderer.api.GLVersioned.GLVersion;

import org.spoutcraft.client.Game;
import org.spoutcraft.client.input.Input;
import org.spoutcraft.client.input.event.KeyboardEvent;
import org.spoutcraft.client.nterface.mesh.ParallelChunkMesher;
import org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel;
import org.spoutcraft.client.nterface.mesh.StandardChunkMesher;
import org.spoutcraft.client.nterface.render.Renderer;
import org.spoutcraft.client.nterface.snapshot.CameraSnapshot;
import org.spoutcraft.client.physics.snapshot.PlayerSnapshot;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;
import org.spoutcraft.client.universe.snapshot.WorldSnapshot;
import org.spoutcraft.client.universe.world.Chunk;
import org.spoutcraft.client.universe.world.World;

/**
 * Contains and manages the renderer, GUI and it's input and camera input. Meshes and renders chunks and entities.
 */
public class Interface extends TickingElement {
    public static final int TPS = 60;
    private static final float PI = (float) TrigMath.PI;
    private static final float TWO_PI = 2 * PI;
    private static final float LIGHT_ANGLE_LIMIT = PI / 64;
    private static final Vector3f SHADOWED_CHUNKS = new Vector3f(Chunk.BLOCKS.SIZE * 4, 64, Chunk.BLOCKS.SIZE * 4);
    private static final Vector3f[] CHUNK_VERTICES;
    private static final float MOUSE_SENSITIVITY = 0.08f;
    private final Game game;
    private final Renderer renderer = new Renderer();
    private final ParallelChunkMesher mesher;
    private final Map<Vector3i, ChunkModel> chunkModels = new HashMap<>();
    private long worldLastUpdateNumber;
    private final TObjectLongMap<Vector3i> chunkLastUpdateNumbers = new TObjectLongHashMap<>();
    private final ViewFrustum frustum = new ViewFrustum();
    private float cameraPitch = 0;
    private float cameraYaw = 0;
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean mouseGrabbed = false;
    private final CameraSnapshot cameraSnapshot = new CameraSnapshot();

    static {
        CHUNK_VERTICES = new Vector3f[8];
        CHUNK_VERTICES[0] = new Vector3f(0, 0, Chunk.BLOCKS.SIZE);
        CHUNK_VERTICES[1] = new Vector3f(Chunk.BLOCKS.SIZE, 0, Chunk.BLOCKS.SIZE);
        CHUNK_VERTICES[2] = new Vector3f(Chunk.BLOCKS.SIZE, Chunk.BLOCKS.SIZE, Chunk.BLOCKS.SIZE);
        CHUNK_VERTICES[3] = new Vector3f(0, Chunk.BLOCKS.SIZE, Chunk.BLOCKS.SIZE);
        CHUNK_VERTICES[4] = new Vector3f(0, 0, 0);
        CHUNK_VERTICES[5] = new Vector3f(Chunk.BLOCKS.SIZE, 0, 0);
        CHUNK_VERTICES[6] = new Vector3f(Chunk.BLOCKS.SIZE, Chunk.BLOCKS.SIZE, 0);
        CHUNK_VERTICES[7] = new Vector3f(0, Chunk.BLOCKS.SIZE, 0);
    }

    /**
     * Constructs a new interface from the game.
     *
     * @param game The game
     */
    public Interface(Game game) {
        super("interface", TPS);
        this.game = game;
        mesher = new ParallelChunkMesher(this, new StandardChunkMesher());
    }

    @Override
    public void onStart() {
        game.getLogger().info("Starting interface");
        // Initialize the renderer
        renderer.setGLVersion(GLVersion.GL32);
        renderer.init();
        // Subscribe to the keyboard input queue
        final Input input = game.getInput();
        input.subscribeToKeyboard();
        // This will trigger the mouse to be grabbed properly
        input.getKeyboardQueue().add(new KeyboardEvent(' ', Keyboard.KEY_ESCAPE, true, 1));
    }

    @Override
    public void onTick(long dt) {
        final WorldSnapshot world = game.getUniverse().getActiveWorldSnapshot();
        updateChunkModels(world);
        handleInput(dt / 1000000000f);
        updateLight(world.getTime());
        final Camera camera = renderer.getRenderModelsStage().getCamera();
        frustum.update(camera.getProjectionMatrix(), camera.getViewMatrix());
        renderer.render();
        updateSnapshots();
    }

    @Override
    public void onStop() {
        game.getLogger().info("Stopping interface");
        // We make sure to stop the input because it relies on the display
        game.getInput().stop();
        mesher.shutdown();
        // Updating with a null world will clear all models
        updateChunkModels(null);
        renderer.dispose();
    }

    private void updateLight(long time) {
        time %= World.MILLIS_IN_DAY;
        double lightAngle;
        final double dayAngle = ((double) time / World.MILLIS_IN_DAY) * TWO_PI;
        if (dayAngle < PI) {
            lightAngle = dayAngle;
        } else {
            lightAngle = dayAngle - PI;
        }
        lightAngle = lightAngle / PI * (PI - 2 * LIGHT_ANGLE_LIMIT) + LIGHT_ANGLE_LIMIT;
        final Vector3f direction = new Vector3f(0, -Math.sin(lightAngle), -Math.cos(lightAngle));
        final Vector3f position = renderer.getRenderModelsStage().getCamera().getPosition();
        renderer.updateLight(direction, new Vector3f(position.getX(), 0, position.getZ()), SHADOWED_CHUNKS);
        // TODO: lower light intensity at night
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
            // If the chunk model is out of date and visible
            if (chunk.getUpdateNumber() > chunkLastUpdateNumbers.get(chunk.getPosition())
                    && isChunkVisible(chunk.getPosition())) {
                final Vector3i position = chunk.getPosition();
                // If we have a previous model remove it to be replaced
                final ChunkModel previous = chunkModels.get(position);
                if (previous != null) {
                    // Don't destroy the model, we'll keep it to render until the new chunk is ready
                    removeChunkModel(previous, false);
                    // No need to remove from the collections, it will be replaced in the addChunkModel call
                }
                // Add the new model
                addChunkModel(chunk, previous);
            }
        }
        // Update the world update number
        worldLastUpdateNumber = world.getUpdateNumber();
        // Safety precautions
        if (renderer.getRenderModelsStage().getModels().size() > chunkModels.size()) {
            game.getLogger().warn("There are more models in the renderer (" + renderer.getRenderModelsStage().getModels().size() + ") than there are chunk models " + chunkModels.size() + "), leak?");
        }
    }

    private void addChunkModel(ChunkSnapshot chunk, ChunkModel previous) {
        final ChunkModel model = mesher.queue(chunk);
        final Vector3i position = chunk.getPosition();
        model.setPosition(position.mul(16).toFloat());
        model.setRotation(Quaternionf.IDENTITY);
        // The previous model is kept to prevent frames with missing chunks because they're being meshed
        model.setPrevious(previous);
        renderer.addSolidModel(model);
        chunkModels.put(position, model);
        chunkLastUpdateNumbers.put(position, chunk.getUpdateNumber());
    }

    private void removeChunkModel(ChunkModel model, boolean destroy) {
        renderer.getRenderModelsStage().removeModel(model);
        if (destroy) {
            // TODO: recycle the vertex array?
            model.destroy();
        }
    }

    private void handleInput(float dt) {
        // Calculate the FPS correction factor
        dt *= TPS;
        // Store the old mouse grabbed state
        final boolean mouseGrabbedBefore = mouseGrabbed;
        // Handle keyboard events
        handleKeyboardEvents();
        // Handle the mouse and keyboard inputs, if the input is active
        final Input input = game.getInput();
        if (input.isActive()) {
            // If the mouse grabbed state has changed from the keyboard events, update the mouse grabbed state
            if (mouseGrabbed != mouseGrabbedBefore) {
                input.setMouseGrabbed(mouseGrabbed);
                // If the mouse has just been re-grabbed, ensure that movement when not grabbed will ignored
                if (mouseGrabbed) {
                    mouseX = input.getMouseX();
                    mouseY = input.getMouseY();
                }
            }
            // Handle the mouse input if it's been grabbed
            if (mouseGrabbed) {
                handleMouseInput(dt);
            }
            // Update the camera position to match the player
            final PlayerSnapshot player = game.getPhysics().getPlayerSnapshot();
            if (player != null) {
                renderer.getRenderModelsStage().getCamera().setPosition(player.getPosition());
            }
        }
    }

    private void handleKeyboardEvents() {
        final Queue<KeyboardEvent> keyboardEvents = game.getInput().getKeyboardQueue();
        while (!keyboardEvents.isEmpty()) {
            final KeyboardEvent event = keyboardEvents.poll();
            if (event.wasPressedDown()) {
                switch (event.getKey()) {
                    case Keyboard.KEY_ESCAPE:
                        mouseGrabbed ^= true;
                        break;
                    case Keyboard.KEY_F2:
                        renderer.saveScreenshot(new File(""));
                }
            }
        }
    }

    private void handleMouseInput(float dt) {
        // Get the input
        final Input input = game.getInput();
        // Calculate sensitivity adjusted to the FPS
        final float sensitivity = MOUSE_SENSITIVITY * dt;
        // Get the latest mouse x and y
        final int mouseX = input.getMouseX();
        final int mouseY = input.getMouseY();
        // Rotate the camera by the difference from the old and new mouse coordinates
        cameraPitch -= (mouseX - this.mouseX) * sensitivity;
        cameraPitch %= 360;
        final Quaternionf pitch = Quaternionf.fromAngleDegAxis(cameraPitch, 0, 1, 0);
        cameraYaw += (mouseY - this.mouseY) * sensitivity;
        cameraYaw %= 360;
        final Quaternionf yaw = Quaternionf.fromAngleDegAxis(cameraYaw, 1, 0, 0);
        // Set the new camera rotation
        renderer.getRenderModelsStage().getCamera().setRotation(pitch.mul(yaw));
        // Update the last mouse x and y
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    private void updateSnapshots() {
        cameraSnapshot.update(renderer.getRenderModelsStage().getCamera());
    }

    public CameraSnapshot getCameraSnapshot() {
        return cameraSnapshot;
    }

    /**
     * Returns the game.
     *
     * @return The game
     */
    public Game getGame() {
        return game;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    /**
     * Returns true if the chunk is visible, using the default chunk size and the position in world coordinates.
     *
     * @param position The position, in world coordinates
     * @return Whether or not the chunk is visible
     */
    public boolean isChunkVisible(Vector3i position) {
        return frustum.intersectsCuboid(CHUNK_VERTICES, position.getX(), position.getY(), position.getZ());
    }

    /**
     * Returns true if the chunk is visible, using the default chunk size and the position in world coordinates.
     *
     * @param position The position, in world coordinates
     * @return Whether or not the chunk is visible
     */
    public boolean isChunkVisible(Vector3f position) {

        return frustum.intersectsCuboid(CHUNK_VERTICES, position);
    }
}
