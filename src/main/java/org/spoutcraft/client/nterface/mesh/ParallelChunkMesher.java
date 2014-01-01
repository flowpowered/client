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
package org.spoutcraft.client.nterface.mesh;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.spout.renderer.data.VertexData;
import org.spout.renderer.gl.VertexArray;
import org.spout.renderer.model.Model;

import org.spoutcraft.client.nterface.render.Renderer;
import org.spoutcraft.client.universe.snapshot.ChunkSnapshot;

/**
 * Meshes chunks in parallel. Returns chunk models which may not be rendered when {@link org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel#render()} is called, this is happens when
 * the meshing is in progress. Parallelism is achieved using a {@link java.util.concurrent.ForkJoinPool} with default thread count. Chunks are meshed using the provided {@link
 * org.spoutcraft.client.nterface.mesh.ChunkMesher}.
 *
 * @see org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel
 */
public class ParallelChunkMesher {
    private final ChunkMesher mesher;
    private final ForkJoinPool executor = new ForkJoinPool();

    /**
     * Constructs a new parallel chunk mesher from the actual mesher.
     *
     * @param mesher The chunk mesher
     */
    public ParallelChunkMesher(ChunkMesher mesher) {
        this.mesher = mesher;
    }

    /**
     * Queues a chunk to be meshed, returning a chunk model which can be used normally. The chunk model will actually only renderer the chunk once meshing it complete.
     *
     * @param chunk The chunk to mesh
     * @return The chunk's model
     */
    public ChunkModel queue(ChunkSnapshot chunk) {
        return new ChunkModel(executor.submit(new ChunkMeshTask(chunk)));
    }

    /**
     * Shuts down the executor used for meshing, cancelling any meshing pending or active.
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    private class ChunkMeshTask implements Callable<VertexData> {
        private final ChunkSnapshot toMesh;

        private ChunkMeshTask(ChunkSnapshot toMesh) {
            this.toMesh = toMesh;
        }

        @Override
        public VertexData call() {
            return mesher.mesh(new ChunkSnapshotGroup(toMesh)).build();
        }
    }

    /**
     * In the case that meshing is occurring and that the chunk is not renderable, a previous model can be rendered instead. To use this feature, set the previous model using {@link
     * org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel#setPrevious(org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel)}. This previous model will be used until the
     * mesh becomes available. At this point, the previous model will be destroyed, and the new one rendered. When a model isn't needed anymore, you must call {@link
     * org.spoutcraft.client.nterface.mesh.ParallelChunkMesher.ChunkModel#destroy()} to dispose of it completely. This will also cancel the meshing if it's in progress, and destroy the previous
     * model.
     */
    public static class ChunkModel extends Model {
        private Future<VertexData> mesh;
        private ChunkModel previous;

        private ChunkModel(Future<VertexData> mesh) {
            this.mesh = mesh;
        }

        @Override
        public void render() {
            // If we have no vertex array, but the mesh is done
            if (getVertexArray() == null && mesh.isDone()) {
                // Get the mesh
                final VertexData vertexData;
                try {
                    vertexData = mesh.get();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                mesh = null;
                // Create the vertex array from the mesh
                final VertexArray vertexArray = Renderer.getGLFactory().createVertexArray();
                vertexArray.setData(vertexData);
                vertexArray.create();
                setVertexArray(vertexArray);
                // Destroy and discard the previous model (if any), as it is now obsolete
                if (previous != null) {
                    previous.destroy();
                    previous = null;
                }
            }
            // If we have a vertex array, we can render
            if (getVertexArray() != null) {
                super.render();
            } else if (previous != null) {
                // Else, fall back on the previous model
                previous.render();
            }
        }

        /**
         * Sets the previous model to renderer until the updated one is ready.
         *
         * @param previous The previous model
         */
        public void setPrevious(ChunkModel previous) {
            this.previous = previous;
        }

        /**
         * Destroys the models, cancelling the meshing task if in progress, and the previous model (if any).
         */
        public void destroy() {
            // If we have a vertex array, destroy it
            if (getVertexArray() != null) {
                getVertexArray().destroy();
            } else {
                // Else, the mesh is still in progress, cancel that
                mesh.cancel(false);
                // Also destroy the previous model if we have one
                if (previous != null) {
                    previous.destroy();
                }
            }
        }
    }
}
